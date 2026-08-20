package com.momatic.domain.meeting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.repository.TeamMemberRepository;
import com.momatic.domain.team.repository.TeamRepository;
import com.momatic.domain.usage.entity.UsageRecord;
import com.momatic.domain.usage.entity.UsageType;
import com.momatic.domain.usage.repository.UsageRecordRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MeetingUploadServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "uploader@example.com";
    private static final Long TEAM_ID = 2L;
    private static final String TITLE = "주간 회의";

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsageRecordRepository usageRecordRepository;

    @Mock
    private MeetingProcessingService meetingProcessingService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private MeetingFileStorageService meetingFileStorageService;

    @InjectMocks
    private MeetingUploadService meetingUploadService;

    private User user;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        user = User.create(
                USER_EMAIL,
                "업로드 사용자",
                "ROLE_USER",
                "google",
                "provider-id"
        );
        ReflectionTestUtils.setField(user, "id", USER_ID);
        file = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("허용되지 않는 파일 형식으로 업로드하면 예외가 발생한다")
    void uploadThrowsWhenFileTypeIsNotAllowed() {
        // given
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("meeting.txt");

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> meetingUploadService.upload(USER_EMAIL, null, TITLE, file)
        );

        // then
        assertEquals(ErrorCode.UPLOAD_INVALID_FILE_TYPE, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀 회의 업로드 시 요청자가 팀 소속이 아니면 예외가 발생한다")
    void uploadThrowsWhenRequesterIsNotTeamMember() {
        // given
        PlanPolicy planPolicy = PlanPolicy.FREE;
        Team team = mock(Team.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("meeting.mp3");
        when(file.getContentType()).thenReturn("audio/mpeg");
        when(file.getSize()).thenReturn(planPolicy.getMaxFileSizeBytes());
        when(userRepository.findByEmailForUpdate(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionService.getActivePlan(USER_ID)).thenReturn(planPolicy);
        when(usageRecordRepository
                .countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        any(Long.class),
                        any(String.class),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                )).thenReturn(0L);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(teamMemberRepository.existsByTeamIdAndUserId(TEAM_ID, USER_ID))
                .thenReturn(false);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> meetingUploadService.upload(USER_EMAIL, TEAM_ID, TITLE, file)
        );

        // then
        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("파일 크기가 플랜의 최대 허용 크기를 초과하면 예외가 발생한다")
    void uploadThrowsWhenFileSizeExceedsPlanLimit() {
        // given
        PlanPolicy planPolicy = PlanPolicy.FREE;
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("meeting.mp3");
        when(file.getContentType()).thenReturn("audio/mpeg");
        when(file.getSize()).thenReturn(planPolicy.getMaxFileSizeBytes() + 1L);
        when(userRepository.findByEmailForUpdate(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionService.getActivePlan(USER_ID)).thenReturn(planPolicy);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> meetingUploadService.upload(USER_EMAIL, null, TITLE, file)
        );

        // then
        assertEquals(ErrorCode.UPLOAD_FILE_SIZE_EXCEEDED, exception.getErrorCode());
    }

    @Test
    @DisplayName("월간 업로드 횟수가 플랜 한도에 도달하면 예외가 발생한다")
    void uploadThrowsWhenMonthlyUploadCountReachesPlanLimit() {
        // given
        PlanPolicy planPolicy = PlanPolicy.FREE;
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("meeting.mp3");
        when(file.getContentType()).thenReturn("audio/mpeg");
        when(file.getSize()).thenReturn(planPolicy.getMaxFileSizeBytes());
        when(userRepository.findByEmailForUpdate(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionService.getActivePlan(USER_ID)).thenReturn(planPolicy);
        when(usageRecordRepository
                .countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        any(Long.class),
                        any(String.class),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                )).thenReturn(planPolicy.getMonthlyUploadCount());

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> meetingUploadService.upload(USER_EMAIL, null, TITLE, file)
        );

        // then
        assertEquals(ErrorCode.UPLOAD_MONTHLY_LIMIT_EXCEEDED, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상적인 개인 회의 업로드 시 사용량 기록이 저장된다")
    void uploadSavesUsageRecordForPersonalMeeting() {
        // given
        PlanPolicy planPolicy = PlanPolicy.FREE;
        long fileSize = planPolicy.getMaxFileSizeBytes();
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("meeting.mp3");
        when(file.getContentType()).thenReturn("audio/mpeg");
        when(file.getSize()).thenReturn(fileSize);
        when(userRepository.findByEmailForUpdate(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionService.getActivePlan(USER_ID)).thenReturn(planPolicy);
        when(usageRecordRepository
                .countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        any(Long.class),
                        any(String.class),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                )).thenReturn(0L);
        when(meetingFileStorageService.storeFile(file)).thenReturn("stored-meeting.mp3");
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> {
            Meeting meeting = invocation.getArgument(0);
            ReflectionTestUtils.setField(meeting, "id", 10L);
            return meeting;
        });
        TransactionSynchronizationManager.initSynchronization();

        try {
            // when
            meetingUploadService.upload(USER_EMAIL, null, TITLE, file);

            // then
            ArgumentCaptor<UsageRecord> captor = ArgumentCaptor.forClass(UsageRecord.class);
            verify(usageRecordRepository).save(captor.capture());
            UsageRecord usageRecord = captor.getValue();
            assertEquals(UsageType.UPLOAD.name(), usageRecord.getUsageType());
            assertEquals(file.getSize(), usageRecord.getFileSizeBytes().longValue());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
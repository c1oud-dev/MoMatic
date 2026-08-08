package com.momatic.domain.meeting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.momatic.domain.meeting.entity.FailedFileDeletion;
import com.momatic.domain.meeting.entity.FailedFileDeletionStatus;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.repository.FailedFileDeletionRepository;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MeetingServiceIntegrationTest {

    @MockBean
    private MeetingFileStorageService meetingFileStorageService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private FailedFileDeletionRepository failedFileDeletionRepository;

    @Test
    @DisplayName("회의 삭제 후 커밋되면 저장 파일 삭제가 트리거된다")
    void deleteMeetingTriggersStoredFileDeletionAfterCommit() throws IOException {
        // given
        String uniqueValue = UUID.randomUUID().toString();
        User owner = saveUser(uniqueValue);
        String storedFileName = "stored-" + uniqueValue + ".mp3";
        Meeting meeting = saveMeeting(owner, storedFileName);

        // when
        meetingService.deleteMeeting(meeting.getId(), owner.getEmail());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(meetingFileStorageService, times(1))
                .deleteFile(storedFileName);
    }

    @Test
    @DisplayName("저장 파일 삭제가 실패하면 삭제 실패 기록이 저장된다")
    void deleteMeetingRecordsFailureWhenStoredFileDeletionFails() throws IOException {
        // given
        String uniqueValue = UUID.randomUUID().toString();
        User owner = saveUser(uniqueValue);
        String storedFileName = "stored-" + uniqueValue + ".mp3";
        Meeting meeting = saveMeeting(owner, storedFileName);
        doThrow(new IOException("파일 삭제 실패"))
                .when(meetingFileStorageService)
                .deleteFile(storedFileName);

        // when
        meetingService.deleteMeeting(meeting.getId(), owner.getEmail());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        List<FailedFileDeletion> failures = failedFileDeletionRepository.findAllByStatus(
                FailedFileDeletionStatus.PENDING
        );
        long matchingFailureCount = failures.stream()
                .filter(failure -> storedFileName.equals(failure.getStoredFileName()))
                .count();
        assertEquals(1L, matchingFailureCount);
    }

    private User saveUser(String uniqueValue) {
        return userRepository.save(User.create(
                "owner-" + uniqueValue + "@example.com",
                "회의 소유자",
                "ROLE_USER",
                "google",
                "provider-" + uniqueValue
        ));
    }

    private Meeting saveMeeting(User owner,
                                String storedFileName) {
        return meetingRepository.save(Meeting.createPending(
                "회의 제목",
                storedFileName,
                "meeting.mp3",
                null,
                owner
        ));
    }
}
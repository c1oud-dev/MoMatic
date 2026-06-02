package com.momatic.domain.meeting.service;

import com.momatic.domain.meeting.aop.UploadLimitCheck;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.subscription.repository.SubscriptionRepository;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.repository.TeamRepository;
import com.momatic.domain.usage.entity.UsageRecord;
import com.momatic.domain.usage.repository.UsageRecordRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/** 회의 파일 업로드 서비스입니다. */
@Service
@RequiredArgsConstructor
public class MeetingUploadService {

    private static final String FREE_PLAN = "FREE";
    private static final String PRO_PLAN = "PRO";
    private static final String USAGE_TYPE_UPLOAD = "UPLOAD";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp3", "mp4", "wav", "m4a");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "audio/mpeg",
            "audio/mp4",
            "audio/wav",
            "audio/x-wav",
            "audio/m4a",
            "video/mp4"
    );

    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final MeetingProcessingService meetingProcessingService;

    @Value("${app.upload.storage-path}")
    private String storagePath;

    @Value("${app.upload.limit.free.max-file-size-bytes}")
    private long freeMaxFileSize;

    @Value("${app.upload.limit.pro.max-file-size-bytes}")
    private long proMaxFileSize;

    /**
     * 음성 파일을 업로드합니다.
     *
     * @param userId 사용자 ID
     * @param teamId 팀 ID
     * @param title 회의 제목
     * @param file 업로드 파일
     * @return 저장된 회의
     */
    @UploadLimitCheck
    @Transactional
    public Meeting upload(Long userId, Long teamId, String title, MultipartFile file) {
        String planType = findPlanType(userId);
        validateFile(file, planType);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String storedFileName = storeFile(file);
        Meeting meeting = Meeting.createPending(title, storedFileName, file.getOriginalFilename(), team, owner);
        Meeting savedMeeting = meetingRepository.save(meeting);

        usageRecordRepository.save(UsageRecord.create(owner, USAGE_TYPE_UPLOAD, 1L, file.getSize()));
        processMeetingAfterCommit(savedMeeting.getId());
        return savedMeeting;
    }

    /**
     * 사용자의 최신 구독 플랜을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 구독 플랜 타입
     */
    private String findPlanType(Long userId) {
        return subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(subscription -> subscription.getPlanType().toUpperCase())
                .orElse(FREE_PLAN);
    }

    /**
     * 업로드 트랜잭션 커밋 이후 회의 비동기 처리를 시작합니다.
     *
     * @param meetingId 회의 ID
     */
    private void processMeetingAfterCommit(Long meetingId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /** 업로드 트랜잭션 커밋 이후 회의 처리를 요청합니다. */
            @Override
            public void afterCommit() {
                meetingProcessingService.processMeeting(meetingId);
            }
        });
    }

    /**
     * 파일 형식 및 크기를 검증합니다.
     *
     * @param file 업로드 파일
     * @param planType 플랜 타입
     */
    private void validateFile(MultipartFile file, String planType) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String mimeType = file.getContentType();

        if (!ALLOWED_EXTENSIONS.contains(extension)
                || mimeType == null
                || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new CustomException(ErrorCode.UPLOAD_INVALID_FILE_TYPE);
        }

        long maxFileSize = PRO_PLAN.equalsIgnoreCase(planType)
                ? proMaxFileSize
                : freeMaxFileSize;
        if (file.getSize() > maxFileSize) {
            throw new CustomException(ErrorCode.UPLOAD_FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 파일 확장자를 추출합니다.
     *
     * @param originalFilename 원본 파일명
     * @return 소문자 확장자
     */
    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new CustomException(ErrorCode.UPLOAD_INVALID_FILE_TYPE);
        }

        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 파일을 로컬 스토리지에 저장합니다.
     *
     * @param file 업로드 파일
     * @return 저장 파일명
     */
    private String storeFile(MultipartFile file) {
        try {
            Path directoryPath = Paths.get(storagePath);
            Files.createDirectories(directoryPath);
            String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = directoryPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }
}


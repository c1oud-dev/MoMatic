package com.momatic.domain.meeting.service;

import com.momatic.domain.meeting.aop.UploadLimitCheck;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.repository.MeetingRepository;
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

import jakarta.annotation.Nullable;
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
    private final UsageRecordRepository usageRecordRepository;
    private final MeetingProcessingService meetingProcessingService;

    @Value("${app.upload.storage-path}")
    private String storagePath;

    /**
     * 음성 파일을 업로드합니다.
     *
     * @param userId 사용자 ID
     * @param teamId 팀 ID, 개인 회의록이면 null
     * @param title 회의 제목
     * @param file 업로드 파일
     * @return 저장된 회의
     */
    @UploadLimitCheck
    @Transactional
    public Meeting upload(Long userId, @Nullable Long teamId, String title, MultipartFile file) {
        validateFile(file);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Team team = findUploadTeam(teamId, owner);

        String storedFileName = storeFile(file);
        Meeting meeting = Meeting.createPending(title, storedFileName, file.getOriginalFilename(), team, owner);
        Meeting savedMeeting = meetingRepository.save(meeting);

        usageRecordRepository.save(UsageRecord.create(owner, USAGE_TYPE_UPLOAD, 1L, file.getSize()));
        processMeetingAfterCommit(savedMeeting.getId());
        return savedMeeting;
    }

    /**
     * 업로드 대상 팀을 조회하고 업로드 사용자의 팀 소속 여부를 검증합니다.
     *
     * @param teamId 팀 ID, 개인 회의록이면 null
     * @param owner 업로드 사용자
     * @return 업로드 대상 팀 또는 null
     */
    private Team findUploadTeam(@Nullable Long teamId,
                                User owner) {
        if (teamId == null) {
            return null;
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
        if (team.getMembers().stream()
                .noneMatch(member -> member.getUser().getId().equals(owner.getId()))) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return team;
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
     * 파일 존재 여부와 형식을 검증합니다.
     *
     * @param file 업로드 파일
     */
    private void validateFile(MultipartFile file) {
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


package com.momatic.domain.user.service;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.payment.repository.PaymentRepository;
import com.momatic.domain.usage.repository.UsageRecordRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 사용자 계정 관리를 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final MeetingRepository meetingRepository;

    @Value("${app.upload.storage-path}")
    private String storagePath;

    /**
     * 현재 인증된 사용자의 계정과 연관 데이터를 하드 삭제하고 세션을 무효화합니다.
     *
     * @param email 삭제할 사용자 이메일
     * @param session 현재 HTTP 세션
     */
    @Transactional
    public void deleteAccount(String email,
                              HttpSession session) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Long userId = user.getId();

        paymentRepository.deleteByUserId(userId);
        usageRecordRepository.deleteByUserId(userId);
        deleteMeetingFiles(userId);
        meetingRepository.deleteByOwnerId(userId);
        userRepository.delete(user);
        session.invalidate();
    }

    /**
     * 사용자가 소유한 회의의 로컬 업로드 파일을 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    private void deleteMeetingFiles(Long userId) {
        for (Meeting meeting : meetingRepository.findAllByOwnerId(userId)) {
            deleteMeetingFile(meeting.getStoredFileName());
        }
    }

    /**
     * 저장 파일명에 해당하는 로컬 파일을 삭제합니다.
     *
     * @param storedFileName 저장 파일명
     */
    private void deleteMeetingFile(String storedFileName) {
        try {
            Path filePath = Paths.get(storagePath).resolve(storedFileName);
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
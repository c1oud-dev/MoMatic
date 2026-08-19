package com.momatic.domain.admin.dto;

import com.momatic.domain.meeting.entity.FailedFileDeletion;
import com.momatic.domain.meeting.entity.FailedFileDeletionStatus;
import java.time.LocalDateTime;

/**
 * 관리자 파일 삭제 재시도 항목 응답입니다.
 *
 * @param id 파일 삭제 기록 ID
 * @param fileName 저장 파일명
 * @param status 처리 상태
 * @param retryCount 재시도 횟수
 * @param lastFailureAt 마지막 실패 시각
 * @param lastFailureReason 마지막 실패 사유
 * @param createdAt 생성일
 */
public record AdminFileDeletionResponse(
        Long id,
        String fileName,
        FailedFileDeletionStatus status,
        int retryCount,
        LocalDateTime lastFailureAt,
        String lastFailureReason,
        LocalDateTime createdAt
) {

    /**
     * 파일 삭제 실패 엔티티를 관리자 응답으로 변환합니다.
     *
     * @param entity 파일 삭제 실패 엔티티
     * @return 관리자 파일 삭제 재시도 응답
     */
    public static AdminFileDeletionResponse from(FailedFileDeletion entity) {
        return new AdminFileDeletionResponse(
                entity.getId(),
                entity.getStoredFileName(),
                entity.getStatus(),
                entity.getRetryCount(),
                entity.getLastAttemptAt(),
                entity.getLastFailureReason(),
                entity.getCreatedAt()
        );
    }
}
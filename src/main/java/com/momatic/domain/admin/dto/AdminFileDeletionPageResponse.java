package com.momatic.domain.admin.dto;

import com.momatic.domain.meeting.entity.FailedFileDeletion;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 관리자 파일 삭제 재시도 목록과 요약 응답입니다.
 *
 * @param content 현재 페이지 항목
 * @param page 현재 페이지 번호
 * @param totalPages 전체 페이지 수
 * @param totalElements 필터 조건의 전체 항목 수
 * @param hasPrevious 이전 페이지 존재 여부
 * @param hasNext 다음 페이지 존재 여부
 * @param pendingCount 대기 중 항목 수
 * @param givenUpCount 포기 항목 수
 */
public record AdminFileDeletionPageResponse(
        List<AdminFileDeletionResponse> content,
        int page,
        int totalPages,
        long totalElements,
        boolean hasPrevious,
        boolean hasNext,
        long pendingCount,
        long givenUpCount
) {

    /**
     * 엔티티 페이지와 상태 요약을 관리자 목록 응답으로 변환합니다.
     *
     * @param entities 파일 삭제 실패 엔티티 페이지
     * @param pendingCount 대기 중 항목 수
     * @param givenUpCount 포기 항목 수
     * @return 관리자 파일 삭제 재시도 목록 응답
     */
    public static AdminFileDeletionPageResponse from(Page<FailedFileDeletion> entities,
                                                     long pendingCount,
                                                     long givenUpCount) {
        return new AdminFileDeletionPageResponse(
                entities.map(AdminFileDeletionResponse::from).getContent(),
                entities.getNumber(),
                entities.getTotalPages(),
                entities.getTotalElements(),
                entities.hasPrevious(),
                entities.hasNext(),
                pendingCount,
                givenUpCount
        );
    }
}
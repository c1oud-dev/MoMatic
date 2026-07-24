package com.momatic.domain.meeting.entity;

/** 회의 파일 삭제 실패 기록의 처리 상태입니다. */
public enum FailedFileDeletionStatus {
    /** 삭제 재시도 대기 상태입니다. */
    PENDING,
    /** 삭제 재시도 성공 상태입니다. */
    RESOLVED,
    /** 최대 재시도 횟수를 초과해 포기한 상태입니다. */
    GIVEN_UP
}
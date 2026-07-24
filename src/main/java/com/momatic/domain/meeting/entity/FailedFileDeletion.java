package com.momatic.domain.meeting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 삭제에 실패한 회의 업로드 파일의 재시도 상태를 기록하는 엔티티입니다. */
@Entity
@Table(name = "failed_file_deletion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FailedFileDeletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FailedFileDeletionStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastAttemptAt;

    /**
     * 파일 삭제 실패 기록을 생성합니다.
     *
     * @param storedFileName 저장 파일명
     * @return 생성된 파일 삭제 실패 기록
     */
    public static FailedFileDeletion create(String storedFileName) {
        FailedFileDeletion failedFileDeletion = new FailedFileDeletion();
        failedFileDeletion.storedFileName = storedFileName;
        failedFileDeletion.status = FailedFileDeletionStatus.PENDING;
        failedFileDeletion.retryCount = 0;
        failedFileDeletion.createdAt = LocalDateTime.now();
        return failedFileDeletion;
    }

    /** 삭제 재시도 성공 상태로 변경합니다. */
    public void markResolved() {
        this.status = FailedFileDeletionStatus.RESOLVED;
        this.lastAttemptAt = LocalDateTime.now();
    }

    /**
     * 삭제 재시도 실패 횟수를 기록하고 최대 횟수 도달 시 포기 상태로 변경합니다.
     *
     * @param maxRetryCount 최대 재시도 횟수
     */
    public void recordFailedAttempt(int maxRetryCount) {
        this.retryCount++;
        this.lastAttemptAt = LocalDateTime.now();
        if (this.retryCount >= maxRetryCount) {
            this.status = FailedFileDeletionStatus.GIVEN_UP;
        }
    }
}
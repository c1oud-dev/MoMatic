package com.momatic.domain.subscription.repository;

import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 구독 정보 조회를 위한 레포지토리입니다. */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 관리자 화면에서 사용할 구독 목록을 사용자 정보와 함께 조회합니다.
     *
     * @param pageable 페이지 요청 정보
     * @return 사용자 정보가 페치된 구독 페이지
     */
    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<Subscription> findAll(Pageable pageable);

    /**
     * 사용자의 상태별 최신 구독 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param status 구독 상태
     * @return 구독 정보
     */
    Optional<Subscription> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            SubscriptionStatus status
    );

    /**
     * 만료 시각이 지난 활성 구독을 조회합니다.
     *
     * @param status 구독 상태
     * @param expiredAt 기준 만료 시각
     * @return 만료 대상 구독 목록
     */
    List<Subscription> findAllByStatusAndExpiredAtBefore(
            SubscriptionStatus status,
            LocalDateTime expiredAt
    );
}

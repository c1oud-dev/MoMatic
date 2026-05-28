package com.momatic.domain.subscription.repository;

import com.momatic.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 구독 정보 조회를 위한 레포지토리입니다. */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 사용자 최신 구독 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 구독 정보
     */
    Optional<Subscription> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}

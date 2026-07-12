package com.momatic.domain.subscription.service;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.dto.SubscriptionSummary;
import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.subscription.entity.SubscriptionStatus;
import com.momatic.domain.subscription.repository.SubscriptionRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 사용자 구독 조회 및 변경을 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * 이메일에 해당하는 사용자의 활성 구독을 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 활성 구독 정보
     */
    @Transactional(readOnly = true)
    public Optional<Subscription> getActiveSubscription(String email) {
        User user = findUser(email);
        return findActiveSubscription(user.getId());
    }

    /**
     * 이메일에 해당하는 사용자의 구독 현황 요약을 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 구독 현황 요약 DTO
     */
    @Transactional(readOnly = true)
    public SubscriptionSummary getSubscriptionSummary(String email) {
        return getActiveSubscription(email)
                .map(SubscriptionSummary::from)
                .orElseGet(SubscriptionSummary::free);
    }

    /**
     * 사용자의 활성 플랜을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 활성 플랜 또는 무료 플랜
     */
    @Transactional(readOnly = true)
    public PlanPolicy getActivePlan(Long userId) {
        return findActiveSubscription(userId)
                .map(Subscription::getPlanType)
                .orElse(PlanPolicy.FREE);
    }

    /**
     * 이메일 기준으로 사용자의 구독 플랜을 업그레이드합니다.
     *
     * @param email 사용자 이메일
     * @param planType 변경할 플랜 문자열
     * @return 변경된 구독
     */
    @Transactional
    public Subscription upgrade(String email,
                                String planType) {
        return upgrade(findUser(email).getId(), PlanPolicy.from(planType));
    }

    /**
     * 사용자 ID 기준으로 구독 플랜을 업그레이드합니다.
     *
     * @param userId 사용자 ID
     * @param planType 변경할 플랜
     * @return 변경된 구독
     */
    @Transactional
    public Subscription upgrade(Long userId,
                                PlanPolicy planType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Subscription subscription = findActiveSubscription(userId)
                .orElseGet(() -> Subscription.createActive(user, planType));
        subscription.upgrade(planType);
        return subscriptionRepository.save(subscription);
    }

    /**
     * ID에 해당하는 구독을 만료 처리합니다.
     *
     * @param subscriptionId 구독 ID
     * @return 만료된 구독
     */
    @Transactional
    public Subscription expireSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        subscription.expire();
        return subscription;
    }

    /**
     * 사용자의 활성 구독을 만료 처리합니다.
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void expireActiveSubscription(Long userId) {
        findActiveSubscription(userId).ifPresent(Subscription::expire);
    }

    /**
     * 사용자의 활성 구독을 취소 처리합니다.
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void cancelActiveSubscription(Long userId) {
        findActiveSubscription(userId).ifPresent(Subscription::cancel);
    }

    /** 자정 스케줄러에서 만료 시각이 지난 활성 구독을 일괄 만료 처리합니다. */
    @Transactional
    public void expireSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAllByStatusAndExpiredAtBefore(
                SubscriptionStatus.ACTIVE,
                LocalDateTime.now()
        );
        subscriptions.forEach(Subscription::expire);
    }

    /**
     * 사용자의 활성 구독을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 활성 구독 정보
     */
    private Optional<Subscription> findActiveSubscription(Long userId) {
        return subscriptionRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(
                userId,
                SubscriptionStatus.ACTIVE
        );
    }

    /**
     * 이메일에 해당하는 사용자를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티
     */
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}

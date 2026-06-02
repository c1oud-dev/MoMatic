package com.momatic.domain.subscription.service;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.subscription.entity.SubscriptionStatus;
import com.momatic.domain.subscription.repository.SubscriptionRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
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
     * 사용자의 구독 플랜을 업그레이드합니다.
     *
     * @param email 사용자 이메일
     * @param planType 변경할 플랜 문자열
     * @return 변경된 구독
     */
    @Transactional
    public Subscription upgrade(String email,
                                String planType) {
        User user = findUser(email);
        PlanPolicy planPolicy = PlanPolicy.from(planType);
        Subscription subscription = findActiveSubscription(user.getId())
                .orElseGet(() -> Subscription.createActive(user, planPolicy));
        subscription.upgrade(planPolicy);
        return subscriptionRepository.save(subscription);
    }

    /**
     * 구독을 만료 처리합니다.
     *
     * @param subscriptionId 구독 ID
     * @return 만료된 구독
     */
    @Transactional
    public Subscription expire(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        subscription.expire();
        return subscription;
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

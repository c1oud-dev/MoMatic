package com.momatic.domain.admin.service;

import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.subscription.repository.SubscriptionRepository;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 콘솔 사용자 및 구독 관리를 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    /**
     * 전체 사용자 목록을 페이지 단위로 조회합니다.
     *
     * @param pageable 페이지 요청 정보
     * @return 사용자 페이지
     */
    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 전체 구독 목록을 페이지 단위로 조회합니다.
     *
     * @param pageable 페이지 요청 정보
     * @return 구독 페이지
     */
    @Transactional(readOnly = true)
    public Page<Subscription> findAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable);
    }

    /**
     * 사용자의 활성 구독을 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 활성 구독 정보
     */
    @Transactional(readOnly = true)
    public Optional<Subscription> getActiveSubscription(String email) {
        return subscriptionService.getActiveSubscription(email);
    }

    /**
     * 관리자 권한으로 사용자의 구독 플랜을 변경합니다.
     *
     * @param userId 사용자 ID
     * @param planType 변경할 플랜 타입
     */
    @Transactional
    public void changePlan(Long userId,
                           String planType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        subscriptionService.upgrade(user.getEmail(), planType);
    }
}
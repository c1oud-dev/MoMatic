package com.momatic.domain.user.service;

import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Google OAuth2 로그인 시 사용자 정보를 동기화하는 서비스입니다.
 * CustomOAuth2UserService의 상속 구조로 인한 트랜잭션 프록시 문제를 해결하기 위해 별도 빈으로 분리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자를 조회하고, 없으면 신규 생성합니다.
     *
     * @param email 사용자 이메일
     * @param name 사용자 이름
     * @return 동기화된 사용자 엔티티
     */
    @Transactional
    public User syncUser(String email, String name) {
        log.info("syncUser 호출: email={}", email);
        return userRepository.findByEmail(email)
                .map(found -> {
                    found.updateProfile(name);
                    return userRepository.save(found);
                })
                .orElseGet(() -> userRepository.save(
                        User.create(email, name, "ROLE_USER", "google", email)
                ));
    }
}
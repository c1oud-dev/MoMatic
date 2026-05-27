package com.momatic.domain.user.repository;

import com.momatic.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 사용자 엔티티 저장소입니다. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 이메일로 사용자를 조회합니다. */
    Optional<User> findByEmail(String email);
}
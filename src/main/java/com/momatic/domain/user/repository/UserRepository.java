package com.momatic.domain.user.repository;

import com.momatic.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** 사용자 엔티티 저장소입니다. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 이메일로 사용자를 조회합니다. */
    Optional<User> findByEmail(String email);

    /**
     * 사용자 행에 쓰기 락을 설정하여 조회합니다.
     *
     * @param id 사용자 ID
     * @return 조회된 사용자
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}
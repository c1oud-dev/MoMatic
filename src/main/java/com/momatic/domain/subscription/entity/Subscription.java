package com.momatic.domain.subscription.entity;

import com.momatic.domain.user.entity.User;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

/** 사용자 구독 정보를 표현하는 엔티티입니다. */
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String planType;

    @Column(nullable = false)
    private String status;

    private LocalDate startedDate;

    private LocalDate endedDate;
}

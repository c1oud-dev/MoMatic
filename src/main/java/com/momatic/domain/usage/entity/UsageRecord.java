package com.momatic.domain.usage.entity;

import com.momatic.domain.user.entity.User;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;

/** 사용자 사용량 기록을 표현하는 엔티티입니다. */
@Entity
@Table(name = "usage_records")
public class UsageRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String usageType;

    @Column(nullable = false)
    private Long usedAmount;
}

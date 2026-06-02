package com.momatic.domain.subscription.entity;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.user.entity.User;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 사용자 구독 정보를 표현하는 엔티티입니다. */
@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanPolicy planType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime expiredAt;

    /**
     * 활성 구독을 생성합니다.
     *
     * @param user 구독 사용자
     * @param planType 구독 플랜
     * @return 활성 구독
     */
    public static Subscription createActive(User user,
                                            PlanPolicy planType) {
        Subscription subscription = new Subscription();
        subscription.user = user;
        subscription.planType = planType;
        subscription.status = SubscriptionStatus.ACTIVE;
        subscription.startedAt = LocalDateTime.now();
        return subscription;
    }

    /**
     * 구독 플랜을 변경하고 활성 상태로 전환합니다.
     *
     * @param planType 변경할 플랜
     */
    public void upgrade(PlanPolicy planType) {
        this.planType = planType;
        this.status = SubscriptionStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.expiredAt = planType == PlanPolicy.FREE
                ? null
                : this.startedAt.plusMonths(1);
    }

    /** 구독을 만료 처리하고 무료 플랜으로 전환합니다. */
    public void expire() {
        this.planType = PlanPolicy.FREE;
        this.status = SubscriptionStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();
    }
}

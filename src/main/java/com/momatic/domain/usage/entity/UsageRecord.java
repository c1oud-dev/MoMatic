package com.momatic.domain.usage.entity;

import com.momatic.domain.user.entity.User;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 사용량 기록을 표현하는 엔티티입니다. */
@Entity
@Table(name = "usage_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    /**
     * 사용량 레코드를 생성합니다.
     *
     * @param user 사용자
     * @param usageType 사용 타입
     * @param usedAmount 사용량
     * @return 생성된 사용량 레코드
     */
    public static UsageRecord create(User user, String usageType, Long usedAmount) {
        UsageRecord usageRecord = new UsageRecord();
        usageRecord.user = user;
        usageRecord.usageType = usageType;
        usageRecord.usedAmount = usedAmount;
        return usageRecord;
    }
}

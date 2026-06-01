package com.momatic.domain.actionItem.entity;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 회의 액션 아이템을 표현하는 엔티티입니다. */
@Entity
@Table(name = "action_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActionItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String task;

    private LocalDate dueDate;

    private String assignee;

    @Enumerated(EnumType.STRING)
    private ActionStatus status = ActionStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    /**
     * 액션 아이템 엔티티를 생성합니다.
     *
     * @param task 액션 아이템 내용
     * @param assignee 담당자
     * @param dueDate 마감일
     * @return 생성된 액션 아이템
     */
    public static ActionItem create(String task, String assignee, LocalDate dueDate) {
        ActionItem actionItem = new ActionItem();
        actionItem.task = task;
        actionItem.assignee = assignee;
        actionItem.dueDate = dueDate;
        actionItem.status = ActionStatus.TODO;
        return actionItem;
    }

    /**
     * 액션 아이템 상태를 변경합니다.
     *
     * @param status 변경할 상태
     */
    public void updateStatus(ActionStatus status) {
        this.status = status;
    }

    /**
     * 액션 아이템의 회의를 설정합니다.
     *
     * @param meeting 회의
     */
    public void assignMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}

package com.momatic.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ActionItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String task;          // 해야 할 일
    private String assignee;      // 담당자
    private LocalDate dueDate;    // 마감일

    @Enumerated(EnumType.STRING)
    private Status status = Status.TODO;

    public enum Status { TODO, IN_PROGRESS, DONE }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;
}

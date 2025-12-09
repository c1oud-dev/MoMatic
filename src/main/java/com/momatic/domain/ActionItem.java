package com.momatic.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "action_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ActionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String task;

    private String assignee;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private ActionStatus status = ActionStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    @JsonIgnore
    private Meeting meeting;

    public ActionItem() {
    }

    public ActionItem(String task, String assignee, LocalDate dueDate, ActionStatus status) {
        this.task = task;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}

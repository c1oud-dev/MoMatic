package com.momatic.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Column(length = 5000)
    private String summary;   // GPT 요약 저장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties({"members", "meetings"})
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"team"})
    private User owner;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionItem> actionItems = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transcript> transcripts = new ArrayList<>();

    public Meeting() {
    }

    public Meeting(String title, LocalDateTime startedAt, LocalDateTime endedAt, String summary) {
        this.title = title;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<ActionItem> getActionItems() {
        return actionItems;
    }

    public List<Transcript> getTranscripts() {
        return transcripts;
    }
}

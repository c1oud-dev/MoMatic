package com.momatic.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "transcripts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String speaker;

    @Column(length = 5000)
    private String content;

    private Integer startSec;

    private Integer endSec;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    @JsonIgnore
    private Meeting meeting;

    public Transcript() {
    }

    public Transcript(String speaker, String content, Integer startSec, Integer endSec) {
        this.speaker = speaker;
        this.content = content;
        this.startSec = startSec;
        this.endSec = endSec;
    }

    public Long getId() {
        return id;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStartSec() {
        return startSec;
    }

    public void setStartSec(Integer startSec) {
        this.startSec = startSec;
    }

    public Integer getEndSec() {
        return endSec;
    }

    public void setEndSec(Integer endSec) {
        this.endSec = endSec;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}

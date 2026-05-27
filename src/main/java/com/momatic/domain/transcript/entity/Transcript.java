package com.momatic.domain.transcript.entity;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;

/** 회의 발화 전사 정보를 표현하는 엔티티입니다. */
@Entity
@Table(name = "transcripts")
public class Transcript extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String speaker;

    @Lob
    private String content;

    private Double startSec;

    private Double endSec;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;
}

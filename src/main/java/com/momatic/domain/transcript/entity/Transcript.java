package com.momatic.domain.transcript.entity;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회의 발화 전사 정보를 표현하는 엔티티입니다. */
@Entity
@Table(name = "transcripts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    /**
     * 전사 엔티티 생성 정적 팩토리 메서드입니다.
     *
     * @param speaker 화자
     * @param content 내용
     * @param startSec 시작 초
     * @param endSec 종료 초
     * @return 생성된 전사 엔티티
     */
    public static Transcript create(String speaker,
                                    String content,
                                    Double startSec,
                                    Double endSec) {
        Transcript transcript = new Transcript();
        transcript.speaker = speaker;
        transcript.content = content;
        transcript.startSec = startSec;
        transcript.endSec = endSec;
        return transcript;
    }

    /**
     * 전사의 회의를 설정합니다.
     *
     * @param meeting 회의
     */
    public void assignMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}

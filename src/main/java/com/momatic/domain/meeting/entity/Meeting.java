package com.momatic.domain.meeting.entity;

import com.momatic.domain.team.entity.Team;
import com.momatic.domain.transcript.entity.Transcript;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 회의 정보를 표현하는 엔티티입니다. */
@Entity
@Table(name = "meetings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MeetingStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Lob
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionItem> actionItems = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transcript> transcripts = new ArrayList<>();

    /**
     * 업로드 직후 회의 엔티티를 생성합니다.
     *
     * @param title 회의 제목
     * @param storedFileName 저장 파일명
     * @param originalFileName 원본 파일명
     * @param team 팀
     * @param owner 소유자
     * @return 생성된 회의 엔티티
     */
    public static Meeting createPending(String title,
                                        String storedFileName,
                                        String originalFileName,
                                        Team team,
                                        User owner) {
        Meeting meeting = new Meeting();
        meeting.title = title;
        meeting.storedFileName = storedFileName;
        meeting.originalFileName = originalFileName;
        meeting.team = team;
        meeting.owner = owner;
        meeting.status = MeetingStatus.PENDING;
        return meeting;
    }

    /**
     * 회의 상태를 변경합니다.
     *
     * @param status 변경할 상태
     */
    public void updateStatus(MeetingStatus status) {
        this.status = status;
    }

    /**
     * 회의 요약을 변경합니다.
     *
     * @param summary 변경할 회의 요약
     */
    public void updateSummary(String summary) {
        this.summary = summary;
    }
}

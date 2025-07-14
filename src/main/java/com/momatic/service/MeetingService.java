package com.momatic.service;

import com.momatic.domain.ActionItem;
import com.momatic.domain.Meeting;
import com.momatic.domain.Transcript;
import com.momatic.repository.MeetingRepository;
import com.momatic.util.SimpleKoreanDateParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepo;
    private final SlackService slackService;
    private final GoogleCalendarService calendarService;
    private final JiraService jiraService;
    private final SimpleKoreanDateParser dateParser;

    /**
     * 저장 및 Slack/Calendar/Jira 알림 수행
     */
    public Long saveAndNotify(Meeting meeting,
                              String rawTranscript,
                              List<ActionItem> items) {
        // Transcript 생성
        Transcript t = Transcript.builder()
                .speaker("system")
                .content(rawTranscript)
                .meeting(meeting)
                .build();

        meeting.setStartedAt(LocalDateTime.now());
        meeting.getTranscripts().add(t);
        meeting.getActionItems().addAll(items);
        items.forEach(a -> a.setMeeting(meeting));

        Meeting saved = meetingRepo.save(meeting);

        // Slack 알림
        slackService.send(buildSlackMessage(saved));

        // Google Calendar 및 Jira 연동
        items.forEach(ai -> {
            dateParser.parse(ai.getDueDate()).ifPresent(date -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = (auth != null && auth.isAuthenticated())
                        ? auth.getName()
                        : "system";
                calendarService.createEvent(username, ai.getTask(), date);
            });
            jiraService.createIssue(
                    ai.getTask(),
                    "Generated from MoMatic meeting #" + saved.getId()
            );
        });

        return saved.getId();
    }

    /**
     * Slack 메시지 본문 생성 헬퍼
     */
    private String buildSlackMessage(Meeting m) {
        StringBuilder sb = new StringBuilder();
        sb.append("*📝 Meeting Summary*\n> ")
                .append(m.getSummary())
                .append("\n\n*✅ Action Items*");
        m.getActionItems().forEach(a -> sb.append("\n• ")
                .append(a.getTask())
                .append(" — _").append(a.getAssignee()).append("_")
                .append(" (").append(a.getDueDate()).append(")"));
        return sb.toString();
    }

    @PreAuthorize("#meeting.team.id == principal.team.id")
    public Meeting createMeeting(Meeting meeting) {
        return meetingRepo.save(meeting);
    }

    @PreAuthorize("#teamId == principal.team.id")
    public List<Meeting> getTeamMeetings(String teamId) {
        return meetingRepo.findByTeamId(teamId);
    }

    /**
     * identifier로 받은 ID로 조회,
     * 유효하지 않거나 존재하지 않으면 가장 최신 회의를 반환
     */
    public Meeting getLatestMeeting(String identifier) {
        if (identifier != null && !identifier.isBlank()) {
            try {
                Long id = Long.parseLong(identifier);
                return meetingRepo.findById(id)
                        .orElseGet(() -> meetingRepo.findTopByOrderByIdDesc()
                                .orElseThrow(() -> new RuntimeException("No meetings found")));
            } catch (NumberFormatException ignored) {
            }
        }
        return meetingRepo.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("No meetings found"));
    }
}

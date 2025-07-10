package com.momatic.service;

import com.momatic.domain.ActionItem;
import com.momatic.domain.Meeting;
import com.momatic.domain.Transcript;
import com.momatic.repository.MeetingRepository;
import com.momatic.util.SimpleKoreanDateParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepo;
    private final SlackService slackService;
    private final GoogleCalendarService calendarService;
    private final JiraService jiraService;
    private final SimpleKoreanDateParser dateParser;

    public Long saveAndNotify(Meeting meeting,
                              String rawTranscript,
                              List<ActionItem> items) {

        // Transcript
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

        /* Slack */
        slackService.send(buildSlackMessage(saved));

        /* Calendar & Jira */
        items.forEach(ai -> {

            /* ① Google Calendar ------------------------------------ */
            dateParser.parse(ai.getDueDate()).ifPresent(date -> {
                // 로그인 사용자가 있으면 email, 없으면 "system"으로 처리
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = (auth != null && auth.isAuthenticated())
                        ? auth.getName()          // 예: user@example.com
                        : "system";               // 옵션 A(permitAll) 테스트용

                calendarService.createEvent(username, ai.getTask(), date);
            });

            /* ② Jira ------------------------------------------------ */
            jiraService.createIssue(
                    ai.getTask(),
                    "Generated from MoMatic meeting #" + saved.getId()
            );
        });

        return saved.getId();
    }

    /* ---------------- private helpers ---------------- */

    private String buildSlackMessage(Meeting m) {
        StringBuilder sb = new StringBuilder();
        sb.append("*📝 Meeting Summary*\n> ").append(m.getSummary()).append("\n\n")
                .append("*✅ Action Items*");
        m.getActionItems().forEach(a -> sb.append("\n• ")
                .append(a.getTask())
                .append(" — _").append(a.getAssignee()).append("_")
                .append(" (").append(a.getDueDate()).append(")"));
        return sb.toString();
    }
}

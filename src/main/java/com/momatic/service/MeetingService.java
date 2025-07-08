package com.momatic.service;

import com.momatic.domain.ActionItem;
import com.momatic.domain.Meeting;
import com.momatic.domain.Transcript;
import com.momatic.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepo;   // ✅ 주입
    private final SlackService slackService;       // ✅ 주입

    /**
     * 회의 저장 후 Slack 전송
     */
    public Long saveAndNotify(Meeting meeting,
                              String rawTranscript,
                              List<ActionItem> items) {

        // Transcript 엔티티 생성
        Transcript transcript = Transcript.builder()
                .speaker("system")
                .content(rawTranscript)
                .startSec(0.0)
                .endSec(0.0)
                .meeting(meeting)
                .build();

        items.forEach(a -> a.setMeeting(meeting));
        meeting.getTranscripts().add(transcript);
        meeting.getActionItems().addAll(items);

        Meeting saved = meetingRepo.save(meeting);

        // Slack 전송
        slackService.send(buildSlackMessage(saved));
        return saved.getId();
    }

    /* Slack 메시지 포맷 헬퍼 */
    private String buildSlackMessage(Meeting m) {
        StringBuilder sb = new StringBuilder();
        sb.append("*📝 Meeting Summary*\n")
                .append("> ").append(m.getSummary()).append("\n\n")
                .append("*✅ Action Items*");
        m.getActionItems().forEach(a ->
                sb.append("\n• ")
                        .append(a.getTask())
                        .append(" — _").append(a.getAssignee()).append("_")
                        .append(" (due ").append(a.getDueDate()).append(")")
        );
        return sb.toString();
    }
}

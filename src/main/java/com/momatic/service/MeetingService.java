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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepo;
    private final GoogleCalendarService calendarService;
    private final SimpleKoreanDateParser dateParser;

    /**
     * 저장 및 후처리 알림 수행
     */
    public Long saveAndNotify(Meeting meeting,
                              String rawTranscript,
                              List<ActionItem> items) {
        List<ActionItem> safeItems = items != null ? new ArrayList<>(items) : new ArrayList<>();

        // Transcript 생성
        Transcript t = Transcript.builder()
                .speaker("system")
                .content(rawTranscript)
                .meeting(meeting)
                .build();

        if (meeting.getStartedAt() == null) {
            meeting.setStartedAt(LocalDateTime.now());
        }

        meeting.setEndedAt(LocalDateTime.now());
        meeting.getTranscripts().add(t);
        meeting.getActionItems().addAll(safeItems);
        safeItems.forEach(a -> a.setMeeting(meeting));

        Meeting saved = meetingRepo.save(meeting);

        // Google Calendar 연동 (Due date가 존재하는 ActionItem만)
        safeItems.forEach(ai -> {
            dateParser.parse(ai.getDueDate()).ifPresent(date -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = (auth != null && auth.isAuthenticated())
                        ? auth.getName()
                        : "system";
                calendarService.createEvent(username, ai.getTask(), date);
            });
        });

        return saved.getId();
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

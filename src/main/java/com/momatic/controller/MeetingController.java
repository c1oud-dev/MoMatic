package com.momatic.controller;

import com.momatic.domain.ActionItem;
import com.momatic.domain.Meeting;
import com.momatic.domain.Transcript;
import com.momatic.service.MeetingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<List<Meeting>> listMeetings() {
        return ResponseEntity.ok(meetingService.findAllMeetings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingDetailResponse> getMeeting(@PathVariable Long id) {
        Meeting meeting = meetingService.findMeeting(id);
        List<ActionItem> actionItems = meetingService.findActionItems(id);
        List<Transcript> transcripts = meetingService.findTranscripts(id);
        return ResponseEntity.ok(new MeetingDetailResponse(meeting, actionItems, transcripts));
    }

    public record MeetingDetailResponse(Meeting meeting, List<ActionItem> actionItems, List<Transcript> transcripts) {
    }
}

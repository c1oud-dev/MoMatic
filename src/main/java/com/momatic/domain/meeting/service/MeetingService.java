package com.momatic.domain.meeting.service;

import com.momatic.domain.action.entity.ActionItem;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.transcript.entity.Transcript;
import com.momatic.domain.action.repository.ActionItemRepository;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.transcript.repository.TranscriptRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final ActionItemRepository actionItemRepository;
    private final TranscriptRepository transcriptRepository;

    public MeetingService(MeetingRepository meetingRepository,
                          ActionItemRepository actionItemRepository,
                          TranscriptRepository transcriptRepository) {
        this.meetingRepository = meetingRepository;
        this.actionItemRepository = actionItemRepository;
        this.transcriptRepository = transcriptRepository;
    }

    @Transactional(readOnly = true)
    public List<Meeting> findAllMeetings() {
        return meetingRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Meeting findMeeting(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ActionItem> findActionItems(Long meetingId) {
        return actionItemRepository.findByMeetingId(meetingId);
    }

    @Transactional(readOnly = true)
    public List<Transcript> findTranscripts(Long meetingId) {
        return transcriptRepository.findByMeetingId(meetingId);
    }

    @Transactional
    public Meeting saveWithDetails(Meeting meeting,
                                   @Nullable String rawTranscript,
                                   List<ActionItem> actionItems) {
        Meeting savedMeeting = meetingRepository.save(meeting);

        for (ActionItem item : actionItems) {
            item.setMeeting(savedMeeting);
            actionItemRepository.save(item);
        }
        if (rawTranscript != null && !rawTranscript.isBlank()) {
            Transcript transcript = new Transcript(
                    "Auto",
                    rawTranscript,
                    0d,
                    (double) rawTranscript.length());
            transcript.setMeeting(savedMeeting);
            transcriptRepository.save(transcript);
        }

        return savedMeeting;
    }
}

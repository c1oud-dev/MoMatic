package com.momatic.service;

import com.momatic.domain.ActionItem;
import com.momatic.domain.Meeting;
import com.momatic.domain.Transcript;
import com.momatic.repository.ActionItemRepository;
import com.momatic.repository.MeetingRepository;
import com.momatic.repository.TranscriptRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
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
}

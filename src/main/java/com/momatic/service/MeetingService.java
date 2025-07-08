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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingService {
    private final MeetingRepository meetingRepo;

    public Long saveFullMeeting(String title,
                                String summary,
                                List<Transcript> transcripts,
                                List<ActionItem> actionItems) {

        Meeting meeting = Meeting.builder()
                .title(title)
                .startedAt(LocalDateTime.now())
                .summary(summary)
                .build();

        transcripts.forEach(t -> t.setMeeting(meeting));
        actionItems.forEach(a -> a.setMeeting(meeting));

        meeting.getTranscripts().addAll(transcripts);
        meeting.getActionItems().addAll(actionItems);

        Meeting saved = meetingRepo.save(meeting);
        return saved.getId();
    }

    public Optional<Meeting> findById(Long id) {
        return meetingRepo.findById(id);
    }
}

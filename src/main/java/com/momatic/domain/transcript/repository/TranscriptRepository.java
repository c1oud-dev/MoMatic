package com.momatic.domain.transcript.repository;

import com.momatic.domain.transcript.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
    List<Transcript> findByMeetingId(Long meetingId);
}

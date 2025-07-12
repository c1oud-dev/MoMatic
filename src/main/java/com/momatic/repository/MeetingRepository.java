package com.momatic.repository;

import com.momatic.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByTeamId(String teamId);
}

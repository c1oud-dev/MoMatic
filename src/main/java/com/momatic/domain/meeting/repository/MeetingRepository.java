package com.momatic.domain.meeting.repository;

import com.momatic.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MeetingRepository extends JpaRepository<Meeting, Long> {

}

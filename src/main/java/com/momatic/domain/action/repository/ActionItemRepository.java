package com.momatic.domain.action.repository;

import com.momatic.domain.action.entity.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {
    List<ActionItem> findByMeetingId(Long meetingId);
}

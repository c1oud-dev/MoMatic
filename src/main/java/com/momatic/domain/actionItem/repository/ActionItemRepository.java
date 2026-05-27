package com.momatic.domain.actionItem.repository;

import com.momatic.domain.actionItem.entity.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {
    List<ActionItem> findByMeetingId(Long meetingId);
}

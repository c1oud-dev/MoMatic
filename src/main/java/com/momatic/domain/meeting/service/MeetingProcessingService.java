package com.momatic.domain.meeting.service;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.entity.MeetingStatus;
import com.momatic.domain.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 회의 비동기 처리 서비스입니다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingProcessingService {

    private final MeetingRepository meetingRepository;

    /**
     * 회의 처리 파이프라인을 비동기로 실행합니다.
     *
     * @param meetingId 회의 ID
     */
    @Async("meetingTaskExecutor")
    @Transactional
    public void processMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("meeting not found"));

        meeting.updateStatus(MeetingStatus.PROCESSING);

        try {
            // TODO: 2차 작업에서 AI 전사/요약/액션아이템 추출 호출 구현
            meeting.updateStatus(MeetingStatus.COMPLETED);
        } catch (Exception exception) {
            meeting.updateStatus(MeetingStatus.FAILED);
            log.error("회의 비동기 처리 실패. meetingId={}", meetingId, exception);
        }
    }
}


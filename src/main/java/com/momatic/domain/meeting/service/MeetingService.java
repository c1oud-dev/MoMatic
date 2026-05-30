package com.momatic.domain.meeting.service;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.transcript.entity.Transcript;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.transcript.repository.TranscriptRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회의 도메인 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final ActionItemRepository actionItemRepository;
    private final TranscriptRepository transcriptRepository;

    /**
     * 전체 회의를 조회합니다.
     *
     * @return 회의 목록
     */
    @Transactional(readOnly = true)
    public List<Meeting> findAllMeetings() {
        return meetingRepository.findAll();
    }

    /**
     * 회의 단건을 조회합니다.
     *
     * @param id 회의 ID
     * @return 조회된 회의
     */
    @Transactional(readOnly = true)
    public Meeting findMeeting(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
    }

    /**
     * 인증 사용자 소유의 회의를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @param ownerEmail 소유자 이메일
     * @return 조회된 회의
     */
    @Transactional(readOnly = true)
    public Meeting findOwnedMeeting(Long meetingId, String ownerEmail) {
        return meetingRepository.findByIdAndOwnerEmail(meetingId, ownerEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
    }

    /**
     * 회의 상세를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @return 회의 상세
     */
    @Transactional(readOnly = true)
    public MeetingDetail getMeetingDetail(Long meetingId) {
        Meeting meeting = findMeeting(meetingId);
        List<ActionItem> actionItems = actionItemRepository.findByMeetingId(meetingId);
        List<Transcript> transcripts = transcriptRepository.findByMeetingId(meetingId);
        return new MeetingDetail(meeting, actionItems, transcripts);
    }

    /**
     * 회의 및 부가 데이터를 저장합니다.
     *
     * @param meeting 저장할 회의 엔티티
     * @param rawTranscript 원문 전사 텍스트
     * @param actionItems 저장할 액션 아이템 목록
     * @return 저장된 회의 엔티티
     */
    @Transactional
    public Meeting saveWithDetails(Meeting meeting,
                                   @Nullable String rawTranscript,
                                   List<ActionItem> actionItems) {
        Meeting savedMeeting = meetingRepository.save(meeting);

        for (ActionItem item : actionItems) {
            item.assignMeeting(savedMeeting);
            actionItemRepository.save(item);
        }
        if (rawTranscript != null && !rawTranscript.isBlank()) {
            Transcript transcript = Transcript.create(
                    "Auto",
                    rawTranscript,
                    0d,
                    (double) rawTranscript.length()
            );
            transcript.assignMeeting(savedMeeting);
            transcriptRepository.save(transcript);
        }

        return savedMeeting;
    }

    /**
     * 회의 상세 조회 결과입니다.
     */
    public record MeetingDetail(Meeting meeting, List<ActionItem> actionItems, List<Transcript> transcripts) {
    }
}

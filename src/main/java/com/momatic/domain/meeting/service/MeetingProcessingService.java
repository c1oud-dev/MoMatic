package com.momatic.domain.meeting.service;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.entity.MeetingStatus;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.transcript.entity.Transcript;
import com.momatic.domain.transcript.repository.TranscriptRepository;
import com.momatic.infra.gpt.GptClient;
import com.momatic.infra.gpt.GptSummaryResult;
import com.momatic.infra.whisper.WhisperClient;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** 회의 비동기 처리 서비스입니다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingProcessingService {

    private final MeetingRepository meetingRepository;
    private final TranscriptRepository transcriptRepository;
    private final ActionItemRepository actionItemRepository;
    private final WhisperClient whisperClient;
    private final GptClient gptClient;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.upload.storage-path}")
    private String storagePath;

    /**
     * 회의 처리 파이프라인을 비동기로 실행합니다.
     *
     * @param meetingId 회의 ID
     */
    @Async("meetingTaskExecutor")
    public void processMeeting(Long meetingId) {
        Meeting meeting = updateStatus(meetingId, MeetingStatus.PROCESSING);

        try {
            File audioFile = resolveAudioFile(meeting).toFile();
            String transcriptText = whisperClient.transcribe(audioFile);
            GptSummaryResult summaryResult = gptClient.summarize(transcriptText);

            saveProcessingResult(meetingId, transcriptText, summaryResult);
        } catch (Exception exception) {
            updateStatus(meetingId, MeetingStatus.FAILED);
            log.error("회의 비동기 처리 실패. meetingId={}", meetingId, exception);
        }
    }

    /**
     * 회의 상태를 별도 트랜잭션으로 변경합니다.
     *
     * @param meetingId 회의 ID
     * @param status 변경할 상태
     * @return 상태가 변경된 회의
     */
    private Meeting updateStatus(Long meetingId, MeetingStatus status) {
        return transactionTemplate.execute(transactionStatus -> {
            Meeting meeting = findMeeting(meetingId);
            meeting.updateStatus(status);
            return meeting;
        });
    }

    /**
     * 회의 처리 결과를 별도 트랜잭션으로 저장합니다.
     *
     * @param meetingId 회의 ID
     * @param transcriptText 전사 텍스트
     * @param summaryResult GPT 분석 결과
     */
    private void saveProcessingResult(Long meetingId, String transcriptText, GptSummaryResult summaryResult) {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Meeting meeting = findMeeting(meetingId);
            meeting.updateSummary(summaryResult.summary());
            saveTranscript(meeting, transcriptText);
            saveActionItems(meeting, summaryResult.actionItems());
            meeting.updateStatus(MeetingStatus.COMPLETED);
        });
    }

    /**
     * 회의를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @return 조회된 회의
     */
    private Meeting findMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("meeting not found"));
    }

    /**
     * 회의에 연결된 업로드 음성 파일 경로를 계산합니다.
     *
     * @param meeting 처리할 회의
     * @return 업로드 음성 파일 경로
     */
    private Path resolveAudioFile(Meeting meeting) {
        return Paths.get(storagePath).resolve(meeting.getStoredFileName());
    }

    /**
     * 전사 텍스트를 Transcript 엔티티로 저장합니다.
     *
     * @param meeting 회의
     * @param transcriptText 전사 텍스트
     */
    private void saveTranscript(Meeting meeting, String transcriptText) {
        Transcript transcript = Transcript.create(
                "Whisper",
                transcriptText,
                0d,
                (double) transcriptText.length()
        );
        transcript.assignMeeting(meeting);
        transcriptRepository.save(transcript);
    }

    /**
     * GPT가 추출한 액션 아이템 목록을 저장합니다.
     *
     * @param meeting 회의
     * @param actionItemResults GPT 액션 아이템 결과 목록
     */
    private void saveActionItems(Meeting meeting, List<GptSummaryResult.ActionItemResult> actionItemResults) {
        if (actionItemResults == null || actionItemResults.isEmpty()) {
            return;
        }

        List<ActionItem> actionItems = actionItemResults.stream()
                .filter(item -> item.content() != null && !item.content().isBlank())
                .map(item -> {
                    ActionItem actionItem = ActionItem.create(item.content(), item.assignee(), item.dueDate());
                    actionItem.assignMeeting(meeting);
                    return actionItem;
                })
                .toList();
        actionItemRepository.saveAll(actionItems);
    }
}


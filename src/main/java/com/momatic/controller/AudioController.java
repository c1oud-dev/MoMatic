package com.momatic.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.domain.ActionItem;
import com.momatic.domain.ActionStatus;
import com.momatic.domain.Meeting;
import com.momatic.service.AudioService;
import com.momatic.service.LLMService;
import com.momatic.service.MeetingService;
import com.momatic.service.WhisperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audio")
public class AudioController {

    private final AudioService audioService;
    private final WhisperService whisperService;
    private final LLMService llmService;

    private final MeetingService meetingService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 파일 저장
            String path = audioService.saveFile(file);

            // 2. STT
            String transcript = whisperService.transcribe(path);

            // 3. 요약 + TODO 추출
            String resultJson = llmService.summarizeAndExtractTodos(transcript);

            // 4. JSON → 객체 파싱 (간단히 Map 사용 예시)
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resultJson);
            String summary = root.get("summary").asText();
            List<ActionItem> items = new ArrayList<>();
            root.get("actionItems").forEach(n -> {
                String dueDate = null;
                if (n.hasNonNull("dueDate") && !n.get("dueDate").asText().isBlank()) {
                    dueDate = n.get("dueDate").asText();
                }
                items.add(new ActionItem(
                        n.get("task").asText(),
                        n.get("assignee").asText(),
                        dueDate,
                        ActionStatus.TODO
                ));
            });

            // 5. Meeting 저장 및 후속 처리
            Meeting meeting = new Meeting(
                    "Auto-upload", // 필요시 파라미터화
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    summary
            );
            meetingService.saveWithDetails(meeting, transcript, items);

            return ResponseEntity.ok(resultJson);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

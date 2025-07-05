package com.momatic.controller;

import com.momatic.service.AudioService;
import com.momatic.service.LLMService;
import com.momatic.service.WhisperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audio")
public class AudioController {

    private final AudioService audioService;
    private final WhisperService whisperService;
    private final LLMService llmService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 파일 저장
            String savedFilePath = audioService.saveFile(file);
            log.info("파일 저장 경로: {}", savedFilePath);

            // 2. Whisper로 전사
            String transcript = whisperService.transcribe(savedFilePath);
            log.info("Transcription: {}", transcript);

            // 3. GPT로 요약 및 TODO 추출
            String result = llmService.summarizeAndExtractTodos(transcript);
            log.info("LLM 결과: {}", result);

            // 4. 응답 반환
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("오디오 업로드 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
        }
    }
}

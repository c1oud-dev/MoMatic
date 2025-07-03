package com.momatic.controller;

import com.momatic.service.AudioService;
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

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 파일 저장
            String filePath = audioService.saveFile(file);

            // 2. Whisper STT 요청
            String transcription = whisperService.transcribe(filePath);

            return ResponseEntity.ok("Transcription:\n" + transcription);
        } catch (Exception e) {
            log.error("오디오 업로드 또는 전사 실패", e);
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }
}

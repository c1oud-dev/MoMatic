package com.momatic.controller;

import com.momatic.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audio")
public class AudioController {

    private final AudioService audioService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        String filePath = audioService.saveFile(file);
        return ResponseEntity.ok("Uploaded to: " + filePath);
    }
}

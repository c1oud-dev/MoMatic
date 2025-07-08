package com.momatic.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class AudioService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 클라이언트로부터 받은 파일(MultipartFile)을 서버 디스크에 저장하고, 저장된 전체 경로를 반환하는 메서드
     */
    public String saveFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".mp3";
            String savedName = UUID.randomUUID() + ext;

            File dest = new File(uploadDir + File.separator + savedName);
            File parentDir = dest.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            file.transferTo(dest);

            log.info("파일 저장 완료: {}", dest.getAbsolutePath());

            return dest.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}

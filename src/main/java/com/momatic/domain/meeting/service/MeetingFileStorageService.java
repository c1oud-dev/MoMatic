package com.momatic.domain.meeting.service;

import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 회의 업로드 파일의 로컬 스토리지 저장과 삭제를 담당하는 서비스입니다. */
@Service
public class MeetingFileStorageService {

    @Value("${app.upload.storage-path}")
    private String storagePath;

    /**
     * 파일을 로컬 스토리지에 저장합니다.
     *
     * @param file 업로드 파일
     * @return 저장 파일명
     */
    public String storeFile(MultipartFile file) {
        try {
            Path directoryPath = Paths.get(storagePath);
            Files.createDirectories(directoryPath);
            String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = directoryPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * 로컬 스토리지에서 저장 파일을 삭제합니다.
     *
     * @param storedFileName 저장 파일명
     * @throws IOException 파일 삭제 실패 시 발생하는 예외
     */
    public void deleteFile(String storedFileName) throws IOException {
        Files.deleteIfExists(Paths.get(storagePath).resolve(storedFileName));
    }
}

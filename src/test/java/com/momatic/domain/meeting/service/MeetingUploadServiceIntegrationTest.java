package com.momatic.domain.meeting.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MeetingUploadServiceIntegrationTest {

    @MockBean
    private MeetingProcessingService meetingProcessingService;

    @MockBean
    private MeetingFileStorageService meetingFileStorageService;

    @Autowired
    private MeetingUploadService meetingUploadService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("업로드 성공 후 트랜잭션이 커밋되면 회의 비동기 처리가 트리거된다")
    void uploadTriggersMeetingProcessingAfterCommit() {
        // given
        String uniqueValue = UUID.randomUUID().toString();
        User user = userRepository.save(User.create(
                "uploader-" + uniqueValue + "@example.com",
                "업로드 사용자",
                "ROLE_USER",
                "google",
                "provider-" + uniqueValue
        ));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "meeting.mp3",
                "audio/mpeg",
                new byte[1000]
        );
        when(meetingFileStorageService.storeFile(any()))
                .thenReturn("stored-" + uniqueValue + ".mp3");

        // when
        meetingUploadService.upload(user.getEmail(), null, "회의 제목", file);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(meetingProcessingService, times(1))
                .processMeeting(any(Long.class));
    }
}
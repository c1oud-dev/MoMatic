package com.momatic.global.config;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.actionItem.entity.ActionStatus;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.meeting.entity.FailedFileDeletion;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.entity.MeetingStatus;
import com.momatic.domain.meeting.repository.FailedFileDeletionRepository;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.payment.entity.Payment;
import com.momatic.domain.payment.repository.PaymentRepository;
import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.repository.TeamRepository;
import com.momatic.domain.transcript.entity.Transcript;
import com.momatic.domain.transcript.repository.TranscriptRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** dev 프로파일에서 수동 QA에 필요한 페이징용 기본 데이터를 한 번 생성합니다. */
@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class BaseInitData implements ApplicationRunner {

    private static final String DEFAULT_EMAIL = "dev@momatic.com";
    private static final String QA_TEAM_NAME = "QA 페이징 테스트 팀";
    private static final int DATA_COUNT = 18;

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final MeetingRepository meetingRepository;
    private final TranscriptRepository transcriptRepository;
    private final ActionItemRepository actionItemRepository;
    private final PaymentRepository paymentRepository;
    private final FailedFileDeletionRepository failedFileDeletionRepository;

    @Value("${app.upload.storage-path}")
    private String storagePath;

    /**
     * 저장소별 기존 데이터 존재 여부를 확인한 뒤 비어 있는 QA 데이터만 생성합니다.
     *
     * @param args 애플리케이션 시작 인자
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User owner = userRepository.findByEmail(DEFAULT_EMAIL)
                .orElseGet(() -> userRepository.save(
                        User.create(DEFAULT_EMAIL, "개발자", "ROLE_USER", "google", DEFAULT_EMAIL)
                ));
        List<Meeting> meetings = createMeetingsIfEmpty(owner);
        createActionItemsIfEmpty(meetings);
        createPaymentsIfEmpty(owner);
        createFileDeletionQueueIfEmpty();
        log.info("QA base data initialization completed for {}", DEFAULT_EMAIL);
    }

    /**
     * 개인 및 팀 회의와 검색용 전사 데이터를 생성합니다.
     *
     * @param owner 회의 소유자
     * @return 액션 아이템을 연결할 회의 목록
     */
    private List<Meeting> createMeetingsIfEmpty(User owner) {
        if (meetingRepository.count() > 0) {
            return meetingRepository.findAllByOwnerId(owner.getId());
        }

        Team team = teamRepository.findByName(QA_TEAM_NAME)
                .orElseGet(() -> teamRepository.save(Team.create(QA_TEAM_NAME, owner)));
        List<Meeting> meetings = new ArrayList<>();
        for (int index = 1; index <= DATA_COUNT; index++) {
            String storedFileName = "qa-meeting-" + index + ".txt";
            Meeting meeting = Meeting.createPending(
                    meetingTitle(index),
                    storedFileName,
                    "QA_회의_녹음_" + index + ".txt",
                    index % 2 == 0 ? team : null,
                    owner
            );
            meeting.updateStatus(meetingStatus(index));
            meeting.updateSummary("QA검색키워드 요약 " + index
                    + ": 예산, 출시, 고객 피드백을 검토한 더미 회의입니다.");
            meetings.add(meetingRepository.save(meeting));
            createTranscript(meeting, index);
            createDummyFile(storedFileName, index);
        }
        return meetings;
    }

    /**
     * 회의 상태가 완료에 치우치면서도 모든 처리 상태를 포함하도록 결정합니다.
     *
     * @param index 데이터 순번
     * @return 회의 처리 상태
     */
    private MeetingStatus meetingStatus(int index) {
        if (index == 2) {
            return MeetingStatus.PENDING;
        }
        if (index == 4) {
            return MeetingStatus.PROCESSING;
        }
        if (index == 6) {
            return MeetingStatus.FAILED;
        }
        return MeetingStatus.COMPLETED;
    }

    /**
     * 검색 결과를 구분할 수 있는 회의 제목을 생성합니다.
     *
     * @param index 데이터 순번
     * @return 회의 제목
     */
    private String meetingTitle(int index) {
        String keyword = index % 3 == 0 ? "알파프로젝트" : index % 3 == 1 ? "베타예산" : "감마출시";
        return "QA " + keyword + " 회의 " + String.format("%02d", index);
    }

    /**
     * 회의 검색과 상세 화면 확인용 전사 한 건을 생성합니다.
     *
     * @param meeting 연결할 회의
     * @param index 데이터 순번
     */
    private void createTranscript(Meeting meeting, int index) {
        Transcript transcript = Transcript.create(
                "QA 화자 " + index,
                "전사검색키워드 " + index + " 고객 요구사항과 다음 일정을 논의했습니다.",
                0.0,
                45.0
        );
        transcript.assignMeeting(meeting);
        transcriptRepository.save(transcript);
    }

    /**
     * 다운로드 및 삭제 경로에서 사용할 더미 업로드 파일을 생성합니다.
     *
     * @param storedFileName 저장 파일명
     * @param index 데이터 순번
     */
    private void createDummyFile(String storedFileName, int index) {
        try {
            Path directory = Path.of(storagePath);
            Files.createDirectories(directory);
            Files.writeString(
                    directory.resolve(storedFileName),
                    "MoMatic QA dummy meeting file " + index,
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException("QA 더미 회의 파일을 생성할 수 없습니다.", exception);
        }
    }

    /**
     * 상태와 마감일이 고르게 분포된 액션 아이템을 생성합니다.
     *
     * @param meetings 연결 가능한 회의 목록
     */
    private void createActionItemsIfEmpty(List<Meeting> meetings) {
        if (actionItemRepository.count() > 0) {
            return;
        }
        if (meetings.isEmpty()) {
            log.warn("QA action items were skipped because the default user has no meetings");
            return;
        }

        for (int index = 1; index <= DATA_COUNT; index++) {
            LocalDate dueDate = index % 2 == 0 ? LocalDate.now().plusDays(index - 9L) : null;
            ActionItem item = ActionItem.create(
                    "QA 액션키워드 업무 " + String.format("%02d", index),
                    "담당자 " + ((index % 4) + 1),
                    dueDate
            );
            item.updateStatus(ActionStatus.values()[(index - 1) % ActionStatus.values().length]);
            item.assignMeeting(meetings.get((index - 1) % meetings.size()));
            actionItemRepository.save(item);
        }
    }

    /**
     * 완료, 실패, 처리 중 상태가 섞인 결제 이력을 생성합니다.
     *
     * @param owner 결제 사용자
     */
    private void createPaymentsIfEmpty(User owner) {
        if (paymentRepository.count() > 0) {
            return;
        }
        for (int index = 1; index <= DATA_COUNT; index++) {
            Payment payment = Payment.createPending(
                    "QA-ORDER-" + String.format("%03d", index),
                    BigDecimal.valueOf(19_900L + index),
                    PlanPolicy.PRO,
                    owner
            );
            int statusIndex = index % 3;
            if (statusIndex == 0) {
                payment.complete("QA-PAYMENT-KEY-" + index);
            } else if (statusIndex == 1) {
                payment.fail();
            } else {
                payment.markProcessing();
            }
            paymentRepository.save(payment);
        }
    }

    /** PENDING, GIVEN_UP, RESOLVED 상태가 섞인 파일 삭제 재시도 기록을 생성합니다. */
    private void createFileDeletionQueueIfEmpty() {
        if (failedFileDeletionRepository.count() > 0) {
            return;
        }
        for (int index = 1; index <= DATA_COUNT; index++) {
            FailedFileDeletion deletion = FailedFileDeletion.create("qa-deletion-missing-" + index + ".dat");
            if (index % 3 == 1) {
                deletion.recordFailedAttempt(1, "QA 수동 재시도 확인용 실패");
            } else if (index % 3 == 2) {
                deletion.markResolved();
            }
            failedFileDeletionRepository.save(deletion);
        }
    }
}
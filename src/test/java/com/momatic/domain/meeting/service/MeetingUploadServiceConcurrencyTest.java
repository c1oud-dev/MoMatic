package com.momatic.domain.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.subscription.repository.SubscriptionRepository;
import com.momatic.domain.usage.entity.UsageRecord;
import com.momatic.domain.usage.entity.UsageType;
import com.momatic.domain.usage.repository.UsageRecordRepository;
import com.momatic.domain.usage.util.UsagePeriod;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class MeetingUploadServiceConcurrencyTest {

    private static final int CONCURRENT_REQUEST_COUNT = 10;

    @MockBean
    private MeetingProcessingService meetingProcessingService;

    @MockBean
    private MeetingFileStorageService meetingFileStorageService;

    @Autowired
    private MeetingUploadService meetingUploadService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UsageRecordRepository usageRecordRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    private User user;
    private Subscription subscription;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        String uniqueValue = UUID.randomUUID().toString();
        user = userRepository.save(User.create(
                "concurrent-uploader-" + uniqueValue + "@example.com",
                "동시 업로드 사용자",
                "ROLE_USER",
                "google",
                "provider-" + uniqueValue
        ));
        subscription = subscriptionRepository.save(
                Subscription.createActive(user, PlanPolicy.FREE)
        );
        usageRecordRepository.saveAll(List.of(
                UsageRecord.create(user, UsageType.UPLOAD.name(), 1L, 1_000L),
                UsageRecord.create(user, UsageType.UPLOAD.name(), 1L, 1_000L)
        ));
        file = new MockMultipartFile(
                "file",
                "meeting.mp3",
                "audio/mpeg",
                new byte[1_000]
        );

        when(meetingFileStorageService.storeFile(any()))
                .thenReturn("stored-" + uniqueValue + ".mp3");

        // 작업 스레드의 독립 트랜잭션에서 준비 데이터를 조회할 수 있도록 커밋한다.
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @AfterEach
    void tearDown() {
        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        }

        meetingRepository.deleteByOwnerId(user.getId());
        usageRecordRepository.deleteByUserId(user.getId());
        subscriptionRepository.deleteById(subscription.getId());
        userRepository.deleteById(user.getId());
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    @DisplayName("동일 사용자의 동시 업로드는 비관적 락으로 직렬화되어 월간 한도를 초과하지 않는다")
    void testConcurrentUploadWithPlanLimit_ShouldEnforceLimitWithLock() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUEST_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(CONCURRENT_REQUEST_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        Queue<Throwable> unexpectedExceptions = new ConcurrentLinkedQueue<>();
        Queue<ErrorCode> failureErrorCodes = new ConcurrentLinkedQueue<>();

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < CONCURRENT_REQUEST_COUNT; index++) {
                final int idx = index;
                Future<?> future = executorService.submit(() -> {
                    readyLatch.countDown();
                    startLatch.await();

                    try {
                        meetingUploadService.upload(
                                user.getId(),
                                null,
                                "동시 업로드 회의 " + idx,
                                file
                        );
                        successCount.incrementAndGet();
                    } catch (CustomException exception) {
                        failureCount.incrementAndGet();
                        failureErrorCodes.add(exception.getErrorCode());
                    } catch (Throwable throwable) {
                        unexpectedExceptions.add(throwable);
                    }
                    return null;
                });
                futures.add(future);
            }

            assertThat(readyLatch.await(10, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            startLatch.countDown();
            executorService.shutdownNow();
            assertThat(executorService.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        TestTransaction.start();
        UsagePeriod period = UsagePeriod.currentMonth();
        long finalUsageCount = usageRecordRepository
                .countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        user.getId(),
                        UsageType.UPLOAD.name(),
                        period.start(),
                        period.end()
                );

        assertThat(unexpectedExceptions).isEmpty();
        assertThat(successCount).hasValue(1);
        assertThat(failureCount).hasValue(9);
        assertThat(failureErrorCodes)
                .hasSize(9)
                .containsOnly(ErrorCode.UPLOAD_MONTHLY_LIMIT_EXCEEDED);
        assertThat(finalUsageCount).isEqualTo(3);
        assertThat(meetingRepository.findAllByOwnerId(user.getId())).hasSize(1);
    }
}
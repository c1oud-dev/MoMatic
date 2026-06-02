package com.momatic.domain.dashboard.service;

import com.momatic.domain.actionItem.entity.ActionStatus;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.dashboard.dto.DashboardResponse;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.usage.repository.UsageRecordRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 대시보드 조회를 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final String USAGE_TYPE_UPLOAD = "UPLOAD";

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final UsageRecordRepository usageRecordRepository;
    private final MeetingRepository meetingRepository;
    private final ActionItemRepository actionItemRepository;

    /**
     * 인증 사용자의 대시보드 정보를 조회합니다.
     *
     * @param ownerEmail 인증 사용자 이메일
     * @return 대시보드 화면 정보
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String ownerEmail) {
        User user = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        PlanPolicy planPolicy = subscriptionService.getActivePlan(user.getId());
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime from = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime to = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        long monthlyUploadCount = usageRecordRepository
                .countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        user.getId(),
                        USAGE_TYPE_UPLOAD,
                        from,
                        to
                );
        long monthlyUploadLimit = planPolicy.getMonthlyUploadCount();
        long monthlyFileSizeBytes = usageRecordRepository.sumFileSizeBytes(
                user.getId(),
                USAGE_TYPE_UPLOAD,
                from,
                to
        );
        List<Meeting> recentMeetings = meetingRepository.findTop5ByOwnerEmailOrderByCreatedAtDesc(ownerEmail);

        return new DashboardResponse(
                monthlyUploadCount,
                monthlyUploadLimit,
                Math.max(monthlyUploadLimit - monthlyUploadCount, 0),
                monthlyFileSizeBytes,
                planPolicy.getMaxFileSizeBytes(),
                planPolicy.name(),
                DashboardResponse.toMeetingResponses(recentMeetings),
                actionItemRepository.findTop5ByMeetingOwnerEmailAndStatusInOrderByCreatedAtDesc(
                                ownerEmail,
                                List.of(ActionStatus.TODO, ActionStatus.IN_PROGRESS)
                        ).stream()
                        .map(DashboardResponse.ActionItemSummary::from)
                        .toList()
        );
    }
}
package com.momatic.domain.meeting.service;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.team.service.TeamPermissionService;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 회의 조회 및 편집 권한을 검증하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class MeetingPermissionService {

    private final UserRepository userRepository;
    private final TeamPermissionService teamPermissionService;

    /**
     * 회의 조회 가능 여부를 검증합니다.
     *
     * @param meeting 회의
     * @param requesterEmail 요청자 이메일
     */
    public void requireReadable(Meeting meeting,
                                String requesterEmail) {
        User requester = findUser(requesterEmail);
        if (!meeting.hasTeam()) {
            if (!meeting.getOwner().getId().equals(requester.getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            return;
        }

        teamPermissionService.requireMembership(
                meeting.getTeam().getId(),
                requester.getId()
        );
    }

    /**
     * 회의 편집 가능 여부를 검증합니다.
     *
     * @param meeting 회의
     * @param requesterEmail 요청자 이메일
     */
    public void requireEditable(Meeting meeting,
                                String requesterEmail) {
        User requester = findUser(requesterEmail);
        if (!meeting.hasTeam()) {
            if (!meeting.getOwner().getId().equals(requester.getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            return;
        }

        teamPermissionService.requireManagePermission(
                meeting.getTeam().getId(),
                requester.getId()
        );
    }

    /**
     * 회의 편집 가능 여부를 확인합니다.
     *
     * @param meeting 회의
     * @param requesterEmail 요청자 이메일
     * @return 회의 편집 가능 여부
     */
    public boolean isEditable(Meeting meeting,
                              String requesterEmail) {
        User requester = findUser(requesterEmail);
        if (!meeting.hasTeam()) {
            return meeting.getOwner().getId().equals(requester.getId());
        }
        return teamPermissionService.requireMembership(
                meeting.getTeam().getId(),
                requester.getId()
        ).canManageTeam();
    }

    /**
     * 사용자 이메일로 사용자를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티
     */
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
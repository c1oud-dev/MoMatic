package com.momatic.domain.meeting.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.service.TeamPermissionService;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MeetingPermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamPermissionService teamPermissionService;

    @InjectMocks
    private MeetingPermissionService meetingPermissionService;

    @Test
    @DisplayName("개인 회의는 소유자 본인이 조회하면 예외가 발생하지 않는다")
    void requireReadableDoesNotThrowWhenPrivateMeetingOwnerReads() {
        // given
        User owner = createUser(1L, "owner@example.com", "소유자");
        Meeting meeting = createMeeting(null, owner);
        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        // when
        assertDoesNotThrow(
                () -> meetingPermissionService.requireReadable(meeting, owner.getEmail())
        );

        // then
        verify(teamPermissionService, never()).requireMembership(any(), any());
    }

    @Test
    @DisplayName("개인 회의는 소유자가 아닌 사용자가 조회하면 예외가 발생한다")
    void requireReadableThrowsWhenPrivateMeetingNonOwnerReads() {
        // given
        User owner = createUser(1L, "owner@example.com", "소유자");
        User requester = createUser(2L, "requester@example.com", "요청자");
        Meeting meeting = createMeeting(null, owner);
        when(userRepository.findByEmail(requester.getEmail()))
                .thenReturn(Optional.of(requester));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> meetingPermissionService.requireReadable(meeting, requester.getEmail())
        );

        // then
        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀 회의는 팀 멤버십 검증을 위임한다")
    void requireReadableDelegatesMembershipCheckForTeamMeeting() {
        // given
        User owner = createUser(1L, "owner@example.com", "소유자");
        User requester = createUser(2L, "requester@example.com", "요청자");
        Team team = createTeam(10L, "개발팀", owner);
        Meeting meeting = createMeeting(team, owner);
        when(userRepository.findByEmail(requester.getEmail()))
                .thenReturn(Optional.of(requester));

        // when
        meetingPermissionService.requireReadable(meeting, requester.getEmail());

        // then
        verify(teamPermissionService).requireMembership(team.getId(), requester.getId());
    }

    @Test
    @DisplayName("개인 회의는 소유자 본인이 편집하면 예외가 발생하지 않는다")
    void requireEditableDoesNotThrowWhenPrivateMeetingOwnerEdits() {
        // given
        User owner = createUser(1L, "owner@example.com", "소유자");
        Meeting meeting = createMeeting(null, owner);
        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        // when
        assertDoesNotThrow(
                () -> meetingPermissionService.requireEditable(meeting, owner.getEmail())
        );

        // then
        verify(teamPermissionService, never()).requireManagePermission(any(), any());
    }

    @Test
    @DisplayName("개인 회의는 소유자가 아닌 사용자가 편집하면 예외가 발생한다")
    void requireEditableThrowsWhenPrivateMeetingNonOwnerEdits() {
        // given
        User owner = createUser(1L, "owner@example.com", "소유자");
        User requester = createUser(2L, "requester@example.com", "요청자");
        Meeting meeting = createMeeting(null, owner);
        when(userRepository.findByEmail(requester.getEmail()))
                .thenReturn(Optional.of(requester));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> meetingPermissionService.requireEditable(meeting, requester.getEmail())
        );

        // then
        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀 회의는 팀 관리 권한 검증을 위임한다")
    void requireEditableDelegatesManagePermissionCheckForTeamMeeting() {
        // given
        User owner = createUser(1L, "owner@example.com", "소유자");
        User requester = createUser(2L, "requester@example.com", "요청자");
        Team team = createTeam(10L, "개발팀", owner);
        Meeting meeting = createMeeting(team, owner);
        when(userRepository.findByEmail(requester.getEmail()))
                .thenReturn(Optional.of(requester));

        // when
        meetingPermissionService.requireEditable(meeting, requester.getEmail());

        // then
        verify(teamPermissionService).requireManagePermission(team.getId(), requester.getId());
    }

    private User createUser(Long id,
                            String email,
                            String name) {
        User user = User.create(
                email,
                name,
                "ROLE_USER",
                "google",
                "provider-id-" + id
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Team createTeam(Long id,
                            String name,
                            User owner) {
        Team team = Team.create(name, owner);
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private Meeting createMeeting(Team team,
                                  User owner) {
        return Meeting.createPending(
                "회의 제목",
                "stored-file-name.mp3",
                "original-file-name.mp3",
                team,
                owner
        );
    }
}
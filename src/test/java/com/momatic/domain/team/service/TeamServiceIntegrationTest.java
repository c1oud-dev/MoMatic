package com.momatic.domain.team.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.entity.TeamInvite;
import com.momatic.domain.team.repository.TeamRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.infra.mail.TeamInviteMailService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TeamServiceIntegrationTest {

    @MockBean
    private TeamInviteMailService teamInviteMailService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    @DisplayName("팀 초대 후 커밋되면 초대 메일 발송이 트리거된다")
    void inviteMemberTriggersInviteMailAfterCommit() {
        // given
        String uniqueValue = UUID.randomUUID().toString();
        User inviter = userRepository.save(User.create(
                "inviter-" + uniqueValue + "@example.com",
                "초대자",
                "ROLE_USER",
                "google",
                "provider-" + uniqueValue
        ));
        Team team = teamRepository.save(Team.create("팀 이름", inviter));
        String inviteeEmail = "invitee-" + UUID.randomUUID() + "@example.com";

        // when
        teamService.inviteMember(
                team.getId(),
                inviter.getEmail(),
                inviteeEmail
        );
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(teamInviteMailService, times(1))
                .sendTeamInvite(any(TeamInvite.class));
    }
}
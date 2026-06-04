package com.momatic.infra.mail;

import com.momatic.domain.team.entity.TeamInvite;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** 팀 초대 이메일 발송을 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class TeamInviteMailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.external.mail.from:no-reply@momatic.com}")
    private String from;

    @Value("${app.external.web.base-url:http://localhost:8083}")
    private String baseUrl;

    /**
     * 팀 초대 이메일을 발송합니다.
     *
     * @param invite 팀 초대 정보
     */
    public void sendTeamInvite(TeamInvite invite) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(invite.getInviteeEmail());
            helper.setSubject("MoMatic 팀 초대가 도착했습니다.");
            helper.setText(renderBody(invite), true);
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("팀 초대 이메일 생성에 실패했습니다.", exception);
        }
    }

    /**
     * 팀 초대 이메일 본문을 렌더링합니다.
     *
     * @param invite 팀 초대 정보
     * @return 렌더링된 이메일 본문
     */
    private String renderBody(TeamInvite invite) {
        Context context = new Context();
        context.setVariable("teamName", invite.getTeam().getName());
        context.setVariable("inviterName", invite.getInviter().getName());
        context.setVariable("joinUrl", baseUrl + "/teams/join?code=" + invite.getCode());
        context.setVariable("expiredAt", invite.getExpiredAt());
        return templateEngine.process("mail/team-invite", context);
    }
}
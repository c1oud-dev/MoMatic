package com.momatic.config;

import com.slack.api.Slack;
import com.slack.api.app_backend.SlackSignature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {
    @Bean
    public Slack slackClient() {
        return Slack.getInstance();
    }

    @Bean
    public SlackSignature.Verifier slackSignatureVerifier(SlackProperties props) {
        // SigningSecret → SlackSignature.Generator 인스턴스를 생성하고, Verifier에 주입
        SlackSignature.Generator generator = new SlackSignature.Generator(props.getSigningSecret());
        return new SlackSignature.Verifier(generator);
    }
}

package com.momatic.config;

import com.momatic.domain.*;
import com.momatic.repository.ActionItemRepository;
import com.momatic.repository.MeetingRepository;
import com.momatic.repository.TeamRepository;
import com.momatic.repository.TranscriptRepository;
import com.momatic.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(TeamRepository teamRepository,
                               UserRepository userRepository,
                               MeetingRepository meetingRepository,
                               ActionItemRepository actionItemRepository,
                               TranscriptRepository transcriptRepository) {
        return args -> {
            if (meetingRepository.count() > 0) {
                return;
            }

            Team team = new Team("AI Product Team");
            teamRepository.save(team);

            User owner = new User("pm@team.com", "PM Kim", "ROLE_PM");
            owner.setTeam(team);
            userRepository.save(owner);

            Meeting meeting = new Meeting(
                    "Voice AI Roadmap",
                    LocalDateTime.now().minusDays(1).withHour(10).withMinute(0),
                    LocalDateTime.now().minusDays(1).withHour(11).withMinute(30),
                    "모바일 앱 음성 명령 개선을 위한 MVP 범위와 일정을 정리했습니다."
            );
            meeting.setTeam(team);
            meeting.setOwner(owner);
            meetingRepository.save(meeting);

            ActionItem actionItem1 = new ActionItem(
                    "Wake-word 검출 정확도 90% 이상 달성 계획 수립",
                    "DSP Park",
                    LocalDate.now().plusDays(7).toString(),
                    ActionStatus.IN_PROGRESS
            );
            actionItem1.setMeeting(meeting);

            ActionItem actionItem2 = new ActionItem(
                    "테스터 10명 대상 사용자 피드백 수집",
                    "QA Lee",
                    LocalDate.now().plusDays(10).toString(),
                    ActionStatus.TODO
            );
            actionItem2.setMeeting(meeting);

            actionItemRepository.save(actionItem1);
            actionItemRepository.save(actionItem2);

            Transcript transcript1 = new Transcript(
                    "김프로",
                    "이번 스프린트에서는 웨이크워드 정확도를 먼저 끌어올리는 게 목표입니다.",
                    10,
                    42
            );
            transcript1.setMeeting(meeting);

            Transcript transcript2 = new Transcript(
                    "박엔지니어",
                    "데이터셋을 재정비하면 1주일 내 베타 모델을 만들 수 있어요.",
                    43,
                    85
            );
            transcript2.setMeeting(meeting);

            transcriptRepository.save(transcript1);
            transcriptRepository.save(transcript2);
        };
    }
}

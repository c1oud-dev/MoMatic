package com.momatic.global.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 비동기 실행 환경을 구성하는 설정 클래스입니다. */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${app.async.meeting.core-pool-size}")
    private int corePoolSize;

    @Value("${app.async.meeting.max-pool-size}")
    private int maxPoolSize;

    @Value("${app.async.meeting.queue-capacity}")
    private int queueCapacity;

    /**
     * 회의 처리 전용 스레드 풀 실행기를 생성합니다.
     *
     * @return 비동기 실행기
     */
    @Bean(name = "meetingTaskExecutor")
    public Executor meetingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("meeting-async-");
        executor.initialize();
        return executor;
    }
}

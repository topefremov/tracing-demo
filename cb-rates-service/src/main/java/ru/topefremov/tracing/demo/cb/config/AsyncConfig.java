package ru.topefremov.tracing.demo.cb.config;

import io.opentelemetry.context.Context;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public ThreadPoolTaskExecutorCustomizer threadPoolTaskExecutorCustomizer() {
        return taskExecutor -> taskExecutor.setTaskDecorator(runnable -> Context.current().wrap(runnable));
    }
}

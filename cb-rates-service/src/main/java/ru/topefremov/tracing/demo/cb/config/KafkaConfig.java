package ru.topefremov.tracing.demo.cb.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${cb-rates-topic:cb-rates}")
    private String cbRatesTopic;

    @Bean
    public NewTopic cbRatesTopic() {
        return TopicBuilder
                .name(cbRatesTopic)
                .partitions(1).build();
    }
}

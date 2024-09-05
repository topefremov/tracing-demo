package ru.topefremov.tracing.demo.cb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.topefremov.tracing.demo.cb.event.NewCbRatesRetrievedEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventProcessor {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${cb-rates-topic:cb-rates}")
    private String cbRatesTopic;

    @Async
    @EventListener(NewCbRatesRetrievedEvent.class)
    public void onNewCbRatesRetrievedEvent(NewCbRatesRetrievedEvent event) {
        log.info("Processing NewCbRatesRetrievedEvent started");
        kafkaTemplate.send(cbRatesTopic, event.rates());
        log.info("Processing NewCbRatesRetrievedEvent finished");
    }
}

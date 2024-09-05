package ru.topefremov.tracing.demo.cb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import ru.topefremov.tracing.demo.cb.event.NewCbRatesRetrievedEvent;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class CbRatesService {
    private final ApplicationEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    @Value("${cb-rates-provider-url-template:http://localhost:8081/api/cb-rates?date={date}}")
    private String urlTemplate;

    @Cacheable("cb-rates")
    public String getCbRates(LocalDate date) {
        Assert.notNull(date, "date must not be null");
        log.info("Retrieving cb rates for {}", date);
        String rates = restTemplate.getForObject(urlTemplate, String.class, date);
        log.info("Retrieved cb rates for {}", date);
        eventPublisher.publishEvent(new NewCbRatesRetrievedEvent(rates));
        return rates;
    }
}

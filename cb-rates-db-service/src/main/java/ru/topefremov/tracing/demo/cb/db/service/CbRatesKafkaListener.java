package ru.topefremov.tracing.demo.cb.db.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.topefremov.tracing.demo.cb.db.dto.CbRatesDto;

@Component
@Slf4j
@RequiredArgsConstructor
public class CbRatesKafkaListener {
    private final JdbcTemplate jdbcTemplate;

    @KafkaListener(topics = "${cb-rates-topic:cb-rates}")
    @Transactional
    public void onNewRates(CbRatesDto cbRatesDto) {
        log.info("New rates received: {}", cbRatesDto);
        cbRatesDto.rates().forEach(rate -> jdbcTemplate.update("INSERT INTO rates (id, date, alpha_code, scale, price) VALUES(?, ?, ?, ?, ?) " +
                        "ON CONFLICT (date, alpha_code) DO UPDATE SET id=EXCLUDED.id, scale=EXCLUDED.scale, price=EXCLUDED.price",
                cbRatesDto.id(), cbRatesDto.date(), rate.alphaCode(), rate.scale(), rate.price()));

    }
}

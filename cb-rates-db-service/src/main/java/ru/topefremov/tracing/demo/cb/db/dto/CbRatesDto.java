package ru.topefremov.tracing.demo.cb.db.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CbRatesDto(String id, LocalDate date, List<Rate> rates) {
    public record Rate(String alphaCode, Integer scale, BigDecimal price) {
    }
}

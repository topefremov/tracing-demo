package ru.topefremov.tracing.demo.cb.controller;

import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.topefremov.tracing.demo.cb.service.CbRatesService;

import java.time.LocalDate;

@RestController
@RequestMapping("api/cb-rates")
@RequiredArgsConstructor
@Slf4j
public class CbRatesController {
    private final CbRatesService cbRatesService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    String getRates(@RequestParam("date") LocalDate date) {
        log.info("Received request for rates on date {}", date);
        log.info("TraceId: {}", Span.current().getSpanContext().getTraceId());
        return cbRatesService.getCbRates(date);
    }
}

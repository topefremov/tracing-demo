package ru.topefremov.tracing.demo.cb.provider.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.topefremov.tracing.demo.cb.provider.service.CbRatesProviderService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cb-rates")
@RequiredArgsConstructor
public class CbRatesController {
    private final CbRatesProviderService cbRatesProviderService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String getCbRates(@RequestParam("date") LocalDate date) {
        return cbRatesProviderService.getCbRates(date);
    }
}

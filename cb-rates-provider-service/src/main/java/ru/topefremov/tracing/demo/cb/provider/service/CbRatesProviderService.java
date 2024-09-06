package ru.topefremov.tracing.demo.cb.provider.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CbRatesProviderService {
    public String getCbRates(LocalDate date) {
        Assert.notNull(date, "date must not be null");
        var template = """
                {
                  "id": "{id}",
                  "date": "{date}",
                  "rates": [
                    {
                      "alphaCode": "USD",
                      "scale": 1,
                      "price": {usd_price}
                    },
                    {
                      "alphaCode": "EUR",
                      "scale": 1,
                      "price": {eur_price}
                    }
                  ]
                }
                """;
        return template.replace("{id}", UUID.randomUUID().toString())
                .replace("{date}", date.toString())
                .replace("{usd_price}", generatePrice(new BigDecimal("60.2532"), new BigDecimal("92.3167")).toString())
                .replace("{eur_price}", generatePrice(new BigDecimal("60.2532"), new BigDecimal("92.3167")).toString());
    }

    public static BigDecimal generatePrice(BigDecimal min, BigDecimal max) {
        var randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));
        return randomBigDecimal.setScale(4, RoundingMode.HALF_UP);
    }
}

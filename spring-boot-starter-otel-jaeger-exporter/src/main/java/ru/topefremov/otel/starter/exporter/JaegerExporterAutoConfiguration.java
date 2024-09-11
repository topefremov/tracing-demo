package ru.topefremov.otel.starter.exporter;

import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ru.topefremov.tracing.demo.cb.exporter.JaegerAgentSpanExporterProvider;

@AutoConfiguration
@EnableConfigurationProperties(JaegerExporterProperties.class)
@AutoConfigureBefore(value = OpenTelemetryAutoConfiguration.class)
@ConditionalOnProperty(prefix = "otel.traces", value = "exporter", havingValue = "jaeger-agent")
public class JaegerExporterAutoConfiguration {
    @Bean
    ConfigurableSpanExporterProvider jaegerAgentSpanExporterProvider() {
        return new JaegerAgentSpanExporterProvider();
    }
}

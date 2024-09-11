package ru.topefremov.tracing.demo.cb.exporter;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class JaegerAgentSpanExporterProvider implements ConfigurableSpanExporterProvider {
    @Override
    public SpanExporter createExporter(ConfigProperties config) {
        return new JaegerAgentSpanExporter(config);
    }

    @Override
    public String getName() {
        return "jaeger-agent";
    }
}

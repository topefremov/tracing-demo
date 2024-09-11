package ru.topefremov.tracing.demo.cb.exporter;

import io.jaegertracing.agent.thrift.Agent;
import io.jaegertracing.thrift.internal.reporters.protocols.ThriftUdpTransport;
import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.*;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JaegerAgentSpanExporter implements SpanExporter {
    private final TTransport transport;
    private final Agent.Client client;

    public JaegerAgentSpanExporter(ConfigProperties config) {
        var host = config.getString("otel.exporter.jaeger.host");
        var port = config.getInt("otel.exporter.jaeger.port");
        try {
            this.transport = ThriftUdpTransport.newThriftUdpClient(host == null ? "localhost" : host, port == null ? 6831 : port);
            transport.open();
            var protocol = new TCompactProtocol(transport);
            this.client = new Agent.Client(protocol);
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        if (spans.isEmpty()) {
            return CompletableResultCode.ofSuccess();
        }

        log.info("Exporting spans started");
        var batch = buildBatch(spans);
        try {
            client.emitBatch(batch);
        } catch (TException e) {
            log.error("Failed to emit batch", e);
            return CompletableResultCode.ofFailure();
        }
        log.info("Exporting spans finished");
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        close();
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public void close() {
        if (transport.isOpen()) {
            transport.close();
        }
    }

    private Batch buildBatch(Collection<SpanData> spans) {
        Batch batch = new Batch();
        var firstSpan = spans.iterator().next();
        var jaegerSpans = spans.stream().map(span -> {
            var jaegerSpan = new Span();
            var jaegerTraceId = Utils.getJaegerTraceId(span.getTraceId());
            jaegerSpan.setTraceIdHigh(jaegerTraceId[0])
                    .setTraceIdLow(jaegerTraceId[1])
                    .setSpanId(Utils.getJaegerSpanId(span.getSpanId()))
                    .setParentSpanId(Utils.getJaegerSpanId(span.getParentSpanId()))
                    .setOperationName(span.getName())
                    .setStartTime(Utils.getStartTime(span))
                    .setDuration(Utils.getDuration(span))
                    .setTags(buildTags(span))
                    .setLogs(buildLogs(span))
                    .setReferences(buildReferences(span));
            return jaegerSpan;
        }).toList();
        batch.setSpans(jaegerSpans);
        batch.setProcess(buildProcess(firstSpan));
        return batch;
    }

    private List<Tag> buildTags(SpanData span) {
        List<Tag> tags = new ArrayList<>();
        var spanKindTag = Utils.getSpanKindTag(span);
        if (spanKindTag != null) {
            tags.add(spanKindTag);
        }

        StatusData status = span.getStatus();
        if (status != null) {
            var statusDescriptionTag = Utils.getStatusDescriptionTag(status);
            if (statusDescriptionTag != null) {
                tags.add(statusDescriptionTag);
            }

            var statusCodeTag = Utils.getStatusCodeTag(status);
            if (statusCodeTag != null) {
                tags.add(statusCodeTag);
            }

            if (status.getStatusCode() == StatusCode.ERROR) {
                tags.add(new Tag().setKey("error").setVType(TagType.BOOL).setVBool(true));
            }
        }

        tags.addAll(Utils.getTagsFromAttributes(span.getAttributes()));
        return tags;
    }

    private List<Log> buildLogs(SpanData span) {
        return span.getEvents().stream()
                .map(eventData ->
                        new Log().setTimestamp(TimeUnit.NANOSECONDS.toMicros(eventData.getEpochNanos()))
                                .setFields(Utils.getTagsFromAttributes(eventData.getAttributes()))
                )
                .toList();
    }

    private List<SpanRef> buildReferences(SpanData span) {
        return span.getLinks().stream()
                .map(linkData -> {
                    var jaegerTraceId = Utils.getJaegerTraceId(span.getTraceId());
                    return new SpanRef()
                            .setRefType(SpanRefType.FOLLOWS_FROM)
                            .setSpanId(Utils.getJaegerSpanId(linkData.getSpanContext().getSpanId()))
                            .setTraceIdHigh(jaegerTraceId[0])
                            .setTraceIdLow(jaegerTraceId[1]);
                })
                .toList();
    }

    private Process buildProcess(SpanData span) {
        var process = new Process();
        Attributes attributes = span.getResource().getAttributes();
        String serviceName = attributes.get(AttributeKey.stringKey("service.name"));
        process.setServiceName(serviceName == null ? "unknown" : serviceName);
        process.setTags(Utils.getTagsFromAttributes(attributes));
        return process;
    }
}

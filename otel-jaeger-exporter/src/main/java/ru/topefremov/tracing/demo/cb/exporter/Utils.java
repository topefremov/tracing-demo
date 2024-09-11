package ru.topefremov.tracing.demo.cb.exporter;

import com.google.common.primitives.Longs;
import io.jaegertracing.thriftjava.Tag;
import io.jaegertracing.thriftjava.TagType;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

class Utils {
    static long[] getJaegerTraceId(String otelTraceId) {
        byte[] decoded;
        try {
            decoded = Hex.decodeHex(otelTraceId);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }

        if (decoded.length != 16) {
            throw new IllegalArgumentException("TraceId must be 16 bytes long");
        }

        var high = Arrays.copyOfRange(decoded, 0, 8);
        var low = Arrays.copyOfRange(decoded, 8, decoded.length);
        return new long[]{Longs.fromByteArray(high), Longs.fromByteArray(low)};
    }

    static long getJaegerSpanId(String otelSpanId) {
        byte[] decoded;
        try {
            decoded = Hex.decodeHex(otelSpanId);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        if (decoded.length != 8) {
            throw new IllegalArgumentException("SpanId must be 8 bytes long");
        }
        return Longs.fromByteArray(decoded);
    }

    static Tag getSpanKindTag(SpanData span) {
        var spanKind = switch (span.getKind()) {
            case SpanKind.INTERNAL -> null;
            case SpanKind.SERVER -> "server";
            case SpanKind.CLIENT -> "client";
            case SpanKind.PRODUCER -> "producer";
            case SpanKind.CONSUMER -> "consumer";
        };
        Tag tag = null;
        if (spanKind != null) {
            tag = new Tag()
                    .setKey("span.kind")
                    .setVStr(spanKind)
                    .setVType(TagType.STRING);
        }
        return tag;
    }

    static List<Tag> getTagsFromAttributes(Attributes attributes) {
        return attributes.asMap().entrySet().stream().map(entry -> {
            var key = entry.getKey();
            var value = entry.getValue();
            var tag = new Tag().setKey(key.getKey());
            setValue(key.getType(), tag, value);
            return tag;
        }).toList();
    }

    static void setValue(AttributeType type, Tag tag, Object value) {
        switch (type) {
            case AttributeType.STRING: {
                tag.setVStr((String) value);
                tag.setVType(TagType.STRING);
                break;
            }
            case AttributeType.BOOLEAN: {
                tag.setVBool((Boolean) value);
                tag.setVType(TagType.BOOL);
                break;
            }
            case AttributeType.LONG: {
                tag.setVLong((Long) value);
                tag.setVType(TagType.LONG);
                break;
            }
            case AttributeType.DOUBLE: {
                tag.setVDouble((Double) value);
                tag.setVType(TagType.DOUBLE);
                break;
            }
            case AttributeType.STRING_ARRAY: {
                tag.setVStr(convertToJsonArr((String[]) value));
                tag.setVType(TagType.STRING);
                break;
            }
            case AttributeType.BOOLEAN_ARRAY: {
                tag.setVStr(convertToJsonArr((Boolean[]) value));
                tag.setVType(TagType.STRING);
                break;
            }
            case AttributeType.LONG_ARRAY: {
                tag.setVStr(convertToJsonArr((Long[]) value));
                tag.setVType(TagType.STRING);
                break;
            }
            case AttributeType.DOUBLE_ARRAY: {
                tag.setVStr(convertToJsonArr((Double[]) value));
                tag.setVType(TagType.STRING);
                break;
            }
        }
    }

    static String convertToJsonArr(Object[] arr) {
        var sb = new StringBuilder();
        sb.append("[");
        if (arr.length > 0) {
            var iterator = Arrays.stream(arr).iterator();
            sb.append("\"").append(iterator.next().toString()).append("\"");
            while (iterator.hasNext()) {
                sb.append(",").append("\"").append(iterator.next().toString()).append("\"");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    static long getStartTime(SpanData span) {
        return TimeUnit.NANOSECONDS.toMicros(span.getStartEpochNanos());
    }


    static long getDuration(SpanData span) {
        long startTimeMicros = getStartTime(span);
        long endTimeMicros = TimeUnit.NANOSECONDS.toMicros(span.getEndEpochNanos());
        return endTimeMicros - startTimeMicros;
    }

    static Tag getStatusDescriptionTag(StatusData status) {
        Tag tag = null;
        if (status.getDescription() != null && !status.getDescription().isBlank()) {
            tag = new Tag().setKey("otel.status_description").setVStr(status.getDescription()).setVType(TagType.STRING);
        }
        return tag;
    }

    static Tag getStatusCodeTag(StatusData status) {
        Tag tag = null;
        if (status.getStatusCode() == StatusCode.OK || status.getStatusCode() == StatusCode.ERROR) {
            tag = new Tag().setKey("otel.status_code").setVStr(status.getStatusCode().name()).setVType(TagType.STRING);
        }
        return tag;
    }
}

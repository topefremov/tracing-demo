package ru.topefremov.otel.starter.exporter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otel.exporter.jaeger")
public class JaegerExporterProperties {
    private String host = "localhost";
    private int port = 6831;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

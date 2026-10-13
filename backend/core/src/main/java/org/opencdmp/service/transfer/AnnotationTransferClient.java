package org.opencdmp.service.transfer;

import gr.cite.tools.exception.MyApplicationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Thin client for the annotation service transfer endpoints. The caller's bearer token and x-tenant
 * header are forwarded - the admin token already carries the annotation audience.
 */
@Component
public class AnnotationTransferClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private final TenantTransferProperties transferProperties;

    public AnnotationTransferClient(TenantTransferProperties transferProperties) {
        this.transferProperties = transferProperties;
    }

    public boolean isConfigured() {
        return this.baseUrl() != null;
    }

    public String baseUrl() {
        String url = this.transferProperties == null ? null : this.transferProperties.getAnnotationServiceUrl();
        if (url == null || url.isBlank()) return null;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public String post(String path, String jsonBody) throws IOException, InterruptedException {
        return this.post(path, jsonBody, null);
    }

    /**
     * tenantOverride replaces the forwarded x-tenant header - used by the same-installation copy,
     * where reading the source tenant's annotations needs a different scope than the caller's.
     */
    public String post(String path, String jsonBody, String tenantOverride) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl() + path))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            String authorization = servletAttributes.getRequest().getHeader("Authorization");
            if (authorization != null) builder.header("Authorization", authorization);
        }
        String tenant = tenantOverride;
        if (tenant == null && attributes instanceof ServletRequestAttributes servletAttributes) {
            tenant = servletAttributes.getRequest().getHeader("x-tenant");
        }
        if (tenant != null) builder.header("x-tenant", tenant);
        HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new MyApplicationException("Annotation service returned status " + response.statusCode() + " for " + path);
        return response.body();
    }
}

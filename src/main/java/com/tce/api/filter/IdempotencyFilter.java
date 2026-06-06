package com.tce.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.domain.model.IdempotencyRecord;
import com.tce.domain.repository.IdempotencyRecordRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class IdempotencyFilter implements Filter {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String idempotencyKey = httpRequest.getHeader(IDEMPOTENCY_HEADER);

        // Only apply to state-changing methods with the header present
        String method = httpRequest.getMethod();
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty() || 
                !("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method))) {
            chain.doFilter(request, response);
            return;
        }

        log.info("Processing request with Idempotency-Key: {}", idempotencyKey);

        // Wrap the request to cache the body for hashing and downstream reading
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
        String requestBody = cachedRequest.getBody();
        String requestHash = calculateHash(httpRequest.getRequestURI() + "|" + requestBody);

        // Check if an idempotency record already exists
        Optional<IdempotencyRecord> existingRecordOpt = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);

        if (existingRecordOpt.isPresent()) {
            IdempotencyRecord record = existingRecordOpt.get();
            if (record.getStatusCode() == -1) {
                // Another request with this key is currently in progress
                sendErrorResponse(httpResponse, HttpStatus.TOO_MANY_REQUESTS.value(), 
                        "An execution for this Idempotency-Key is already in progress.");
                return;
            }

            // Verify if the request hash matches the stored hash
            if (!record.getRequestHash().equals(requestHash)) {
                sendErrorResponse(httpResponse, HttpStatus.CONFLICT.value(), 
                        "Idempotency-Key conflict: Key is reused with a different request body.");
                return;
            }

            // Replay the cached response
            log.info("Replaying cached response for key: {}", idempotencyKey);
            httpResponse.setStatus(record.getStatusCode());
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(record.getResponseBody());
            httpResponse.flushBuffer();
            return;
        }

        // Lock the key by persisting an 'in-progress' placeholder (-1 statusCode)
        IdempotencyRecord placeholder = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .requestHash(requestHash)
                .responseHash("")
                .responseBody("")
                .statusCode(-1) // -1 signifies IN_PROGRESS
                .createdAt(Instant.now())
                .build();

        try {
            idempotencyRecordRepository.saveAndFlush(placeholder);
        } catch (DataIntegrityViolationException ex) {
            // Concurrent execution collision
            sendErrorResponse(httpResponse, HttpStatus.TOO_MANY_REQUESTS.value(), 
                    "An execution for this Idempotency-Key is already in progress.");
            return;
        }

        // Wrap response to capture output body
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        boolean success = false;
        try {
            chain.doFilter(cachedRequest, wrappedResponse);
            success = true;
        } finally {
            if (success) {
                byte[] responseContent = wrappedResponse.getContentAsByteArray();
                String responseBody = new String(responseContent, StandardCharsets.UTF_8);
                String responseHash = calculateHash(responseBody);
                int status = wrappedResponse.getStatus();

                // Update the placeholder with the actual result
                updateRecord(idempotencyKey, requestHash, responseHash, responseBody, status);

                // Copy captured content back to real response
                wrappedResponse.copyBodyToResponse();
            } else {
                // If execution failed with an unhandled exception, release the key lock
                try {
                    idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                            .ifPresent(idempotencyRecordRepository::delete);
                } catch (Exception e) {
                    log.error("Failed to delete idempotency placeholder on chain error", e);
                }
            }
        }
    }

    private void updateRecord(String key, String requestHash, String responseHash, String responseBody, int statusCode) {
        try {
            idempotencyRecordRepository.findByIdempotencyKey(key).ifPresent(record -> {
                record.setRequestHash(requestHash);
                record.setResponseHash(responseHash);
                record.setResponseBody(responseBody);
                record.setStatusCode(statusCode);
                idempotencyRecordRepository.saveAndFlush(record);
            });
        } catch (Exception e) {
            log.error("Failed to save final idempotency record for key: {}", key, e);
        }
    }

    private String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, String> errorDetails = Map.of("error", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
        response.flushBuffer();
    }

    /**
     * Helper wrapper class to cache HttpServletRequest body.
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = requestInputStream.readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
        }

        public String getBody() {
            return new String(this.cachedBody, StandardCharsets.UTF_8);
        }
    }
}

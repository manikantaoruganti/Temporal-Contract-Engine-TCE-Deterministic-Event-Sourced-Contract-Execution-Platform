package com.tce.api.filter;

import com.tce.BaseIntegrationTest;
import com.tce.api.dto.ContractCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IdempotencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testIdempotencyRequestLifecycle() {
        String idempotencyKey = UUID.randomUUID().toString();

        ContractCreateRequest requestBody = new ContractCreateRequest();
        requestBody.setName("Idempotent Contract");
        requestBody.setDescription("Testing idempotency validation");
        requestBody.setEffectiveDate(Instant.now().plusSeconds(86400)); // tomorrow

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);

        HttpEntity<ContractCreateRequest> entity = new HttpEntity<>(requestBody, headers);

        // 1. Initial request -> Should create contract and return 201 Created
        ResponseEntity<String> response1 = restTemplate.postForEntity("/api/v1/contracts", entity, String.class);
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        String body1 = response1.getBody();
        assertNotNull(body1);

        // 2. Second request with same key and body -> Should return cached response with 201 Created
        ResponseEntity<String> response2 = restTemplate.postForEntity("/api/v1/contracts", entity, String.class);
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertEquals(body1, response2.getBody());

        // 3. Third request with same key but different body -> Should return 409 Conflict
        ContractCreateRequest differentBody = new ContractCreateRequest();
        differentBody.setName("Tampered Name");
        differentBody.setEffectiveDate(Instant.now().plusSeconds(86400));
        HttpEntity<ContractCreateRequest> tamperedEntity = new HttpEntity<>(differentBody, headers);

        ResponseEntity<String> response3 = restTemplate.postForEntity("/api/v1/contracts", tamperedEntity, String.class);
        assertEquals(HttpStatus.CONFLICT, response3.getStatusCode());
        assertTrue(response3.getBody().contains("conflict"));
    }
}

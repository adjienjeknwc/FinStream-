package com.finstream.loan.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * TDD test suite validating the Circuit Breaker fallback rules in ExternalCreditBureauService.
 */
public class ExternalCreditBureauServiceTest {

    private RestTemplate restTemplate;
    private ExternalCreditBureauService bureauService;

    @BeforeEach
    public void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        bureauService = new ExternalCreditBureauService(restTemplate, "http://mock-bureau-api");
    }

    @Test
    @DisplayName("Should return bureau credit score when the API client is healthy")
    public void testFetchCreditScore_HealthyApi() {
        // Arrange
        String accountId = "acc-98112";
        int expectedScore = 750;
        when(restTemplate.getForObject(any(String.class), eq(Integer.class)))
                .thenReturn(expectedScore);

        // Act
        int score = bureauService.fetchCreditScore(accountId);

        // Assert
        assertEquals(expectedScore, score);
    }

    @Test
    @DisplayName("Should intercept connection/timeout failure and fallback to a conservative score")
    public void testFetchCreditScore_FallbackOnTimeout() {
        // Arrange
        String accountId = "acc-98112";
        // Simulate a timeout exception when calling the external api
        when(restTemplate.getForObject(any(String.class), eq(Integer.class)))
                .thenThrow(new RuntimeException("Connection timed out"));

        // Act
        int score = bureauService.fetchCreditScore(accountId);

        // Assert
        // The fallback method must yield the default conservative risk profile score of 600
        assertEquals(600, score);
    }
}

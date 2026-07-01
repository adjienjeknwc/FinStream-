package com.finstream.loan.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service class performing REST lookups against the external Credit Bureau API,
 * configured with Resilience4j circuit breakers to guarantee fallback stability.
 */
@Service
public class ExternalCreditBureauService {

    private static final Logger log = LoggerFactory.getLogger(ExternalCreditBureauService.class);

    private final RestTemplate restTemplate;
    private final String bureauServiceUrl;

    public ExternalCreditBureauService(RestTemplate restTemplate,
                                       @Value("${finstream.bureau-service.url:http://localhost:8085}") String bureauServiceUrl) {
        this.restTemplate = restTemplate;
        this.bureauServiceUrl = bureauServiceUrl;
    }

    /**
     * Queries the external Credit Bureau API for the customer's score.
     * Guarded by Resilience4j fallback annotations.
     *
     * @param accountId the account identifier to score
     * @return the resolved credit rating
     */
    @CircuitBreaker(name = "bureauService", fallbackMethod = "localFallbackScoring")
    public int fetchCreditScore(String accountId) {
        log.info("Sending Credit Bureau lookup request for Account ID: {}", accountId);
        String url = bureauServiceUrl + "/api/v1/bureau/score/" + accountId;
        
        Integer response = restTemplate.getForObject(url, Integer.class);
        if (response == null) {
            throw new RuntimeException("Null response returned from Bureau API client");
        }
        return response;
    }

    /**
     * Fallback method executed when the external Credit Bureau API times out or fails.
     * Defaults to a conservative threshold profile (600 score) to prevent approval leaks.
     *
     * @param accountId the account target
     * @param t         the exception context caught by the circuit breaker AOP aspect
     * @return the fallback credit rating
     */
    public int localFallbackScoring(String accountId, Throwable t) {
        log.warn("Resilience4j Alert: Fallback triggered for Account ID: {} due to: {}", accountId, t.getMessage());
        // Return conservative score that defaults applications to manual review or rejection
        return 600;
    }
}

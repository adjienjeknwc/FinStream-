package com.finstream.loan.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreditAssessmentWorker {

    private static final Logger log = LoggerFactory.getLogger(CreditAssessmentWorker.class);

    @JobWorker(type = "check-credit-score", autoComplete = false)
    public void checkCreditScore(final JobClient client, final ActivatedJob job) {
        log.info("Executing credit assessment worker for job key: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String accountId = (String) variables.get("accountId");
        Object rawAmount = variables.get("requestedAmount");

        double requestedAmount = 0.0;
        if (rawAmount instanceof Number) {
            requestedAmount = ((Number) rawAmount).doubleValue();
        } else if (rawAmount instanceof String) {
            try {
                requestedAmount = Double.parseDouble((String) rawAmount);
            } catch (NumberFormatException e) {
                log.error("Failed to parse requestedAmount: {}", rawAmount, e);
            }
        }

        log.info("Parsing variables - accountId: {}, requestedAmount: {}", accountId, requestedAmount);

        // Perform a mock credit score assessment: 720 if amount < 50000 else 640
        int creditScore = (requestedAmount < 50000.0) ? 720 : 640;
        boolean eligible = creditScore >= 700;

        Map<String, Object> outputVariables = Map.of(
            "creditScore", creditScore,
            "eligible", eligible
        );

        log.info("Completing job {} with creditScore: {}, eligible: {}", job.getKey(), creditScore, eligible);

        client.newCompleteCommand(job.getKey())
            .variables(outputVariables)
            .send()
            .join();
    }
}

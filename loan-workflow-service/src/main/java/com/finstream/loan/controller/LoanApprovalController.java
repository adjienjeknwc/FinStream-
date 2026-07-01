package com.finstream.loan.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanApprovalController {

    private static final Logger log = LoggerFactory.getLogger(LoanApprovalController.class);
    private final ZeebeClient zeebeClient;

    public LoanApprovalController(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> applyForLoan(@Valid @RequestBody LoanApplicationRequest request) {
        log.info("Received loan application for account: {}, amount: {}", request.accountId(), request.requestedAmount());

        Map<String, Object> variables = Map.of(
            "accountId", request.accountId(),
            "requestedAmount", request.requestedAmount()
        );

        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
            .bpmnProcessId("loan-evaluation-process")
            .latestVersion()
            .variables(variables)
            .send()
            .join();

        log.info("Started process instance key: {} for process: {}", event.getProcessInstanceKey(), event.getBpmnProcessId());

        return ResponseEntity.ok(new LoanApplicationResponse(
            event.getProcessInstanceKey(),
            event.getBpmnProcessId(),
            "Loan application received, evaluation workflow started."
        ));
    }

    public record LoanApplicationRequest(
        @NotBlank(message = "Account ID is required")
        String accountId,

        @NotNull(message = "Requested amount is required")
        @Positive(message = "Requested amount must be positive")
        Double requestedAmount
    ) {}

    public record LoanApplicationResponse(
        long processInstanceKey,
        String bpmnProcessId,
        String message
    ) {}
}

package com.finstream.loan.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep2;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.ZeebeFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanApprovalControllerTest {

    private ZeebeClient zeebeClient;
    private LoanApprovalController controller;

    @BeforeEach
    void setUp() {
        zeebeClient = mock(ZeebeClient.class);
        controller = new LoanApprovalController(zeebeClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testApplyForLoan_Success() {
        LoanApprovalController.LoanApplicationRequest request =
                new LoanApprovalController.LoanApplicationRequest("acc-123", 25000.0);

        CreateProcessInstanceCommandStep1 step1 = mock(CreateProcessInstanceCommandStep1.class);
        CreateProcessInstanceCommandStep2 step2 = mock(CreateProcessInstanceCommandStep2.class);
        CreateProcessInstanceCommandStep3 step3 = mock(CreateProcessInstanceCommandStep3.class);
        ZeebeFuture<ProcessInstanceEvent> future = mock(ZeebeFuture.class);
        ProcessInstanceEvent event = mock(ProcessInstanceEvent.class);

        when(zeebeClient.newCreateInstanceCommand()).thenReturn(step1);
        when(step1.bpmnProcessId("loan-evaluation-process")).thenReturn(step2);
        when(step2.latestVersion()).thenReturn(step3);
        when(step3.variables(any(Map.class))).thenReturn(step3);
        when(step3.send()).thenReturn(future);
        when(future.join()).thenReturn(event);

        when(event.getProcessInstanceKey()).thenReturn(987654321L);
        when(event.getBpmnProcessId()).thenReturn("loan-evaluation-process");

        ResponseEntity<LoanApprovalController.LoanApplicationResponse> response = controller.applyForLoan(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(987654321L, response.getBody().processInstanceKey());
        assertEquals("loan-evaluation-process", response.getBody().bpmnProcessId());

        verify(zeebeClient).newCreateInstanceCommand();
        verify(step1).bpmnProcessId("loan-evaluation-process");
        verify(step2).latestVersion();
        verify(step3).variables(Map.of("accountId", "acc-123", "requestedAmount", 25000.0));
    }
}

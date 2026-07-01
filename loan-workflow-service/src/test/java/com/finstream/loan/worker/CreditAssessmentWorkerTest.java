package com.finstream.loan.worker;

import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreditAssessmentWorkerTest {

    private CreditAssessmentWorker worker;
    private JobClient jobClient;
    private ActivatedJob activatedJob;
    private CompleteJobCommandStep1 completeCommand;
    private ZeebeFuture<Void> future;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        worker = new CreditAssessmentWorker();
        jobClient = mock(JobClient.class);
        activatedJob = mock(ActivatedJob.class);
        completeCommand = mock(CompleteJobCommandStep1.class);
        future = mock(ZeebeFuture.class);

        when(activatedJob.getKey()).thenReturn(1001L);
        when(jobClient.newCompleteCommand(1001L)).thenReturn(completeCommand);
        when(completeCommand.variables(any(Map.class))).thenReturn(completeCommand);
        when(completeCommand.send()).thenReturn(future);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCheckCreditScore_LowAmount_Eligible() {
        Map<String, Object> inputVariables = Map.of(
            "accountId", "acc-789",
            "requestedAmount", 45000.0
        );
        when(activatedJob.getVariablesAsMap()).thenReturn(inputVariables);

        worker.checkCreditScore(jobClient, activatedJob);

        ArgumentCaptor<Map<String, Object>> outputVariablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(completeCommand).variables(outputVariablesCaptor.capture());
        verify(completeCommand).send();
        verify(future).join();

        Map<String, Object> output = outputVariablesCaptor.getValue();
        assertEquals(720, output.get("creditScore"));
        assertEquals(true, output.get("eligible"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCheckCreditScore_HighAmount_NotEligible() {
        Map<String, Object> inputVariables = Map.of(
            "accountId", "acc-789",
            "requestedAmount", 50000.0
        );
        when(activatedJob.getVariablesAsMap()).thenReturn(inputVariables);

        worker.checkCreditScore(jobClient, activatedJob);

        ArgumentCaptor<Map<String, Object>> outputVariablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(completeCommand).variables(outputVariablesCaptor.capture());
        verify(completeCommand).send();
        verify(future).join();

        Map<String, Object> output = outputVariablesCaptor.getValue();
        assertEquals(640, output.get("creditScore"));
        assertEquals(false, output.get("eligible"));
    }
}

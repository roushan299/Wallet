package com.example.wallet.service;

import com.example.wallet.entity.Transaction;
import com.example.wallet.service.saga.SagaContext;
import com.example.wallet.service.saga.SagaOrchestrator;
import com.example.wallet.service.saga.steps.SagaStepFactory.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;
import static com.example.wallet.service.saga.steps.SagaStepFactory.TransferMoneySagaSteps;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferSagaService {

    private final TransactionService transactionService;
    private final SagaOrchestrator sagaOrchestrator;

    @Transactional
    public Long initiateTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String description) {
        log.info("Initiating transfer for wallet {} to wallet {} with amount {} and description", fromWalletId, toWalletId, amount, description);
        Transaction transaction = transactionService.createTransaction(fromWalletId, toWalletId, amount, description);
        SagaContext sagaContext = SagaContext.builder()
                .data(Map.ofEntries(
                        Map.entry("fromWalletId", fromWalletId),
                        Map.entry("transactionId", transaction.getId()),
                        Map.entry("toWalletId", toWalletId),
                        Map.entry("amount", amount),
                        Map.entry("description", description)
                ))
                .build();
        Long sagaInstanceId = sagaOrchestrator.startSaga(sagaContext);
        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(), sagaInstanceId);
        executeTransferSaga(sagaInstanceId);
        return sagaInstanceId;
    }

    public void executeTransferSaga(Long sagaInstanceId){
        log.info("Executing transfer saga for sagaInstanceId {}", sagaInstanceId);

        try {

            for(SagaStepType step: TransferMoneySagaSteps){
                boolean success = sagaOrchestrator.executeStep(sagaInstanceId, step.toString());
                if(!success){
                    log.error("Failed to execute step {}", step.toString());
                    sagaOrchestrator.failSaga(sagaInstanceId);
                    return;
                }
                sagaOrchestrator.compensateSaga(sagaInstanceId);
                log.info("Transfer saga completed with id {}", sagaInstanceId);
            }

        }catch (Exception e){
            log.error("Failed to execute transfer saga with id {}", sagaInstanceId, e);
            sagaOrchestrator.failSaga(sagaInstanceId);
        }
    }


}

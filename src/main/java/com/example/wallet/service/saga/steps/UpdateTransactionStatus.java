package com.example.wallet.service.saga.steps;

import com.example.wallet.entity.Transaction;
import com.example.wallet.enums.TransactionStatus;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.service.saga.SagaContext;
import com.example.wallet.service.saga.SagaStepInterface;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatus implements SagaStepInterface {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean execute(SagaContext context) {
        Long transactionId = context.getLong("transactionId");
        log.info("Updating transaction status for transaction {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));

        context.put("originalTransactionStatus", transaction.getStatus());

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transaction status updated for transaction {}", transactionId);

        context.put("transactionStatusAfterUpdate", transaction.getStatus());

        log.info("Update transaction status step executed successfully");
        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        Long transactionId = context.getLong("transactionId");
        TransactionStatus originalTransactionStatus = TransactionStatus.valueOf(context.getString("originalTransactionStatus"));
        log.info("Compensating transaction status for transaction {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setStatus(originalTransactionStatus);
        transactionRepository.save(transaction);
        log.info("Transaction status compensated for transaction {}", transactionId);
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString();
    }
}

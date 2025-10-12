package com.example.wallet.config;

import com.example.wallet.service.saga.SagaStepInterface;
import com.example.wallet.service.saga.steps.CreditDestinationStepInterface;
import com.example.wallet.service.saga.steps.DebitSourceWalletStepInterface;
import com.example.wallet.service.saga.steps.SagaStepFactory;
import com.example.wallet.service.saga.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaConfig {


    @Bean
    public Map<String, SagaStepInterface> sagaStepMap(DebitSourceWalletStepInterface debitSourceWalletStep,
                                                      CreditDestinationStepInterface creditDestinationStep,
                                                      UpdateTransactionStatus updateTransactionStatus) {
        Map<String, SagaStepInterface> sagaStepMap = new HashMap<>();
        sagaStepMap.put(SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(), creditDestinationStep);
        sagaStepMap.put(SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(), updateTransactionStatus);
        sagaStepMap.put(SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(), debitSourceWalletStep);
        return sagaStepMap;
    }

}

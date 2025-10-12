package com.example.wallet.service.saga.steps;

import com.example.wallet.entity.Wallet;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.saga.SagaContext;
import com.example.wallet.service.saga.SagaStepInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationStepInterface implements SagaStepInterface {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context){
        // step 1: Get the destination wallet id form the context
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");
        log.info("Crediting destination wallet: {}, with amount: {}", toWalletId, amount);

        //step 2: Fetch the destination wallet from database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("originalToWalletBalance", wallet.getBalance());

        //step 3: credit the destination wallet
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet credited with balance {}", wallet.getBalance());

        //step 4: Update the context with the changes
        context.put("originalToWalletBalance", wallet.getBalance());
        log.info("Credit destination wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context){
        // step 1: Get the destination wallet id form the context
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");
        log.info("Compensation credit of destination wallet: {}, with amount: {}", toWalletId, amount);

        //step 2: Fetch the destination wallet from database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        log.info("Wallet fetched with balance {}", wallet.getBalance());

        //step 3: credit the destination wallet
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Wallet debited with balance {}", wallet.getBalance());

        //step 4: Update the context with the changes
        context.put("originalToWalletBalance", wallet.getBalance());
        log.info("Credit compensation destination wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName(){
        return SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }


}

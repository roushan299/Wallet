package com.example.wallet.service;

import com.example.wallet.entity.Wallet;
import com.example.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    public final WalletRepository walletRepository;

    public Wallet createWallet(Long userId) {
        log.info("Creating wallet for user {}", userId);
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .isActive(true)
                .balance(BigDecimal.ZERO)
                .build();
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created for user {}", userId);
        return savedWallet;
    }

    public Wallet getWalletById(Long walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("No wallet found with id "+ walletId));
    }

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("No wallet is associated to user "+ userId));
    }

    @Transactional
    public Wallet debit(Long userId, BigDecimal amount) {
        log.info("Debiting {} from wallet {}", amount, userId);
        Wallet wallet = getWalletByUserId(userId);
        walletRepository.updateBalanceByUserId(userId, wallet.getBalance().subtract(amount));
        wallet = getWalletByUserId(userId);
        log.info("Wallet debited {} with amount {}", wallet.getId(), amount);
        return wallet;
    }

    @Transactional
    public Wallet credit(Long userId, BigDecimal amount) {
        log.info("Crediting {} to wallet {}", amount, userId);
        Wallet wallet = getWalletByUserId(userId);
        walletRepository.updateBalanceByUserId(userId, wallet.getBalance().add(amount));
        wallet = getWalletByUserId(userId);
        log.info("Wallet credited {} with amount {}", wallet.getId(), amount);
        return wallet;
    }

    public BigDecimal getBalance(Long walletId) {
        log.info("Getting balance for wallet {}", walletId);
        Wallet wallet = getWalletById(walletId);
        BigDecimal balance = wallet.getBalance();
        log.info("Balance for wallet {} is {}", wallet, balance);
        return balance;
    }

}

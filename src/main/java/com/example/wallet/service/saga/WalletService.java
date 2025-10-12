package com.example.wallet.service.saga;


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
    public void debit(Long walletId, BigDecimal amount) {
        log.info("Debiting wallet {} with amount {}", walletId, amount);
        Wallet wallet = getWalletById(walletId);
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Wallet debited {} with amount {}", walletId, amount);
    }

    @Transactional
    public void credit(Long walletId, BigDecimal amount) {
        log.info("Crediting wallet {} with amount {}", walletId, amount);
        Wallet wallet = getWalletById(walletId);
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet credited {} with amount {}", walletId, amount);
    }

    public BigDecimal getBalance(Long walletId) {
        log.info("Getting balance for wallet {}", walletId);
        Wallet wallet = getWalletById(walletId);
        BigDecimal balance = wallet.getBalance();
        log.info("Balance for wallet {} is {}", wallet, balance);
        return balance;
    }

}

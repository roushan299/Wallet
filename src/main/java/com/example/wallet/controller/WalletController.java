package com.example.wallet.controller;

import com.example.wallet.dto.CreateWalletRequestDTO;
import com.example.wallet.dto.CreditWalletRequestDTO;
import com.example.wallet.dto.DebitWalletRequestDTO;
import com.example.wallet.entity.Wallet;
import com.example.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/wallet")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> addWallet(@RequestBody CreateWalletRequestDTO request) {

        try {
            Wallet wallet = walletService.createWallet(request.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
        }catch (Exception e) {
            log.error("Error creating wallet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet.getBalance());
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<Wallet> debit(@PathVariable Long userId, @RequestBody DebitWalletRequestDTO request) {
        Wallet wallet = walletService.debit(userId, request.getAmount());
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<Wallet> credit(@PathVariable Long userId, @RequestBody CreditWalletRequestDTO request) {
        Wallet wallet = walletService.credit(userId, request.getAmount());
        return ResponseEntity.ok(wallet);
    }

}

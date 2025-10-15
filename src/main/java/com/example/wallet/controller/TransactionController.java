package com.example.wallet.controller;

import com.example.wallet.dto.TransactionRequestDTO;
import com.example.wallet.dto.TransferResponseDTO;
import com.example.wallet.service.TransferSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/transaction")
public class TransactionController {

    private final TransferSagaService transferSagaService;

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createTransaction(@RequestBody TransactionRequestDTO request) {

        try {
            Long sagaInstanceId = transferSagaService.initiateTransfer(request.getFromWalletId(), request.getToWalletId(), request.getAmount(), request.getDescription());

            TransferResponseDTO transferResponseDTO = TransferResponseDTO.builder()
                    .sagaInstanceId(sagaInstanceId)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(transferResponseDTO);

        } catch (Exception e) {
            log.info("Error creating transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

}

package com.example.wallet.repository;

import com.example.wallet.entity.Transaction;
import com.example.wallet.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromWalletId(Long walletId); // all the debit transaction

    List<Transaction> findByToWalletId(Long walletId); // all the credit transaction

    @Query("Select t from Transaction t where t.fromWalletId = :walletId or t.toWalletId = :walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId); // all the transaction of a wallet

    List<Transaction> finByStatus(TransactionStatus status);

    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);

}

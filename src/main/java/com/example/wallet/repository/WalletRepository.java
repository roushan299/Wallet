package com.example.wallet.repository;

import com.example.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("Select w from Wallet w where w.userId = :userId")
    Optional<Wallet> findByIdWithLock(@Param("userId") Long userId);

    @Modifying
    @Query("Update Wallet w Set w.balance = :balance Where w.userId = :userId")
    void updateBalanceByUserId(@Param("userId")Long userId, @Param("balance") BigDecimal balance);

}

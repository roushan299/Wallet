package com.example.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "user_id", nullable = false)
    private  Long userId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;


    public boolean hasSufficientBalance(BigDecimal balance) {
        return this.balance.compareTo(balance) >= 0;
    }

    public void debit(BigDecimal amount) {
        if(!hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Not enough balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }


}

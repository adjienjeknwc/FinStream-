package com.finstream.account.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Entity mapping banking account records to the 'accounts' database schema.
 */
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_kyc", columnList = "kyc_status"),
    @Index(name = "idx_account_balance", columnList = "balance")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Account holder name is required")
    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email formatting")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotNull(message = "KYC status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private KycStatus kycStatus;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Column(name = "balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

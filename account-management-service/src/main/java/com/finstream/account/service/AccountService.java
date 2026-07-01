package com.finstream.account.service;

import com.finstream.account.domain.Account;
import com.finstream.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service class exposing operations against the Account repository.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsExceedingBalance(BigDecimal threshold) {
        return accountRepository.findAccountsExceedingBalance(threshold);
    }
}

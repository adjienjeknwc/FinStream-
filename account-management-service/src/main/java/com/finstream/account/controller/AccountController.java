package com.finstream.account.controller;

import com.finstream.account.domain.Account;
import com.finstream.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing FinStream banking accounts.
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Validated @RequestBody Account account) {
        Account created = accountService.createAccount(account);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable UUID id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Account>> searchAccountsExceedingBalance(@RequestParam BigDecimal threshold) {
        List<Account> accounts = accountService.getAccountsExceedingBalance(threshold);
        return ResponseEntity.ok(accounts);
    }
}

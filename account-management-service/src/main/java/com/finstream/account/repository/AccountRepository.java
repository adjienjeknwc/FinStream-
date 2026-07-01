package com.finstream.account.repository;

import com.finstream.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository management component exposing operations against the Account entity.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Hand-optimized native SQL query utilizing indexed columns to filter accounts exceeding 
     * a specific balance threshold.
     */
    @Query(value = "SELECT * FROM accounts a WHERE a.balance > :balanceThreshold ORDER BY a.balance DESC", nativeQuery = true)
    List<Account> findAccountsExceedingBalance(@Param("balanceThreshold") BigDecimal balanceThreshold);
}

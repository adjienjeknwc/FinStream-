package com.finstream.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap loader for the FinStream Account Management Microservice.
 */
@SpringBootApplication
public class AccountManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountManagementApplication.class, args);
    }
}

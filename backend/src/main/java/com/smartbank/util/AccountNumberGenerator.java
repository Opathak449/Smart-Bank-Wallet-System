package com.smartbank.util;

import com.smartbank.repository.WalletRepository;
import java.util.concurrent.ThreadLocalRandom;

public class AccountNumberGenerator {
    public static String generate(WalletRepository repo) {
        String accountNumber;
        do {
            accountNumber = "10" + String.format("%014d", ThreadLocalRandom.current().nextLong(0, 99999999999999L));
        } while (repo.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}

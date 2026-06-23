package com.smartbank.config;

import com.smartbank.entity.Role;
import com.smartbank.entity.User;
import com.smartbank.entity.Wallet;
import com.smartbank.repository.RoleRepository;
import com.smartbank.repository.UserRepository;
import com.smartbank.repository.WalletRepository;
import com.smartbank.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@SuppressWarnings("null")
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureRole("ROLE_USER", "Standard user role");
        ensureRole("ROLE_ADMIN", "Admin role with access to admin panel");
        Role superAdminRole = ensureRole("ROLE_SUPER_ADMIN", "Super admin role with access to all pages");

        if (!userRepository.existsByUsername("superadmin")) {
            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

            User superAdmin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .username("superadmin")
                    .email("superadmin@smartbank.com")
                    .mobileNumber("9999999999")
                    .address("SmartBank HQ")
                    .password(passwordEncoder.encode("SuperAdmin@123"))
                    .status(User.UserStatus.ACTIVE)
                    .emailVerified(true)
                    .roles(Set.of(userRole, adminRole, superAdminRole))
                    .build();

            superAdmin = userRepository.save(superAdmin);

            String accountNumber = AccountNumberGenerator.generate(walletRepository);
            Wallet wallet = Wallet.builder()
                    .accountNumber(accountNumber)
                    .user(superAdmin)
                    .balance(BigDecimal.ZERO)
                    .status(Wallet.WalletStatus.ACTIVE)
                    .build();
            walletRepository.save(wallet);

            log.info("Super admin created — username: superadmin / password: SuperAdmin@123");
        }
    }

    private Role ensureRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = Role.builder().name(name).description(description).build();
            return roleRepository.save(role);
        });
    }
}

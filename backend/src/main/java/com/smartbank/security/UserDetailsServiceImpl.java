// ============================================================
// FILE: security/UserDetailsServiceImpl.java
// ============================================================
package com.smartbank.security;

import com.smartbank.entity.User;
import com.smartbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == User.UserStatus.ACTIVE,
                true, true, true,
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .collect(Collectors.toList())
        );
    }
}

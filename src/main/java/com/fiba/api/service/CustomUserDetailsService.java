package com.fiba.api.service;

import com.fiba.api.exception.ResourceNotFoundException;
import com.fiba.api.model.User;
import com.fiba.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + email + " не найден"));

            // Создаем роль на основе роли пользователя с префиксом "ROLE_"
            String roleWithPrefix = "ROLE_" + user.getRole().toUpperCase();
            
            boolean accountNonExpired = user.isEmailVerified(); // аккаунт не истек, если email подтвержден
            
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix)))
                    .accountExpired(!accountNonExpired) // инверсия, так как метод принимает "expired" (истек), а не "non-expired"
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        } catch (UsernameNotFoundException e) {
            throw e; // Пробрасываем оригинальное исключение для корректной обработки
        } catch (Exception e) {
            // Оборачиваем другие исключения для предоставления более понятной ошибки
            throw new UsernameNotFoundException("Ошибка при загрузке пользователя: " + e.getMessage(), e);
        }
    }
    
    /**
     * Получить пользователя по ID с проверкой его существования
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "id", id));
    }
} 
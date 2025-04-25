package com.fiba.api.service;

import com.fiba.api.dto.RegisterRequest;
import com.fiba.api.exception.BadRequestException;
import com.fiba.api.exception.ResourceNotFoundException;
import com.fiba.api.model.Profile;
import com.fiba.api.model.User;
import com.fiba.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;

    /**
     * Регистрация нового пользователя
     * @throws BadRequestException если email уже зарегистрирован или данные некорректны
     */
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new BadRequestException("Данные для регистрации не могут быть пустыми");
        }

        String email = registerRequest.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email не может быть пустым");
        }

        String name = registerRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Имя не может быть пустым");
        }

        String password = registerRequest.getPassword();
        if (password == null || password.length() < 6) {
            throw new BadRequestException("Пароль должен содержать минимум 6 символов");
        }

        // Проверка, существует ли пользователь с таким email
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new BadRequestException("Email уже зарегистрирован");
        }

        try {
            // Создание нового пользователя
            User user = User.builder()
                    .name(name.trim())
                    .email(email.trim().toLowerCase())
                    .password(passwordEncoder.encode(password))
                    .emailVerified(true) // Для демо считаем все email подтвержденными
                    .role(registerRequest.getRole().toUpperCase())
                    .build();

            User savedUser = userRepository.save(user);
            log.info("Создан новый пользователь с ID: {}", savedUser.getId());

            // Создание профиля пользователя
            Profile profile = Profile.builder()
                    .user(savedUser)
                    .build();
            Profile savedProfile = profileService.saveProfile(profile);
            log.info("Создан профиль для пользователя с ID: {}", savedUser.getId());

            return savedUser;
        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя: {}", e.getMessage(), e);
            throw new BadRequestException("Ошибка при регистрации пользователя: " + e.getMessage());
        }
    }

    /**
     * Проверяет, существует ли пользователь с указанным email
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    /**
     * Обновляет данные пользователя
     * @throws BadRequestException если данные некорректны
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Transactional
    public User updateUser(User user) {
        if (user == null) {
            throw new BadRequestException("Данные пользователя не могут быть пустыми");
        }

        if (user.getId() == null) {
            throw new BadRequestException("ID пользователя не может быть null");
        }

        // Проверяем существование пользователя
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "id", user.getId()));

        // Проверяем email на уникальность, если он изменился
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email уже используется другим пользователем");
        }

        try {
            User savedUser = userRepository.save(user);
            log.info("Обновлен пользователь с ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            log.error("Ошибка при обновлении пользователя: {}", e.getMessage(), e);
            throw new BadRequestException("Ошибка при обновлении пользователя: " + e.getMessage());
        }
    }

    /**
     * Получает пользователя по email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email не может быть пустым");
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "email", email));
    }

    /**
     * Получает пользователя по ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("ID пользователя не может быть null");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "id", id));
    }

    /**
     * Получает список всех пользователей
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Удаляет пользователя по ID
     */
    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new BadRequestException("ID пользователя не может быть null");
        }
        
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь", "id", id);
        }

        try {
            userRepository.deleteById(id);
            log.info("Удален пользователь с ID: {}", id);
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя: {}", e.getMessage(), e);
            throw new BadRequestException("Ошибка при удалении пользователя: " + e.getMessage());
        }
    }

    /**
     * Поиск пользователей по имени или email
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.searchByNameOrEmail(query.trim().toLowerCase());
    }

    /**
     * Получить пользователей по списку ID
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findByIdIn(ids);
    }
} 
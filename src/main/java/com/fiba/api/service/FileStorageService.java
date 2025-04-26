package com.fiba.api.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

@Service
@Slf4j
public class FileStorageService {

    private Path rootLocation;
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/bmp",
        "image/webp"
    };
    
    /**
     * Инициализирует сервис хранения файлов, создавая необходимые директории
     * Вызывается автоматически при запуске приложения
     */
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing FileStorageService with upload directory: {}", uploadDir);
            
            rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            log.info("Absolute path for upload directory: {}", rootLocation);
            
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                log.info("Created root upload directory: {}", rootLocation);
            }
            
            // Определяем структуру папок и их назначение
            Map<String, String> subdirs = new HashMap<>();
            subdirs.put("profiles", "Фотографии профилей пользователей");
            subdirs.put("avatars", "Маленькие аватарки пользователей");
            subdirs.put("tournaments", "Изображения турниров");
            subdirs.put("sponsors", "Логотипы спонсоров");
            subdirs.put("ads", "Рекламные изображения");
            subdirs.put("teams", "Логотипы команд");
            subdirs.put("temp", "Временные файлы");
            
            // Создаем все необходимые поддиректории
            for (Map.Entry<String, String> entry : subdirs.entrySet()) {
                Path subdirPath = rootLocation.resolve(entry.getKey());
                if (!Files.exists(subdirPath)) {
                    Files.createDirectories(subdirPath);
                    log.info("Created subdirectory for {}: {}", entry.getValue(), subdirPath);
                }
                
                // Создаем .gitkeep для сохранения пустых директорий в git
                Path gitkeepFile = subdirPath.resolve(".gitkeep");
                if (!Files.exists(gitkeepFile)) {
                    Files.createFile(gitkeepFile);
                }
            }
            
            // Проверяем права доступа
            checkAndSetPermissions(rootLocation);
            
            log.info("FileStorageService initialized successfully");
        } catch (IOException e) {
            String msg = "Could not initialize storage location: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
    
    /**
     * Проверяет и устанавливает правильные права доступа для директорий
     */
    private void checkAndSetPermissions(Path directory) throws IOException {
        // Проверяем базовые разрешения
        if (!Files.isReadable(directory)) {
            log.warn("Directory is not readable: {}", directory);
        }
        if (!Files.isWritable(directory)) {
            log.warn("Directory is not writable: {}", directory);
        }
        
        // Проверяем все поддиректории
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    checkAndSetPermissions(path);
                }
            }
        }
    }
    
    /**
     * Очищает временные файлы старше определенного возраста
     */
    @Scheduled(cron = "0 0 3 * * *") // Запускается каждый день в 3 утра
    public void cleanupTempFiles() {
        try {
            Path tempDir = rootLocation.resolve("temp");
            if (!Files.exists(tempDir)) {
                return;
            }
            
            // Удаляем файлы старше 24 часов
            Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path))
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toInstant()
                            .isBefore(Instant.now().minus(24, ChronoUnit.HOURS));
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.info("Deleted old temp file: {}", path);
                    } catch (IOException e) {
                        log.error("Error deleting temp file: {}", path, e);
                    }
                });
        } catch (IOException e) {
            log.error("Error during temp files cleanup", e);
        }
    }
    
    /**
     * Проверяет, является ли файл допустимым изображением
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        // Проверка размера файла
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Размер файла превышает максимально допустимый (5MB)");
        }

        // Проверка типа файла
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("Недопустимый тип файла. Разрешены только изображения (JPEG, PNG, GIF, BMP, WEBP)");
        }

        // Проверка имени файла
        String filename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (filename.contains("..")) {
            throw new IllegalArgumentException("Имя файла содержит недопустимый путь");
        }
    }

    /**
     * Проверяет, является ли тип файла разрешенным изображением
     */
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Сохраняет загруженный файл в указанной поддиректории
     *
     * @param file Загруженный файл
     * @param subdirectory Поддиректория для сохранения (например, "sponsors", "tournaments")
     * @return Путь к сохраненному файлу относительно корня приложения
     * @throws IOException Если произошла ошибка при сохранении файла
     */
    public String storeFile(MultipartFile file, String subdirectory) throws IOException {
        validateImageFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        log.debug("Processing file upload: name={}, size={}, type={}, subdirectory={}",
                 originalFilename, file.getSize(), file.getContentType(), subdirectory);

        // Получаем расширение файла
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // Генерируем уникальное имя файла
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Создаем путь для сохранения файла
        Path targetLocation = rootLocation.resolve(subdirectory).resolve(newFilename);
        log.debug("Saving file to: {}", targetLocation);
        
        // Сохраняем файл
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("Successfully stored file: {}", targetLocation);

        // Возвращаем путь относительно корня приложения
        return "/uploads/" + subdirectory + "/" + newFilename;
    }
    
    /**
     * Сохраняет загруженный файл в директории для изображений турниров
     */
    public String storeTournamentImage(MultipartFile file) throws IOException {
        return storeFile(file, "tournaments");
    }
    
    /**
     * Сохраняет загруженный файл в директории для логотипов спонсоров
     */
    public String storeSponsorLogo(MultipartFile file) throws IOException {
        return storeFile(file, "sponsors");
    }
    
    /**
     * Сохраняет загруженный файл в директории для изображений рекламы
     */
    public String storeAdImage(MultipartFile file) throws IOException {
        return storeFile(file, "ads");
    }
    
    /**
     * Сохраняет загруженный файл как фотографию профиля
     */
    public String storeProfilePhoto(MultipartFile file) throws IOException {
        return storeFile(file, "profiles");
    }

    /**
     * Сохраняет загруженный файл как аватар пользователя
     */
    public String storeAvatar(MultipartFile file) throws IOException {
        return storeFile(file, "avatars");
    }

    /**
     * Загружает файл как ресурс
     *
     * @param filePath путь к файлу
     * @return Resource объект файла
     * @throws IOException если произошла ошибка при загрузке файла
     */
    public Resource loadFileAsResource(Path filePath) throws IOException {
        try {
            Resource resource = new org.springframework.core.io.FileSystemResource(filePath.toFile());
            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("Файл не найден: " + filePath);
            }
        } catch (Exception e) {
            throw new IOException("Ошибка при загрузке файла: " + filePath, e);
        }
    }
}
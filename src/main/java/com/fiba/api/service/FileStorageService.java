package com.fiba.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

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
            
            String[] subdirs = {"tournaments", "sponsors", "ads", "profiles", "avatars"};
            for (String subdir : subdirs) {
                Path subdirPath = rootLocation.resolve(subdir);
                if (!Files.exists(subdirPath)) {
                    Files.createDirectories(subdirPath);
                    log.info("Created subdirectory: {}", subdirPath);
                }
            }
            
            log.info("FileStorageService initialized successfully");
        } catch (IOException e) {
            String msg = "Could not initialize storage location: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
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
}
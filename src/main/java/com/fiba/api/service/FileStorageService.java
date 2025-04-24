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
     * Сохраняет загруженный файл в указанной поддиректории
     *
     * @param file Загруженный файл
     * @param subdirectory Поддиректория для сохранения (например, "sponsors", "tournaments")
     * @return Путь к сохраненному файлу относительно корня приложения
     * @throws IOException Если произошла ошибка при сохранении файла
     */
    private String storeFile(MultipartFile file, String subdirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            log.error("Failed to store empty file in {}", subdirectory);
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        log.debug("Processing file upload: name={}, size={}, type={}, subdirectory={}",
                 originalFilename, file.getSize(), file.getContentType(), subdirectory);

        if (originalFilename.contains("..")) {
            String msg = "Cannot store file with relative path outside current directory: " + originalFilename;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        Path targetLocation = rootLocation.resolve(subdirectory).resolve(newFilename);
        log.debug("Saving file to: {}", targetLocation);
        
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("Successfully stored file: {}", targetLocation);

        // Возвращаем путь относительно корня приложения
        return subdirectory + "/" + newFilename;
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
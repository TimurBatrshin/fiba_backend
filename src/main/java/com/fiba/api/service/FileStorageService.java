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

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
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
            log.info("Initializing FileStorageService...");
            
            // Convert to absolute path and normalize
            rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            log.info("Root upload directory: {}", rootLocation);
            
            // Создаем основную директорию загрузки, если она не существует
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                log.info("Created base upload directory: {}", rootLocation);
            } else {
                log.info("Base upload directory already exists: {}", rootLocation);
            }
            
            // Создаем поддиректории для разных типов файлов
            String[] subdirs = {"tournaments", "sponsors", "ads", "profiles", "avatars"};
            for (String subdir : subdirs) {
                Path subdirPath = rootLocation.resolve(subdir);
                if (!Files.exists(subdirPath)) {
                    Files.createDirectories(subdirPath);
                    log.info("Created subdirectory: {}", subdirPath);
                } else {
                    log.info("Subdirectory already exists: {}", subdirPath);
                }
            }
            log.info("FileStorageService initialized successfully");
        } catch (IOException e) {
            String msg = "Could not initialize storage location: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "Unexpected error initializing storage: " + e.getMessage();
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
            log.warn("Attempt to save empty file in directory {}", subdirectory);
            throw new IllegalArgumentException("Failed to store empty file");
        }
        
        try {
            // Получаем имя файла
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            log.info("Processing file: {}, size: {} bytes, type: {}, directory: {}",
                     originalFilename, file.getSize(), file.getContentType(), subdirectory);
            
            // Проверяем имя файла на недопустимые символы
            if (originalFilename.contains("..")) {
                String msg = "Filename contains invalid path sequence: " + originalFilename;
                log.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            // Генерируем уникальное имя файла, сохраняя расширение
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            log.debug("Generated new filename: {}", newFilename);
            
            // Get subdirectory path
            Path uploadPath = rootLocation.resolve(subdirectory);
            if (!Files.exists(uploadPath)) {
                log.info("Directory {} does not exist, creating...", uploadPath);
                Files.createDirectories(uploadPath);
                log.info("Created directory: {}", uploadPath);
            }
            
            // Сохраняем файл
            Path targetLocation = uploadPath.resolve(newFilename);
            log.debug("Copying file to: {}", targetLocation);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved successfully: {}", targetLocation);
            
            // Return relative URL path for the file
            String filePath = "uploads/" + subdirectory + "/" + newFilename;
            log.info("Returning file URL path: {}", filePath);
            return filePath;
        } catch (IOException e) {
            String msg = String.format("Failed to store file in %s: %s", subdirectory, e.getMessage());
            log.error(msg, e);
            throw new IOException(msg, e);
        } catch (Exception e) {
            String msg = String.format("Unexpected error storing file in %s: %s", subdirectory, e.getMessage());
            log.error(msg, e);
            throw new IOException(msg, e);
        }
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
    public String storeFile(MultipartFile file) throws IOException {
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
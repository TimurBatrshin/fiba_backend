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
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    /**
     * Инициализирует сервис хранения файлов, создавая необходимые директории
     * Вызывается автоматически при запуске приложения
     */
    @PostConstruct
    public void init() {
        try {
            log.info("Инициализация FileStorageService...");
            // Создаем основную директорию загрузки, если она не существует
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Создана базовая директория для загрузки файлов: {}", uploadPath);
            } else {
                log.info("Базовая директория для загрузки файлов уже существует: {}", uploadPath);
            }
            
            // Создаем поддиректории для разных типов файлов
            String[] subdirs = {"tournaments", "sponsors", "ads", "profiles", "avatars"};
            for (String subdir : subdirs) {
                Path subdirPath = Paths.get(uploadDir, subdir);
                if (!Files.exists(subdirPath)) {
                    Files.createDirectories(subdirPath);
                    log.info("Создана поддиректория: {}", subdirPath);
                } else {
                    log.info("Поддиректория уже существует: {}", subdirPath);
                }
            }
            log.info("FileStorageService инициализирован успешно");
        } catch (IOException e) {
            log.error("Ошибка при инициализации FileStorageService", e);
            // Не выбрасываем исключение, чтобы не блокировать запуск приложения
        } catch (Exception e) {
            log.error("Неожиданная ошибка при инициализации FileStorageService", e);
        }
    }
    
    /**
     * Сохраняет загруженный файл в директории для изображений турниров
     *
     * @param file Загруженный файл
     * @return Путь к сохраненному файлу относительно корня приложения
     * @throws IOException Если произошла ошибка при сохранении файла
     */
    public String storeTournamentImage(MultipartFile file) throws IOException {
        return storeFile(file, "tournaments");
    }
    
    /**
     * Сохраняет загруженный файл в директории для логотипов спонсоров
     *
     * @param file Загруженный файл
     * @return Путь к сохраненному файлу относительно корня приложения
     * @throws IOException Если произошла ошибка при сохранении файла
     */
    public String storeSponsorLogo(MultipartFile file) throws IOException {
        return storeFile(file, "sponsors");
    }
    
    /**
     * Сохраняет загруженный файл в директории для изображений рекламы
     *
     * @param file Загруженный файл
     * @return Путь к сохраненному файлу относительно корня приложения
     * @throws IOException Если произошла ошибка при сохранении файла
     */
    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, "ads");
    }
    
    /**
     * Сохраняет загруженный файл как фотографию профиля
     *
     * @param file Загруженный файл
     * @return Путь к сохраненному файлу относительно корня приложения
     * @throws IOException Если произошла ошибка при сохранении файла
     */
    public String storeProfilePhoto(MultipartFile file) throws IOException {
        return storeFile(file, "profiles");
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
            log.warn("Попытка сохранить пустой файл в директории {}", subdirectory);
            return null;
        }
        
        try {
            // Получаем имя файла
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            log.info("Обработка файла: {}, размер: {} байт, тип: {}, директория: {}",
                     originalFilename, file.getSize(), file.getContentType(), subdirectory);
            
            // Проверяем имя файла на недопустимые символы
            if (originalFilename.contains("..")) {
                log.error("Имя файла содержит недопустимые символы: {}", originalFilename);
                throw new IOException("Имя файла содержит недопустимые символы: " + originalFilename);
            }
            
            // Генерируем уникальное имя файла, сохраняя расширение
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            log.debug("Сгенерировано новое имя файла: {}", newFilename);
            
            // Создаем директорию для сохранения файла
            Path uploadPath = Paths.get(uploadDir, subdirectory);
            if (!Files.exists(uploadPath)) {
                log.info("Директория {} не существует, создаем...", uploadPath);
                Files.createDirectories(uploadPath);
                log.info("Создана директория: {}", uploadPath);
            }
            
            // Сохраняем файл
            Path targetLocation = uploadPath.resolve(newFilename);
            log.debug("Копирование файла в: {}", targetLocation);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Файл успешно сохранен: {}", targetLocation);
            
            // Возвращаем относительный путь к файлу для хранения в БД
            String filePath = "/" + uploadDir + "/" + subdirectory + "/" + newFilename;
            log.info("Возвращаемый путь к файлу: {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла в {}: {}", subdirectory, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при сохранении файла в {}: {}", subdirectory, e.getMessage(), e);
            throw new IOException("Неожиданная ошибка при сохранении файла: " + e.getMessage(), e);
        }
    }
}
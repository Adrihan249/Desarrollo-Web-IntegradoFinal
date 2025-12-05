package com.taskmanager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración de Almacenamiento de Archivos
 *
 * CUMPLE REQUERIMIENTO N°11: Adjuntar archivos
 *
 * Configura:
 * - Directorio de uploads
 * - Tamaño máximo de archivos
 * - MultipartResolver para manejar uploads
 */
@Configuration
@Slf4j
public class FileStorageConfig {

    @Value("${file.upload-dir:uploads/attachments}")
    private String uploadDir;

    @Value("${file.max-size:52428800}") // 50MB por defecto
    private long maxFileSize;

    /**
     * Crea el directorio de uploads si no existe
     */
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Upload directory created: {}", uploadPath.toAbsolutePath());
            } else {
                log.info("Upload directory already exists: {}", uploadPath.toAbsolutePath());
            }

            log.info("File upload configuration:");
            log.info("  - Upload directory: {}", uploadDir);
            log.info("  - Max file size: {} MB", maxFileSize / (1024 * 1024));

        } catch (IOException e) {
            log.error("Could not create upload directory: {}", e.getMessage());
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    /**
     * Configura el MultipartResolver
     * Necesario para manejar uploads de archivos
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
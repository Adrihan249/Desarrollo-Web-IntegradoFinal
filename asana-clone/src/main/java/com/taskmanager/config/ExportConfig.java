package com.taskmanager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

// ===================================
// EXPORT CONFIG
// ===================================
@Configuration
@Slf4j
public class ExportConfig {

    /**
     * Crear directorio de exports si no existe
     *
     * Importante:
     * - NO puede ser un @Bean porque retorna void
     * - Se usa @PostConstruct porque solo ejecuta lógica de inicio
     */
    @PostConstruct
    public void initializeExportDirectory() {
        String exportDir = System.getProperty("export.directory", "uploads/exports");
        java.nio.file.Path path = java.nio.file.Paths.get(exportDir);

        try {
            if (!java.nio.file.Files.exists(path)) {
                java.nio.file.Files.createDirectories(path);
                log.info("Export directory created: {}", path.toAbsolutePath());
            } else {
                log.info("Export directory exists: {}", path.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Failed to create export directory", e);
        }
    }
}

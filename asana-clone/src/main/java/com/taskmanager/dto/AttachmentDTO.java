package com.taskmanager.dto;

// ============================================================================

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTOs para Attachment
 *
 * CUMPLE REQUERIMIENTO N°11: Adjuntar archivos
 */
public class AttachmentDTO {

    /**
     * DTO para subir un archivo
     * El archivo se envía como multipart/form-data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadRequest {

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        private String description;

        // El archivo se recibe como MultipartFile en el controller
        // Este DTO contiene solo la metadata adicional
    }

    /**
     * DTO de respuesta de archivo adjunto
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long taskId;
        private UserDTO.Summary uploadedBy;
        private String fileName;
        private String filePath;
        private String downloadUrl; // URL para descargar el archivo
        private String mimeType;
        private Long fileSize;
        private String formattedFileSize; // "2.5 MB"
        private String fileExtension;
        private String description;
        private Boolean isImage;
        private String thumbnailUrl;
        private Integer downloadCount;
        private LocalDateTime createdAt;
    }

    /**
     * DTO resumido para listados
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String fileName;
        private String fileExtension;
        private String formattedFileSize;
        private Boolean isImage;
        private String thumbnailUrl;
        private LocalDateTime createdAt;
    }
}
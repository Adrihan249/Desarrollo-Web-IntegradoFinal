package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Attachment (Archivo Adjunto)
 *
 * CUMPLE REQUERIMIENTO N°11: Adjuntar archivos
 *
 * Permite a los usuarios adjuntar archivos a las tareas:
 * - Documentos (PDF, Word, Excel)
 * - Imágenes (PNG, JPG, GIF)
 * - Archivos comprimidos (ZIP, RAR)
 * - Código fuente
 * - Cualquier tipo de archivo
 *
 * Los archivos se almacenan en el sistema de archivos o en S3
 * Esta entidad guarda la metadata del archivo
 */
@Entity
@Table(name = "attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tarea a la que pertenece el archivo
     */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    /**
     * Usuario que subió el archivo
     */
    @ManyToOne
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    /**
     * Nombre original del archivo
     */
    @Column(nullable = false, length = 255)
    private String fileName;

    /**
     * Nombre del archivo en el sistema de almacenamiento
     * Suele ser un UUID + extensión para evitar conflictos
     * Ejemplo: "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf"
     */
    @Column(nullable = false, length = 255)
    private String storedFileName;

    /**
     * Ruta o URL donde está almacenado el archivo
     * - Local: "/uploads/files/..."
     * - S3: "https://bucket.s3.amazonaws.com/..."
     */
    @Column(nullable = false, length = 500)
    private String filePath;

    /**
     * Tipo MIME del archivo
     * Ejemplos: "application/pdf", "image/png", "text/plain"
     */
    @Column(nullable = false, length = 100)
    private String mimeType;

    /**
     * Tamaño del archivo en bytes
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * Extensión del archivo (sin el punto)
     * Ejemplos: "pdf", "png", "docx"
     */
    @Column(length = 10)
    private String fileExtension;

    /**
     * Descripción o notas sobre el archivo (opcional)
     */
    @Column(length = 500)
    private String description;

    /**
     * Indica si es una imagen (para preview)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isImage = false;

    /**
     * URL de thumbnail/preview (para imágenes)
     */
    @Column(length = 500)
    private String thumbnailUrl;

    /**
     * Hash MD5 del archivo (para verificar integridad)
     */
    @Column(length = 32)
    private String fileHash;

    /**
     * Número de descargas del archivo
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Tipos de archivo comunes
     */
    public enum FileType {
        IMAGE("Imagen", new String[]{"jpg", "jpeg", "png", "gif", "bmp", "svg"}),
        DOCUMENT("Documento", new String[]{"pdf", "doc", "docx", "txt", "rtf"}),
        SPREADSHEET("Hoja de Cálculo", new String[]{"xls", "xlsx", "csv"}),
        PRESENTATION("Presentación", new String[]{"ppt", "pptx"}),
        COMPRESSED("Archivo Comprimido", new String[]{"zip", "rar", "7z", "tar", "gz"}),
        CODE("Código Fuente", new String[]{"java", "js", "py", "cpp", "cs", "html", "css"}),
        VIDEO("Video", new String[]{"mp4", "avi", "mov", "wmv"}),
        AUDIO("Audio", new String[]{"mp3", "wav", "ogg"}),
        OTHER("Otro", new String[]{});

        private final String displayName;
        private final String[] extensions;

        FileType(String displayName, String[] extensions) {
            this.displayName = displayName;
            this.extensions = extensions;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String[] getExtensions() {
            return extensions;
        }

        /**
         * Determina el tipo de archivo basado en la extensión
         */
        public static FileType fromExtension(String extension) {
            if (extension == null) return OTHER;

            String ext = extension.toLowerCase();

            for (FileType type : values()) {
                for (String typeExt : type.extensions) {
                    if (typeExt.equals(ext)) {
                        return type;
                    }
                }
            }

            return OTHER;
        }
    }

    /**
     * Incrementa el contador de descargas
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * Obtiene el tamaño formateado legible
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Verifica si el archivo es descargable
     */
    public boolean isDownloadable() {
        return filePath != null && !filePath.isEmpty();
    }
}
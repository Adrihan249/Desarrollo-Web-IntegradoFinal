// ===================================
// EXPORT JOB ENTITY
// ===================================
package com.taskmanager.model;

import com.taskmanager.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "export_jobs", indexes = {
        @Index(name = "idx_export_user", columnList = "requested_by_id"),
        @Index(name = "idx_export_status", columnList = "status"),
        @Index(name = "idx_export_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    // CONFIGURACIÓN DEL EXPORT
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExportType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExportFormat format;

    private Long referenceId; // ID del proyecto/suscripción

    // FILTROS APLICADOS
    @Column(columnDefinition = "TEXT")
    private String filters; // JSON con filtros

    // ESTADO DEL JOB
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExportStatus status = ExportStatus.PENDING;

    @Column(length = 255)
    private String fileName;

    @Column(length = 500)
    private String filePath;

    private Long fileSize; // Bytes

    // PROGRESO
    private Integer totalRecords;

    private Integer processedRecords;

    @Column(nullable = false)
    private Integer progress = 0; // 0-100%

    // RESULTADO
    @Column(length = 500)
    private String downloadUrl;

    private LocalDateTime expiresAt; // El archivo expira en 7 días

    @Column(nullable = false)
    private Integer downloadCount = 0;

    // ERROR HANDLING
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad UserBlock (Bloqueo de Usuarios)
 *
 * Permite que usuarios bloqueen a otros para no recibir sus mensajes
 */
@Entity
@Table(name = "user_blocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que bloquea
     */
    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    /**
     * Usuario bloqueado
     */
    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    /**
     * Razón del bloqueo (opcional)
     */
    @Column(length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
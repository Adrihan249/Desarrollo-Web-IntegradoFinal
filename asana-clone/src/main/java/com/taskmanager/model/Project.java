package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Entidad Project (Proyecto)
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°3: Creación de proyectos
 * - N°4: Asignación de colaboradores (relación ManyToMany con User)
 */

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;
    @Column(length = 1000)

    private String description;
    /**

     * Requerimiento N°4: Asignación de colaboradores

     * ManyToMany: Un proyecto puede tener múltiples miembros

     */
    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )

    @Builder.Default
    private Set<User> members = new HashSet<>();
    /**

     * Usuario que creó el proyecto

     */

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    /**

     * Estado del proyecto

     */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;
    /**
     * Color para identificación visual del proyecto (hexadecimal)
     */
    @Column(length = 7)
    @Builder.Default
    private String color = "#3B82F6"; // Azul por defecto
    /**
     * Fecha límite del proyecto (N°17)
     */
    @Column
    private LocalDateTime deadline;
    /**

     * Procesos del proyecto (N°5)

     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Process> processes = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ActivityLog> activityLogs = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
    @Column(nullable = false, updatable = false)

    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    /**

     * Estados posibles del proyecto

     */

    public enum ProjectStatus {
        ACTIVE("Activo"),
        ON_HOLD("En Progreso"),
        COMPLETED("Completado"),
        CANCELLED("Cancelado");


        private final String displayName;
        ProjectStatus(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }
    // 🔥 NUEVA RELACIÓN: Lista de invitaciones asociadas a este proyecto
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Invitation> invitations = new HashSet<>();

    @Column(nullable = false)
    /**
     * Método helper para agregar un miembro al proyecto
     */
    public void addMember(User user) {
        this.members.add(user);
        user.getProjects().add(this);
    }
    /**

     * Método helper para remover un miembro del proyecto

     */
    public void removeMember(User user) {
        this.members.remove(user);
        user.getProjects().remove(this);

    }
    /**
     * Verifica si un usuario es miembro del proyecto

     */
    public boolean hasMember(User user) {
        return this.members.contains(user);

    }
}
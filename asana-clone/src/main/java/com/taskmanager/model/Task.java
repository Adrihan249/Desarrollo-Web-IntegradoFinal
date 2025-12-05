package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad Task (Tarea)
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°6: Estados de tareas
 * - N°18: Subtareas (relación auto-referencial)
 * - N°10: Comentarios en tareas (OneToMany con Comment)
 * - N°11: Adjuntar archivos (OneToMany con Attachment)
 * - N°17: Fecha límite y recordatorios
 *
 * Una tarea pertenece a un proceso (columna Kanban) y puede tener:
 * - Subtareas (otras tareas hijas)
 * - Asignados (usuarios responsables)
 * - Comentarios
 * - Archivos adjuntos
 * - Prioridad y estado
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    /**
     * N°5: Proceso (columna Kanban) al que pertenece la tarea
     */
    @ManyToOne
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    /**
     * Proyecto al que pertenece (desnormalización para queries rápidas)
     */
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Usuario que creó la tarea
     */
    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /**
     * Usuarios asignados a la tarea (responsables)
     * ManyToMany porque una tarea puede tener múltiples asignados
     */
    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> assignees = new HashSet<>();

    /**
     * N°6: Estado de la tarea
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    /**
     * Prioridad de la tarea
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    /**
     * N°17: Fecha límite de la tarea
     */
    @Column
    private LocalDateTime dueDate;

    /**
     * Fecha de inicio de la tarea
     */
    @Column
    private LocalDateTime startDate;

    /**
     * Fecha de completado (se setea automáticamente al completar)
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * N°18: Tarea padre (para subtareas)
     * Si es null, es una tarea principal
     */
    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    /**
     * N°18: Subtareas de esta tarea
     * Relación auto-referencial OneToMany
     */
    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> subtasks = new ArrayList<>();

    /**
     * N°10: Comentarios en la tarea
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    /**
     * N°11: Archivos adjuntos
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Posición en el proceso (para ordenamiento en la columna Kanban)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer position = 0;

    /**
     * Etiquetas de la tarea (para filtrado y organización)
     */
    @ElementCollection
    @CollectionTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id")
    )
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    /**
     * Estimación de tiempo en horas
     */
    @Column
    private Integer estimatedHours;

    /**
     * Tiempo real gastado en horas
     */
    @Column
    @Builder.Default
    private Integer actualHours = 0;

    /**
     * Porcentaje de completado (0-100)
     * Útil para tareas con subtareas
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer completionPercentage = 0;

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
     * N°6: Estados posibles de una tarea
     */
    public enum TaskStatus {
        TODO("Por Hacer"),
        IN_PROGRESS("En Progreso"),
        IN_REVIEW("En Revisión"),
        BLOCKED("Bloqueada"),
        DONE("Completada"),
        CANCELLED("Cancelada");

        private final String displayName;

        TaskStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Prioridades de tareas
     */
    public enum TaskPriority {
        LOW("Baja"),
        MEDIUM("Media"),
        HIGH("Alta"),
        URGENT("Urgente");

        private final String displayName;

        TaskPriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Método helper para agregar un asignado
     */
    public void addAssignee(User user) {
        this.assignees.add(user);
    }

    /**
     * Método helper para remover un asignado
     */
    public void removeAssignee(User user) {
        this.assignees.remove(user);
    }

    /**
     * N°18: Método helper para agregar una subtarea
     */
    public void addSubtask(Task subtask) {
        this.subtasks.add(subtask);
        subtask.setParentTask(this);
    }

    /**
     * N°18: Verifica si es una subtarea
     */
    public boolean isSubtask() {
        return this.parentTask != null;
    }

    /**
     * N°18: Calcula el porcentaje de completado basado en subtareas
     */
    public void updateCompletionFromSubtasks() {
        if (subtasks.isEmpty()) {
            return;
        }

        int completedSubtasks = (int) subtasks.stream()
                .filter(st -> st.getStatus() == TaskStatus.DONE)
                .count();

        this.completionPercentage = (completedSubtasks * 100) / subtasks.size();
    }

    /**
     * N°6: Marca la tarea como completada
     */
    public void markAsCompleted() {
        this.status = TaskStatus.DONE;
        this.completedAt = LocalDateTime.now();
        this.completionPercentage = 100;
    }

    /**
     * Verifica si la tarea está vencida
     */
    public boolean isOverdue() {
        return dueDate != null &&
                LocalDateTime.now().isAfter(dueDate) &&
                status != TaskStatus.DONE &&
                status != TaskStatus.CANCELLED;
    }
}
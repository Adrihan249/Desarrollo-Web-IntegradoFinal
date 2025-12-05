package com.taskmanager.model;



import jakarta.persistence.*;

import lombok.*;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;



/**

 * Entidad Process (Proceso/Columna Kanban)

 *

 * CUMPLE REQUERIMIENTO N°5: Gestión de procesos (Sprint 2)

 *

 * Representa una columna en el tablero Kanban del proyecto

 * Ejemplos: "Por Hacer", "En Progreso", "En Revisión", "Completado"

 *

 * Esta entidad está preparada para el Sprint 2 pero se define aquí

 * porque Project tiene una relación OneToMany con Process

 */

@Entity

@Table(name = "processes")

@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class Process {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false, length = 100)

    private String name;



    @Column(length = 500)

    private String description;



    /**

     * Proyecto al que pertenece este proceso

     */

    @ManyToOne

    @JoinColumn(name = "project_id", nullable = false)

    private Project project;



    /**

     * Orden de visualización en el tablero (0, 1, 2, ...)

     */

    @Column(nullable = false)

    @Builder.Default

    private Integer position = 0;



    /**

     * Color para identificación visual (hexadecimal)

     */

    @Column(length = 7)

    @Builder.Default

    private String color = "#6B7280"; // Gris por defecto



    /**

     * Límite de tareas (Work In Progress limit)

     * null = sin límite

     */

    @Column

    private Integer taskLimit;



    /**

     * Indica si este proceso representa tareas completadas

     * Útil para estadísticas y reportes

     */

    @Column(nullable = false)

    @Builder.Default

    private Boolean isCompleted = false;



    @Column(nullable = false, updatable = false)

    private LocalDateTime createdAt;



    @Column(nullable = false)

    private LocalDateTime updatedAt;



    @PrePersist

    protected void onCreate() {

        createdAt = LocalDateTime.now();

        updatedAt = LocalDateTime.now();

    }

    /**

     * Tareas pertenecientes a este proceso (columna Kanban)

     */

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)

    @Builder.Default

    private List<Task> tasks = new ArrayList<>();



    @PreUpdate

    protected void onUpdate() {

        updatedAt = LocalDateTime.now();

    }



    /**

     * Procesos predeterminados comunes en gestión de proyectos

     */

    public enum DefaultProcessType {

        TODO("Por Hacer", "#EF4444", false),

        IN_PROGRESS("En Progreso", "#F59E0B", false),

        IN_REVIEW("En Revisión", "#3B82F6", false),

        DONE("Completado", "#10B981", true);



        private final String name;

        private final String color;

        private final boolean isCompleted;



        DefaultProcessType(String name, String color, boolean isCompleted) {

            this.name = name;

            this.color = color;

            this.isCompleted = isCompleted;

        }



        public String getName() {

            return name;

        }



        public String getColor() {

            return color;

        }



        public boolean isCompleted() {

            return isCompleted;

        }



    }

}
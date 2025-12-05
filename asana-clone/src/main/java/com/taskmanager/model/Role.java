package com.taskmanager.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Role (Rol)
 *
 * CUMPLE REQUERIMIENTO N°2: Gestión de roles
 *
 * Define los diferentes roles que pueden tener los usuarios:
 * - ADMIN: Administrador del sistema
 * - PROJECT_MANAGER: Gestor de proyectos
 * - MEMBER: Miembro colaborador
 * - VIEWER: Visualizador (solo lectura)
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del rol (debe empezar con ROLE_ por convención de Spring Security)
     * Ejemplos: ROLE_ADMIN, ROLE_PROJECT_MANAGER, ROLE_MEMBER
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    // Relación inversa con User
    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude // Evita recursión infinita en toString()
    @Builder.Default
    private Set<User> users = new HashSet<>();

    /**
     * Enumeración de roles del sistema
     */
    public enum RoleType {
        ROLE_ADMIN("Administrador del sistema"),
        ROLE_PROJECT_MANAGER("Gestor de proyectos"),
        ROLE_MEMBER("Miembro colaborador"),
        ROLE_VIEWER("Visualizador");

        private final String description;

        RoleType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
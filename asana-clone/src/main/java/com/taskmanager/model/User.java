package com.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- IMPORT NECESARIO
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidad Usuario del Sistema - CORREGIDA
 *
 * ✅ CORRECCIÓN CRÍTICA DE SERIALIZACIÓN:
 * - Se añade @JsonIgnore a todas las colecciones bidireccionales de JPA
 * (proyectos, tareas, etc.) para romper el ciclo de serialización
 * Task <-> User y Project <-> User, resolviendo el error 500.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"projects", "createdProjects", "roles", "createdTasks", "assignedTasks"})
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private String password;

    // Requerimiento N°16: Personalización de perfil
    @Column(length = 500)
    private String bio;

    @Column(length = 255)
    private String avatarUrl;

    @Column(length = 20)
    private String phoneNumber;

    // Requerimiento N°2: Gestión de roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // ===================================================================
    // CORRECCIONES DE SERIALIZACIÓN (@JsonIgnore)
    // ===================================================================

    // Proyectos de los que es miembro (MAPPED BY 'members' en Project)
    @JsonIgnore // <-- ¡CRÍTICO! ROMPE EL CICLO PROJECT <-> USER
    @ManyToMany(mappedBy = "members")
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    // Proyectos creados (MAPPED BY 'createdBy' en Project)
    @JsonIgnore // <-- ¡CRÍTICO! ROMPE EL CICLO PROJECT <-> USER
    @OneToMany(mappedBy = "createdBy")
    @Builder.Default
    private List<Project> createdProjects = new ArrayList<>();

    // TAREAS CREADAS (MAPPED BY 'createdBy' en Task)
    @JsonIgnore // <-- ¡CRÍTICO! ROMPE EL CICLO TASK <-> USER
    @OneToMany(mappedBy = "createdBy")
    @Builder.Default
    private Set<Task> createdTasks = new HashSet<>();

    // TAREAS ASIGNADAS (MAPPED BY 'assignees' en Task)
    @JsonIgnore // <-- ¡CRÍTICO! ROMPE EL CICLO TASK <-> USER
    @ManyToMany(mappedBy = "assignees")
    @Builder.Default
    private Set<Task> assignedTasks = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ===================================================================
    // Auditoría automática
    // ===================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===================================================================
    // Implementación de UserDetails para Spring Security
    // ===================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active != null && active;
    }

    // ===================================================================
    // Métodos helper
    // ===================================================================

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Boolean getActive() {
        return active != null ? active : true;
    }
}
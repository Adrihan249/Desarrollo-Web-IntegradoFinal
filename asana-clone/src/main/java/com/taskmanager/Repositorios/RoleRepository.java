package com.taskmanager.Repositorios;

import com.taskmanager.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ===================================================================
 * RoleRepository - Repositorio de Roles
 *
 * CUMPLE REQUERIMIENTO N°2: Gestión de roles
 * ===================================================================
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * N°2: Busca rol por nombre
     * Query Method: SELECT * FROM roles WHERE name = ?
     */
    Optional<Role> findByName(String name);

    /**
     * N°2: Verifica si existe un rol
     */
    boolean existsByName(String name);
}
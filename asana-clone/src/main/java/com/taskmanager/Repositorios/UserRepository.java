package com.taskmanager.Repositorios;

import com.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ===================================================================
 * UserRepository - Repositorio de Usuarios
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°1: Autenticación (findByEmail)
 * - N°9: Gestión de usuarios (CRUD completo con Query Methods)
 *
 * Query Methods: Spring Data JPA genera automáticamente las consultas
 * basándose en el nombre del método. No necesitas escribir SQL.
 * ===================================================================
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * N°1: Busca usuario por email (para autenticación)
     * Query Method: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * N°9: Verifica si existe un usuario con ese email
     * Query Method: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);

    /**
     * N°9: Busca usuarios activos
     * Query Method: SELECT * FROM users WHERE active = true
     */
    List<User> findByActiveTrue();

    /**
     * N°9: Busca usuarios por nombre o apellido (búsqueda flexible)
     * Query Method: SELECT * FROM users WHERE
     *               first_name LIKE %keyword% OR last_name LIKE %keyword%
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    /**
     * N°2: Busca usuarios por rol
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
}
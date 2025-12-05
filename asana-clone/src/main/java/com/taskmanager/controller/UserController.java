package com.taskmanager.controller;

import com.taskmanager.Repositorios.UserRepository;
import com.taskmanager.dto.UserDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.model.User;
import com.taskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controlador REST de Usuarios
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°9: Gestión de usuarios (CRUD completo)
 * - N°2: Gestión de roles (asignación de roles)
 * - N°16: Personalización de perfil
 *
 * @PreAuthorize: Autorización basada en roles (N°2)
 * @AuthenticationPrincipal: Inyecta el usuario autenticado actual
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class UserController {
    // Estas tres dependencias serán inyectadas automáticamente por el constructor
    // generado por @RequiredArgsConstructor.
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserService userService;

    // TODO: Eliminar el constructor manual. Spring/Lombok lo generan por nosotros.
    /*
    public UserController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userService= new UserService(); // ANTES: Fallaba al llamar a new UserService() sin argumentos
    }
    */

    /**
     * N°9: Obtiene todos los usuarios
     * GET /api/users
     *
     * @return ResponseEntity con lista de usuarios
     *
     * Status codes:
     * - 200 OK: Lista retornada exitosamente
     */
    @GetMapping
    public ResponseEntity<List<UserDTO.Response>> getAllUsers() {
        log.info("GET /api/users - Fetching all users");

        List<UserDTO.Response> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    /**
     * N°9: Obtiene un usuario por ID
     * GET /api/users/{id}
     *
     * @param id ID del usuario
     * @return ResponseEntity con datos del usuario
     *
     * Status codes:
     * - 200 OK: Usuario encontrado
     * - 404 NOT FOUND: Usuario no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.Response> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Fetching user by ID", id);

        try {
            UserDTO.Response user = userService.getUserById(id);
            return ResponseEntity.ok(user);

        } catch (ResourceNotFoundException e) { // Usar ResourceNotFoundException para mayor claridad
            log.error("User not found with ID: {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
    /**
     * Busca un usuario por email
     * GET /api/users/search?email=user@example.com
     */
    @GetMapping("/search")
    public ResponseEntity<UserDTO.Summary> searchUserByEmail(
            @RequestParam String email,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/users/search?email={}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con email: " + email
                    ));

            // No permitir buscar a uno mismo
            if (user.getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().build();
            }

            UserDTO.Summary summary = userMapper.userToSummary(user);
            return ResponseEntity.ok(summary);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * N°9: Busca usuarios por palabra clave
     * GET /api/users/search?keyword=texto
     *
     * Nota: Tienes dos métodos @GetMapping("/search"). Asegúrate de que las
     * firmas sean lo suficientemente distintas o usa paths diferentes.
     * Este método se diferencia por el parámetro 'keyword' vs el parámetro 'email'
     * en el otro, lo cual está bien en Spring.
     *
     * @param keyword Palabra clave para buscar
     * @return ResponseEntity con lista de usuarios encontrados
     *
     * Usa Query Method sin SQL
     *
     * Status codes:
     * - 200 OK: Búsqueda realizada (puede retornar lista vacía)
     */
    @GetMapping(value = "/search", params = "keyword") // Añadido 'params' para mayor claridad
    public ResponseEntity<List<UserDTO.Response>> searchUsers(
            @RequestParam String keyword) {
        log.info("GET /api/users/search?keyword={}", keyword);

        List<UserDTO.Response> users = userService.searchUsers(keyword);

        return ResponseEntity.ok(users);
    }

    /**
     * Busca un usuario por email
     * GET /api/users/search?email=user@example.com
     */
    @GetMapping(value = "/search", params = "email") // Añadido 'params' para mayor claridad
    public ResponseEntity<UserDTO.Summary> searchUserByEmailWithEmailParam( // Renombrado para evitar ambigüedad de método si no se usa params
                                                                            @RequestParam String email,
                                                                            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/users/search?email={}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con email: " + email
                    ));

            // No permitir buscar a uno mismo
            if (user.getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().build();
            }

            UserDTO.Summary summary = userMapper.userToSummary(user);
            return ResponseEntity.ok(summary);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * N°16: Actualiza perfil de usuario
     * PUT /api/users/{id}
     *
     * @param id ID del usuario a actualizar
     * @param request Datos a actualizar
     * @param currentUser Usuario autenticado actual
     * @return ResponseEntity con usuario actualizado
     *
     * Solo el propio usuario puede actualizar su perfil
     * (excepto ADMIN que puede actualizar cualquiera)
     *
     * Status codes:
     * - 200 OK: Usuario actualizado
     * - 403 FORBIDDEN: No tiene permisos
     * - 404 NOT FOUND: Usuario no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO.Response> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/users/{} - Updating user", id);

        // Valida que el usuario actualice su propio perfil o sea ADMIN
        if (!currentUser.getId().equals(id) &&
                !hasRole(currentUser, "ROLE_ADMIN")) {
            log.warn("User ID {} attempted to update user ID {} without permission",
                    currentUser.getId(), id);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        try {
            UserDTO.Response updatedUser = userService.updateUser(id, request);
            return ResponseEntity.ok(updatedUser);

        } catch (ResourceNotFoundException e) { // Usar ResourceNotFoundException para mayor claridad
            log.error("Error updating user ID {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°9: Cambia contraseña del usuario
     * POST /api/users/{id}/change-password
     *
     * @param id ID del usuario
     * @param request Contraseña actual y nueva
     * @param currentUser Usuario autenticado actual
     * @return ResponseEntity sin contenido
     *
     * Status codes:
     * - 204 NO CONTENT: Contraseña cambiada exitosamente
     * - 400 BAD REQUEST: Contraseña actual incorrecta
     * - 403 FORBIDDEN: No tiene permisos
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/users/{}/change-password", id);

        // Solo el propio usuario puede cambiar su contraseña
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        try {
            userService.changePassword(id, request);

            // 204 NO CONTENT: Operación exitosa sin contenido en respuesta
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            // Contraseña actual incorrecta
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    /**
     * N°9: Desactiva un usuario (soft delete)
     * DELETE /api/users/{id}
     *
     * Solo ADMIN puede desactivar usuarios
     *
     * @param id ID del usuario a desactivar
     * @return ResponseEntity sin contenido
     *
     * Status codes:
     * - 204 NO CONTENT: Usuario desactivado
     * - 403 FORBIDDEN: No es ADMIN
     * - 404 NOT FOUND: Usuario no existe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deactivating user", id);

        try {
            userService.deactivateUser(id);
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) { // Usar ResourceNotFoundException para mayor claridad
            log.error("Error deactivating user ID {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°9: Reactiva un usuario
     * POST /api/users/{id}/activate
     *
     * Solo ADMIN puede reactivar usuarios
     *
     * @param id ID del usuario a reactivar
     * @return ResponseEntity sin contenido
     *
     * Status codes:
     * - 204 NO CONTENT: Usuario reactivado
     * - 403 FORBIDDEN: No es ADMIN
     * - 404 NOT FOUND: Usuario no existe
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        log.info("POST /api/users/{}/activate", id);

        try {
            userService.activateUser(id);
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) { // Usar ResourceNotFoundException para mayor claridad
            log.error("Error activating user ID {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°2: Asigna roles a un usuario
     * PUT /api/users/{id}/roles
     *
     * Solo ADMIN puede asignar roles
     *
     * @param id ID del usuario
     * @param roleNames Set de nombres de roles a asignar
     * @return ResponseEntity con usuario actualizado
     *
     * Status codes:
     * - 200 OK: Roles asignados
     * - 403 FORBIDDEN: No es ADMIN
     * - 404 NOT FOUND: Usuario o rol no existe
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO.Response> assignRoles(
            @PathVariable Long id,
            @RequestBody Set<String> roleNames) {
        log.info("PUT /api/users/{}/roles - Assigning roles: {}", id, roleNames);

        try {
            UserDTO.Response updatedUser = userService.assignRoles(id, roleNames);
            return ResponseEntity.ok(updatedUser);

        } catch (ResourceNotFoundException e) { // Usar ResourceNotFoundException para mayor claridad
            log.error("Error assigning roles to user ID {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°9: Obtiene lista resumida de usuarios
     * GET /api/users/summary
     *
     * Útil para selects y listados donde no se necesita toda la info
     *
     * @return ResponseEntity con lista resumida
     *
     * Status codes:
     * - 200 OK: Lista retornada
     */
    @GetMapping("/summary")
    public ResponseEntity<List<UserDTO.Summary>> getAllUsersSummary() {
        log.info("GET /api/users/summary");

        List<UserDTO.Summary> users = userService.getAllUsersSummary();

        return ResponseEntity.ok(users);
    }

    /**
     * Método helper para verificar si un usuario tiene un rol específico
     */
    private boolean hasRole(User user, String roleName) {
        // Asumiendo que los nombres de rol en el objeto User no tienen el prefijo "ROLE_"
        // Si tienen el prefijo, usa roleName directamente. Si no, añade el prefijo para la verificación.
        String fullRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(fullRoleName));
    }
}
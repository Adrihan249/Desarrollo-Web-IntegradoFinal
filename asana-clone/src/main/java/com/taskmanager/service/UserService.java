package com.taskmanager.service;

import com.taskmanager.dto.UserDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.model.Role;
import com.taskmanager.model.User;
import com.taskmanager.Repositorios.RoleRepository;
import com.taskmanager.Repositorios.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de Gestión de Usuarios
 * ✅ Implementa UserDetailsService para Spring Security
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * ✅ CRÍTICO: Método requerido por Spring Security
     * Se llama automáticamente durante la autenticación
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("🔍 [UserDetailsService] Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ [UserDetailsService] User not found with email: {}", email);
                    return new UsernameNotFoundException(
                            "Usuario no encontrado con email: " + email
                    );
                });

        log.info("✅ [UserDetailsService] User loaded successfully: {} (ID: {})",
                user.getEmail(), user.getId());
        log.debug("   - Roles: {}", user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(", ")));
        log.debug("   - Active: {}", user.getActive());

        // ✅ Retorna el User que implementa UserDetails
        return user;
    }

    /**
     * N°1 + N°9: Crea un nuevo usuario
     */
    public UserDTO.Response createUser(UserDTO.RegisterRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        User user = userMapper.registerRequestToUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role memberRole = roleRepository.findByName(Role.RoleType.ROLE_MEMBER.name())
                .orElseThrow(() -> new ResourceNotFoundException("Rol MEMBER no encontrado"));
        user.setRoles(Set.of(memberRole));

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return userMapper.userToResponse(savedUser);
    }

    /**
     * N°9: Obtiene todos los usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Response> getAllUsers() {
        log.debug("Fetching all active users");
        return userRepository.findByActiveTrue().stream()
                .map(userMapper::userToResponse)
                .collect(Collectors.toList());
    }

    /**
     * N°9: Obtiene un usuario por ID
     */
    @Transactional(readOnly = true)
    public UserDTO.Response getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + id
                ));
        return userMapper.userToResponse(user);
    }

    /**
     * N°9: Busca usuarios por palabra clave
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Response> searchUsers(String keyword) {
        log.debug("Searching users with keyword: {}", keyword);
        return userRepository.searchByKeyword(keyword).stream()
                .map(userMapper::userToResponse)
                .collect(Collectors.toList());
    }

    /**
     * N°16: Actualiza perfil de usuario
     */
    public UserDTO.Response updateUser(Long id, UserDTO.UpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + id
                ));

        userMapper.updateUserFromDto(request, user);
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return userMapper.userToResponse(updatedUser);
    }

    /**
     * N°9: Cambia contraseña del usuario
     */
    public void changePassword(Long id, UserDTO.ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + id
                ));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", id);
    }

    /**
     * N°9: Desactiva un usuario (soft delete)
     */
    public void deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + id
                ));

        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully with ID: {}", id);
    }

    /**
     * N°9: Reactiva un usuario
     */
    public void activateUser(Long id) {
        log.info("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + id
                ));

        user.setActive(true);
        userRepository.save(user);

        log.info("User activated successfully with ID: {}", id);
    }

    /**
     * N°2: Asigna roles a un usuario
     */
    public UserDTO.Response assignRoles(Long userId, Set<String> roleNames) {
        log.info("Assigning roles to user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Rol no encontrado: " + roleName
                        )))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);

        log.info("Roles assigned successfully to user ID: {}", userId);
        return userMapper.userToResponse(updatedUser);
    }

    /**
     * N°9: Obtiene versión resumida de usuarios para listados
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Summary> getAllUsersSummary() {
        log.debug("Fetching all users summary");
        return userRepository.findByActiveTrue().stream()
                .map(userMapper::userToSummary)
                .collect(Collectors.toList());
    }
}
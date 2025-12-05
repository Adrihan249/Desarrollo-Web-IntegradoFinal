package com.taskmanager.service;

import com.taskmanager.dto.SubscriptionDTO;
import com.taskmanager.dto.UserDTO;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.model.Role;
import com.taskmanager.model.User;
import com.taskmanager.Repositorios.RoleRepository;
import com.taskmanager.Repositorios.UserRepository;
import com.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Servicio de Autenticación
 *
 * Maneja:
 * - Registro de nuevos usuarios y asignación de Plan Free 🚀
 * - Login y generación de tokens JWT
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SubscriptionService subscriptionService; // 💡 Inyectado

    /**
     * N°1: Registra un nuevo usuario en el sistema
     */
    public AuthResponse register(UserDTO.RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        User user = userMapper.registerRequestToUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Asigna rol por defecto: MEMBER
        Role memberRole = roleRepository.findByName(Role.RoleType.ROLE_MEMBER.name())
                .orElseThrow(() -> new IllegalStateException(
                        "Rol MEMBER no encontrado en el sistema"
                ));
        user.setRoles(Set.of(memberRole));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // 🚀 PASO CRÍTICO: ASIGNAR SUSCRIPCIÓN GRATUITA POR DEFECTO
        SubscriptionDTO.CreateRequest freeSubscriptionRequest = new SubscriptionDTO.CreateRequest();

        // ASUMIMOS ID=1 para el Plan Free. ¡VERIFICA ESTE ID!
        freeSubscriptionRequest.setPlanId(1L);
        freeSubscriptionRequest.setBillingPeriod(com.taskmanager.model.enums.BillingPeriod.MONTHLY);
        // Usar un valor que no sea null para PaymentMethod en el DTO de creación
        freeSubscriptionRequest.setPaymentMethod("FREE_PLAN");

        try {
            subscriptionService.createSubscription(savedUser.getId(), freeSubscriptionRequest);
            log.info("Default 'Free' subscription created for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to create default 'Free' subscription for user ID: {}", savedUser.getId(), e);
            // La transacción podría revertirse si esta excepción es grave
        }

        // Genera token JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", savedUser.getId());
        extraClaims.put("roles", savedUser.getRoles().stream()
                .map(Role::getName)
                .toList());

        String jwtToken = jwtService.generateToken(extraClaims, savedUser);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(userMapper.userToResponse(savedUser))
                .build();
    }
    // ...
    @Transactional(readOnly = true)
    public UserDTO.Response getUserDetailsByEmail(String email) {
        log.debug("Fetching user details for email: {}", email);

        // 1. Buscar User en el repositorio por email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario no encontrado para el email: " + email
                ));

        // 2. Mapearlo a UserDTO.Response y retornar
        return userMapper.userToResponse(user);
    }// 🚨 ERROR SINTÁCTICO: Punto y coma (;) innecesario que termina la declaración.
    /**
     * N°1: Autentica un usuario y genera token JWT
     // ...
    /**
     * N°1: Autentica un usuario y genera token JWT
     *
     * @param request Credenciales (email y password)
     * @return Respuesta con token JWT y datos del usuario
     */
    public AuthResponse login(UserDTO.LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        // 1. Autentica las credenciales
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Busca el usuario en la base de datos
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario no encontrado después de autenticación exitosa"
                ));

        // 3. Valida que el usuario esté activo
        if (!user.getActive()) {
            throw new IllegalStateException("Usuario desactivado");
        }

        log.info("User authenticated successfully: {}", user.getEmail());

        // 4. Genera token JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .toList());

        String jwtToken = jwtService.generateToken(extraClaims, user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(userMapper.userToResponse(user))
                .build();
    }

    /**
     * N°1: Valida un token JWT y retorna información del usuario
     */
    @Transactional(readOnly = true)
    public UserDTO.Response validateToken(String token) {
        log.debug("Validating JWT token");

        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario no encontrado"
                ));

        if (!jwtService.isTokenValid(token, user)) {
            throw new IllegalStateException("Token inválido o expirado");
        }

        return userMapper.userToResponse(user);
    }

    /**
     * DTO interno para respuestas de autenticación
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private UserDTO.Response user;
    }
}
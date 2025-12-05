package com.taskmanager.controller;

import com.taskmanager.dto.UserDTO;
import com.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST de Autenticación
 *
 * CUMPLE REQUERIMIENTO N°1: Autenticación de usuarios
 *
 * Endpoints públicos para:
 * - Registro de usuarios
 * - Login (generación de token JWT)
 * - Validación de token
 *
 * ResponseEntity: Permite controlar completamente la respuesta HTTP
 * (status code, headers, body)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    private final AuthService authService;

    /**
     * N°1: Endpoint de registro
     * POST /api/auth/register
     *
     * @param request Datos del nuevo usuario (validados con @Valid)
     * @return ResponseEntity con token JWT y datos del usuario
     *
     * Status codes:
     * - 201 CREATED: Usuario creado exitosamente
     * - 400 BAD REQUEST: Datos inválidos o email duplicado
     */
    @PostMapping("/register")
    public ResponseEntity<AuthService.AuthResponse> register(
            @Valid @RequestBody UserDTO.RegisterRequest request) {
        log.info("POST /api/auth/register - Email: {}", request.getEmail());

        try {
            AuthService.AuthResponse response = authService.register(request);

            // Retorna 201 CREATED con el body
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException e) {
            // Email duplicado u otro error de validación
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    /**
     * N°1: Endpoint de login
     * POST /api/auth/login
     *
     * @param request Credenciales (email y password)
     * @return ResponseEntity con token JWT y datos del usuario
     *
     * Status codes:
     * - 200 OK: Login exitoso
     * - 401 UNAUTHORIZED: Credenciales inválidas
     */
    @PostMapping("/login")
    public ResponseEntity<AuthService.AuthResponse> login(
            @Valid @RequestBody UserDTO.LoginRequest request) {
        log.info("POST /api/auth/login - Email: {}", request.getEmail());

        try {
            AuthService.AuthResponse response = authService.login(request);

            // Retorna 200 OK con el body
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Credenciales inválidas o usuario desactivado
            log.error("Login failed for email {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    /**
     * N°1: Endpoint para obtener usuario actual
     * GET /api/auth/me
     *
     * Requiere autenticación (token JWT en header Authorization)
     *
     * @return ResponseEntity con datos del usuario autenticado
     *
     * Status codes:
     * - 200 OK: Usuario encontrado
     * - 401 UNAUTHORIZED: Token inválido o no proporcionado
     */
    // AuthController.java - getCurrentUser (CORREGIDO)
    @GetMapping("/me")
    public ResponseEntity<UserDTO.Response> getCurrentUser() {
        log.debug("GET /api/auth/me");

        try {
            // 1. Obtiene la autenticación del SecurityContext
            Authentication authentication = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                // Si no está autenticado o es anónimo, denegar
                log.warn("Attempt to access /me without proper authentication.");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .build();
            }

            // 2. Extrae el email (el username en Spring Security)
            String email = authentication.getName();
            log.debug("Getting user details for email: {}", email);

            // 3. Obtiene los datos completos del usuario usando el email
            // Necesitarás agregar un método en AuthService que busque por email.
            UserDTO.Response user = authService.getUserDetailsByEmail(email);

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
        // 💡 IMPORTANTE: Elimina el método privado extractTokenFromContext() o asegúrate que no se use.
    }

    /**
     * N°1: Endpoint para validar token
     * POST /api/auth/validate
     *
     * @params  token Token JWT a validar
     * @return ResponseEntity con datos del usuario si el token es válido
     *
     * Status codes:
     * - 200 OK: Token válido
     * - 401 UNAUTHORIZED: Token inválido o expirado
     */
    @PostMapping("/validate")
    public ResponseEntity<UserDTO.Response> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        log.debug("POST /api/auth/validate");

        try {
            // Extrae el token del header (remueve "Bearer ")
            String token = authHeader.substring(7);

            UserDTO.Response user = authService.validateToken(token);
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    /**
     * Método helper para extraer el token del contexto
     */
    private String extractTokenFromContext() {
        // En un escenario real, el token estaría en el request
        // Para simplificar, retornamos null y usamos la autenticación del context
        return null;
    }
}
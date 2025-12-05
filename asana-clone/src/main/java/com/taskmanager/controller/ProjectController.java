package com.taskmanager.controller;

import com.taskmanager.dto.InvitationDTO;
import com.taskmanager.dto.ProjectDTO;
import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.security.UserPrincipal;
import com.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST de Proyectos
 *
 * CUMPLE REQUERIMIENTOS:
 * - N°3: Creación de proyectos
 * - N°4: Asignación de colaboradores
 * - N°13: Filtros y búsqueda
 * - N°17: Fecha límite y recordatorios
 *
 * ResponseEntity: Control completo de respuestas HTTP
 * @AuthenticationPrincipal: Inyecta el usuario autenticado
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ProjectController {

    private final ProjectService projectService;

    /**
     * N°3: Crea un nuevo proyecto
     * POST /api/projects
     *
     * @param request Datos del proyecto
     * @param currentUser Usuario autenticado (será el creador)
     * @return ResponseEntity con proyecto creado
     *
     * Status codes:
     * - 201 CREATED: Proyecto creado exitosamente
     * - 400 BAD REQUEST: Datos inválidos
     */
    @PostMapping
    public ResponseEntity<ProjectDTO.Response> createProject(
            @Valid @RequestBody ProjectDTO.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects - Creating project: {} by user: {}",
                request.getName(), currentUser.getEmail());

        try {
            ProjectDTO.Response project = projectService.createProject(
                    request,
                    currentUser.getId()
            );

            // Retorna 201 CREATED con Location header
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(project);

        } catch (Exception e) {
            log.error("Error creating project: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }
    /**
     * N°5: Invita a un miembro al proyecto por email
     * POST /api/projects/{projectId}/invite
     *
     * Solo el creador puede invitar
     * Lanza una excepción si el límite de miembros del plan ha sido alcanzado (403).
     *
     * @param projectId ID del proyecto
     * @param request Objeto con el email a invitar
     * @param currentUser Usuario autenticado (el que invita)
     * @return ResponseEntity con la confirmación de la invitación (o datos de la invitación)
     *
     * Status codes:
     * - 201 CREATED: Invitación enviada exitosamente
     * - 400 BAD REQUEST: Email inválido, usuario ya es miembro o ya tiene invitación pendiente
     * - 403 FORBIDDEN: No es el creador o límite de miembros alcanzado
     * - 404 NOT FOUND: Proyecto no existe
     */
    /**
     * N°5: Invita a un miembro al proyecto por email
     * POST /api/projects/{projectId}/invite
     *
     * 🔥 CORRECCIÓN: Cambiar UserPrincipal por User directamente
     */
    @PostMapping("/{projectId}/invite")
    public ResponseEntity<?> inviteMember(
            @PathVariable Long projectId,
            @RequestBody @Valid ProjectDTO.InviteMemberRequest request,
            @AuthenticationPrincipal User currentUser) { // 🔥 CAMBIO AQUÍ: User en lugar de UserPrincipal

        // 🔥 Validación de autenticación
        if (currentUser == null) {
            log.error("Current user is null - authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado"));
        }

        log.info("POST /api/projects/{}/invite - Inviting {} by user: {}",
                projectId, request.getInvitedEmail(), currentUser.getEmail());

        try {
            InvitationDTO.Response response = projectService.inviteMember(
                    projectId,
                    request,
                    currentUser.getId() // 🔥 Ahora sí funciona porque User tiene getId()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            log.warn("Access denied: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            log.error("Error inviting member: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno del servidor"));
        }
    }
    /**
     * N°3: Obtiene todos los proyectos del usuario autenticado
     * GET /api/projects
     *
     * Incluye proyectos creados y proyectos donde es miembro
     * Usa Query Method sin SQL
     *
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con lista de proyectos
     *
     * Status codes:
     * - 200 OK: Lista retornada (puede estar vacía)
     */
    @GetMapping
    public ResponseEntity<List<ProjectDTO.Response>> getUserProjects(
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects - Fetching projects for user: {}",
                currentUser.getEmail());

        List<ProjectDTO.Response> projects = projectService.getUserProjects(
                currentUser.getId()
        );

        return ResponseEntity.ok(projects);
    }

    /**
     * N°3: Obtiene un proyecto por ID
     * GET /api/projects/{id}
     *
     * @param id ID del proyecto
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con datos del proyecto
     *
     * Status codes:
     * - 200 OK: Proyecto encontrado
     * - 403 FORBIDDEN: Usuario no tiene acceso al proyecto
     * - 404 NOT FOUND: Proyecto no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO.Response> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/{} - User: {}", id, currentUser.getEmail());

        try {
            ProjectDTO.Response project = projectService.getProjectById(
                    id,
                    currentUser.getId()
            );
            return ResponseEntity.ok(project);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            log.warn("User {} attempted to access project {} without permission",
                    currentUser.getEmail(), id);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            log.error("Project not found: {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°3: Actualiza un proyecto
     * PUT /api/projects/{id}
     *
     * Solo el creador puede actualizar el proyecto
     *
     * @param id ID del proyecto
     * @param request Datos a actualizar
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con proyecto actualizado
     *
     * Status codes:
     * - 200 OK: Proyecto actualizado
     * - 403 FORBIDDEN: No es el creador
     * - 404 NOT FOUND: Proyecto no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO.Response> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/projects/{} - Updating by user: {}",
                id, currentUser.getEmail());

        try {
            ProjectDTO.Response project = projectService.updateProject(
                    id,
                    request,
                    currentUser.getId()
            );
            return ResponseEntity.ok(project);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            log.warn("User {} attempted to update project {} without permission",
                    currentUser.getEmail(), id);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            log.error("Error updating project {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°4: Agrega un miembro al proyecto
     * POST /api/projects/{projectId}/members/{userId}
     *
     * Solo el creador puede agregar miembros
     *
     * @param projectId ID del proyecto
     * @param userId ID del usuario a agregar
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con proyecto actualizado
     *
     * Status codes:
     * - 200 OK: Miembro agregado
     * - 403 FORBIDDEN: No es el creador
     * - 404 NOT FOUND: Proyecto o usuario no existe
     */
    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDTO.Response> addMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/projects/{}/members/{} - Adding member by user: {}",
                projectId, userId, currentUser.getEmail());

        try {
            ProjectDTO.Response project = projectService.addMember(
                    projectId,
                    userId,
                    currentUser.getId()
            );
            return ResponseEntity.ok(project);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            log.error("Error adding member: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°4: Remueve un miembro del proyecto
     * DELETE /api/projects/{projectId}/members/{userId}
     *
     * Solo el creador puede remover miembros
     * No se puede remover al creador
     *
     * @param projectId ID del proyecto
     * @param userId ID del usuario a remover
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con proyecto actualizado
     *
     * Status codes:
     * - 200 OK: Miembro removido
     * - 400 BAD REQUEST: Intentando remover al creador
     * - 403 FORBIDDEN: No es el creador
     * - 404 NOT FOUND: Proyecto o usuario no existe
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDTO.Response> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/projects/{}/members/{} - Removing member by user: {}",
                projectId, userId, currentUser.getEmail());

        try {
            ProjectDTO.Response project = projectService.removeMember(
                    projectId,
                    userId,
                    currentUser.getId()
            );
            return ResponseEntity.ok(project);

        } catch (IllegalArgumentException e) {
            // Intentando remover al creador
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            log.error("Error removing member: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    /**
     * N°13: Busca proyectos por palabra clave
     * GET /api/projects/search?keyword=texto
     *
     * Usa Query Method sin SQL
     *
     * @param keyword Palabra clave
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con lista de proyectos encontrados
     *
     * Status codes:
     * - 200 OK: Búsqueda realizada (puede estar vacía)
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProjectDTO.Summary>> searchProjects(
            @RequestParam String keyword,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/search?keyword={} - User: {}",
                keyword, currentUser.getEmail());

        List<ProjectDTO.Summary> projects = projectService.searchProjects(
                keyword,
                currentUser.getId()
        );

        return ResponseEntity.ok(projects);
    }

    /**
     * N°13: Filtra proyectos por estado
     * GET /api/projects/by-status?status=ACTIVE
     *
     * Usa Query Method sin SQL
     * Estados: ACTIVE, ON_HOLD, COMPLETED, CANCELLED
     *
     * @param status Estado del proyecto
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con lista de proyectos
     *
     * Status codes:
     * - 200 OK: Filtro aplicado
     * - 400 BAD REQUEST: Estado inválido
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<ProjectDTO.Summary>> getProjectsByStatus(
            @RequestParam String status,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/by-status?status={} - User: {}",
                status, currentUser.getEmail());

        try {
            Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status);

            List<ProjectDTO.Summary> projects = projectService.getProjectsByStatus(
                    projectStatus,
                    currentUser.getId()
            );

            return ResponseEntity.ok(projects);

        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }
    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ProjectDTO.Response> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestBody ProjectDTO.StatusUpdateRequest request,
            @AuthenticationPrincipal(expression = "id") Long currentUserId
    ) {
        log.info("PATCH /api/projects/{}/status - New Status: {}", projectId, request.getStatus());

        ProjectDTO.Response response = projectService.updateProjectStatus(
                projectId,
                // Pasamos la cadena (String) del estado al servicio
                request.getStatus(),
                currentUserId
        );

        return ResponseEntity.ok(response);
    }
    /**
     * N°17: Obtiene proyectos con deadline próximo
     * GET /api/projects/upcoming-deadlines?days=7
     *
     * Usa Query Method sin SQL
     *
     * @param days Cantidad de días a futuro (default 7)
     * @param currentUser Usuario autenticado
     * @return ResponseEntity con lista de proyectos
     *
     * Status codes:
     * - 200 OK: Lista retornada
     */
    @GetMapping("/upcoming-deadlines")
    public ResponseEntity<List<ProjectDTO.Summary>> getUpcomingDeadlines(
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/projects/upcoming-deadlines?days={} - User: {}",
                days, currentUser.getEmail());

        List<ProjectDTO.Summary> projects = projectService.getUpcomingDeadlines(
                currentUser.getId(),
                days
        );

        return ResponseEntity.ok(projects);
    }

    // --- CORRECCIÓN CRÍTICA: CAMBIADO DE @PostMapping a @PutMapping ---
    /**
     * N°3: Archiva un proyecto
     * PUT /api/projects/{id}/archive // 👈 MÉTODO CORREGIDO
     *
     * Solo el creador puede archivar
     *
     * @param id ID del proyecto
     * @param currentUser Usuario autenticado
     * @return ResponseEntity sin contenido
     *
     * Status codes:
     * - 204 NO CONTENT: Proyecto archivado
     * - 403 FORBIDDEN: No es el creador
     * - 404 NOT FOUND: Proyecto no existe
     */
    @PutMapping("/{id}/archive") // 👈 ¡ESTO SOLUCIONA EL ERROR 500!
    public ResponseEntity<Void> archiveProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/projects/{}/archive - User: {}",
                id, currentUser.getEmail());

        try {
            projectService.archiveProject(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            // Este catch probablemente captura ResourceNotFoundException (que debería ser 404)
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
    @PutMapping("/{id}/unarchive")
    public ResponseEntity<Void> unarchiveProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        // ... Llama al service
        projectService.unarchiveProject(id, currentUser.getId());
        return ResponseEntity.noContent().build(); // 204
    }
    /**
     * N°3: Elimina un proyecto permanentemente
     * DELETE /api/projects/{id}
     *
     * Solo el creador puede eliminar
     *
     * @param id ID del proyecto
     * @param currentUser Usuario autenticado
     * @return ResponseEntity sin contenido
     *
     * Status codes:
     * - 204 NO CONTENT: Proyecto eliminado
     * - 403 FORBIDDEN: No es el creador
     * - 404 NOT FOUND: Proyecto no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/projects/{} - User: {}",
                id, currentUser.getEmail());

        try {
            projectService.deleteProject(id, currentUser.getId());
            return ResponseEntity.noContent().build();

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
}
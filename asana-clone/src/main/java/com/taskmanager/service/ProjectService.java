// ===================================
// PROJECT SERVICE - CORREGIDO CON SINCRONIZACIÓN
// ===================================
package com.taskmanager.service;

import com.taskmanager.Repositorios.InvitationRepository;
import com.taskmanager.Repositorios.TaskRepository;
import com.taskmanager.dto.InvitationDTO;
import com.taskmanager.dto.ProjectDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.InvitationMapper;
import com.taskmanager.mapper.ProjectMapper;
import com.taskmanager.model.Invitation;
import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.model.ActivityLog;
import com.taskmanager.Repositorios.ProjectRepository;
import com.taskmanager.Repositorios.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {
    private final EntityManager entityManager;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final SubscriptionService subscriptionService;
    private final ActivityLogService activityLogService;
    private final InvitationRepository invitationRepository;
    private final NotificationService notificationService;
    private final InvitationMapper invitationMapper;
    private final TaskRepository taskRepository;
    public ProjectDTO.Response createProject(ProjectDTO.CreateRequest request, Long createdByUserId) {
        log.info("Creating new project: {} by user ID: {}", request.getName(), createdByUserId);

        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + createdByUserId
                ));
        projectRepository.flush();
        entityManager.clear();
        // Verificar límite de proyectos de la suscripción
        if (!subscriptionService.canCreateProject(createdByUserId)) {
            // MEJORA: Lanzar AccessDeniedException o una excepción específica de negocio (ej. SubscriptionLimitReachedException).
            // Si lanzas AccessDeniedException, el Controller mapeará al 403, que es un error común para límites/permisos.
            throw new AccessDeniedException(
                    "Has alcanzado el límite de proyectos de tu plan. " +
                            "Actualiza tu suscripción para crear más proyectos."
            );
        }

        Project project = projectMapper.createRequestToProject(request);
        project.setCreatedBy(creator);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            Set<User> members = request.getMemberIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Usuario no encontrado con ID: " + userId
                            )))
                    .collect(Collectors.toSet());

            members.add(creator);
            project.setMembers(members);
        } else {
            project.setMembers(Set.of(creator));
        }

        Project savedProject = projectRepository.save(project);

        // Actualizar contador de proyectos en suscripción
        subscriptionService.updateProjectCount(createdByUserId, 1);

        // Registrar actividad
        activityLogService.logActivity(
                savedProject,
                creator,
                ActivityLog.ActivityType.PROJECT_CREATED,
                "PROJECT",
                savedProject.getId(),
                savedProject.getName()
        );

        try {
            String title = "🎉 Proyecto Creado: " + savedProject.getName();
            String message = String.format("Has creado el proyecto '%s'. Empieza a añadir tareas y miembros.", savedProject.getName());
            String actionUrl = "/projects/" + savedProject.getId();

            notificationService.createNotification(
                    creator, // Destinatario (el creador)
                    creator, // Actor
                    com.taskmanager.model.Notification.NotificationType.PROJECT_CREATED, // Tipo
                    title,
                    message,
                    "PROJECT",
                    savedProject.getId(),
                    actionUrl,
                    com.taskmanager.model.Notification.NotificationPriority.NORMAL // Prioridad
            );
            log.info("Notification created for project creator: {}", creator.getEmail());
        } catch (Exception e) {
            // No hacemos rollback si la notificación falla, solo registramos el error
            log.error("Failed to create project creation notification: {}", e.getMessage(), e);
        }


        log.info("Project created successfully with ID: {}", savedProject.getId());

        return projectMapper.projectToResponse(savedProject);
    }
    // 🔥 NUEVO: Envía una invitación para unirse a un proyecto usando el email.
// Implementa la restricción de miembros por suscripción.
    // ========== ProjectService.java (Método inviteMember CORREGIDO) ==========

    // 🔥 CORRECCIÓN: Cambiar 'void' a 'InvitationDTO.Response'
    public InvitationDTO.Response inviteMember(Long projectId, ProjectDTO.InviteMemberRequest request, Long requestingUserId) {

        // 🔥 LÓGICA DE VALIDACIÓN (Correcta)
        log.info("User ID: {} is inviting {} to project ID: {}",
                requestingUserId, request.getInvitedEmail(), projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + projectId));

        User sender = project.getCreatedBy(); // El creador/remitente

        // 1. VALIDACIÓN DE PERMISOS: Solo el creador puede invitar
        if (!sender.getId().equals(requestingUserId)) {
            throw new AccessDeniedException("Solo el creador puede invitar miembros a este proyecto");
        }

        // 2. VALIDACIÓN DE LÍMITE DE MIEMBROS POR SUSCRIPCIÓN
        int memberLimit = subscriptionService.getMemberLimit(requestingUserId);
        int currentMembers = project.getMembers().size();

        if (currentMembers >= memberLimit) {
            throw new AccessDeniedException(String.format(
                    "Has alcanzado el límite de %d miembros de tu plan de suscripción. Actualiza tu plan.",
                    memberLimit
            ));
        }

        // 3. VALIDACIÓN DE DUPLICADOS Y ESTADO (Usuario ya es miembro o ya invitado)
        String invitedEmail = request.getInvitedEmail().toLowerCase();

        User invitedUser = userRepository.findByEmail(invitedEmail).orElse(null);

        if (invitedUser != null && project.hasMember(invitedUser)) {
            throw new IllegalArgumentException("El usuario ya es miembro de este proyecto.");
        }

        // Validar si ya hay una invitación PENDIENTE
        Optional<Invitation> existingInvitation = invitationRepository
                .findByInvitedEmailIgnoreCaseAndProjectIdAndStatus(
                        invitedEmail,
                        projectId,
                        Invitation.InvitationStatus.PENDING
                );

        if (existingInvitation.isPresent()) {
            throw new IllegalArgumentException("Ya existe una invitación pendiente para este usuario en este proyecto.");
        }

        // 4. CREAR Y GUARDAR LA INVITACIÓN
        Invitation invitation = Invitation.builder()
                .project(project)
                .sender(sender)
                .invitedEmail(invitedEmail)
                .status(Invitation.InvitationStatus.PENDING)
                .build();
        Invitation savedInvitation = invitationRepository.save(invitation);

        // 5. ENVIAR NOTIFICACIÓN
        notificationService.sendProjectInvitationNotification(
                invitedUser,
                invitedEmail,
                sender.getFullName(),
                project.getName(),
                projectId,
                savedInvitation.getId()
        );

        // 6. Registrar actividad
        activityLogService.logActivity(
                project,
                sender,
                ActivityLog.ActivityType.MEMBER_INVITED,
                "USER",
                null,
                invitedEmail
        );

        log.info("Invitation sent to {} for project ID: {}", invitedEmail, projectId);

        // 7. DEVOLVER DTO (Ahora válido por la firma corregida)
        return invitationMapper.toResponseDTO(savedInvitation);
    }
    /**
     * Permite al usuario responder a una invitación.
     * Si acepta, lo añade como miembro (si existe).
     */
    public void respondToInvitation(Long invitationId, String status, Long respondingUserId) {
        log.info("User ID: {} responding to invitation ID: {} with status: {}",
                respondingUserId, invitationId, status);

        Invitation invitation = invitationRepository.findById(invitationId) // 🔥 DESCOMENTADO
                .orElseThrow(() -> new ResourceNotFoundException("Invitación no encontrada con ID: " + invitationId));

        User responder = userRepository.findById(respondingUserId) // 🔥 DESCOMENTADO
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + respondingUserId));

        // 1. VALIDAR ESTADO ACTUAL
        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Esta invitación ya ha sido respondida o cancelada.");
        }

        // 2. VALIDAR QUE EL RESPONSABLE SEA EL INVITADO
        if (!invitation.getInvitedEmail().equalsIgnoreCase(responder.getEmail())) {
            throw new AccessDeniedException("Solo el destinatario puede responder a esta invitación");
        }

        // 3. PROCESAR RESPUESTA
        Project project = invitation.getProject();
        User projectCreator = project.getCreatedBy();
        String responseStatus = status.toUpperCase();

        if ("ACCEPTED".equals(responseStatus)) {
            // A. Añadir al proyecto (solo si no es ya miembro, lo cual es doble verificación)
            if (!project.hasMember(responder)) {
                project.addMember(responder);
                projectRepository.save(project);
            } else {
                // Esto podría pasar si el creador lo añadió manualmente después de enviar la invitación.
                log.warn("User {} already member of project {}. Skipping addMember.", responder.getEmail(), project.getName());
            }

            // B. Actualizar estado y registrar
            invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
            invitation.setRespondedAt(LocalDateTime.now());
            invitationRepository.save(invitation); // 🔥 DESCOMENTADO

            // C. Notificar al creador del proyecto
            notificationService.sendInvitationAcceptedNotification( // 🔥 DESCOMENTADO
                    projectCreator,
                    responder.getEmail(),
                    project.getName()
            );

            // D. Registrar actividad
            activityLogService.logActivity( // 🔥 DESCOMENTADO
                    project, responder, ActivityLog.ActivityType.MEMBER_ADDED,
                    "USER", respondingUserId, responder.getFullName()
            );

            log.info("Invitation accepted. User {} added to project {}", responder.getEmail(), project.getName());

        } else if ("REJECTED".equals(responseStatus)) {
            // A. Actualizar estado
            invitation.setStatus(Invitation.InvitationStatus.REJECTED);
            invitation.setRespondedAt(LocalDateTime.now());
            invitationRepository.save(invitation); // 🔥 DESCOMENTADO

            // B. Notificar al creador del proyecto (con la carita triste 😢)
            notificationService.sendInvitationRejectedNotification( // 🔥 DESCOMENTADO
                    projectCreator,
                    responder.getEmail(),
                    project.getName()
            );

            log.info("Invitation rejected by user {}", responder.getEmail());
        } else {
            throw new IllegalArgumentException("Estado de respuesta inválido: " + status);
        }
    }
    public ProjectDTO.Response updateProject(Long id, ProjectDTO.UpdateRequest request, Long userId) {
        log.info("Updating project ID: {} by user: {}", id, userId);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));

        // Validar dueño
        if (!project.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el creador puede actualizar este proyecto");
        }

        // Aplicar cambios
        projectMapper.updateProjectFromDto(request, project);

        Project updated = projectRepository.save(project);

        return projectMapper.projectToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO.Response> getUserProjects(Long userId) {
        log.debug("Fetching projects for user ID: {}", userId);

        return projectRepository.findAllByUserId(userId).stream()
                .map(projectMapper::projectToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDTO.Response getProjectById(Long id, Long userId) {
        log.debug("Fetching project ID: {} for user ID: {}", id, userId);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id
                ));

        validateUserAccess(project, userId);

        return projectMapper.projectToResponse(project);
    }

    // ========== ProjectService.java - Método updateProjectStatus ==========

    /**
     * 🔥 TRANSACCIÓN SEPARADA PARA EVITAR ROLLBACK EN CASCADA
     * Actualiza el estado del proyecto sin afectar la transacción padre
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProjectStatusAsync(Long projectId, String newStatus, Long userId) {
        log.info("Updating project status asynchronously - Project ID: {}, New Status: {}",
                projectId, newStatus);

        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Proyecto no encontrado con ID: " + projectId
                    ));

            Project.ProjectStatus oldStatus = project.getStatus();

            // Convertir String a Enum
            Project.ProjectStatus newEnumStatus;
            try {
                newEnumStatus = Project.ProjectStatus.valueOf(newStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Estado de proyecto inválido: {}", newStatus);
                return; // No lanzar excepción, solo registrar warning
            }

            // Si el estado es el mismo, no hacer nada
            if (oldStatus == newEnumStatus) {
                log.debug("Project status is already {}, skipping update", newStatus);
                return;
            }

            project.setStatus(newEnumStatus);
            projectRepository.save(project);

            // Registrar actividad (opcional)
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && activityLogService != null) {
                    activityLogService.logActivity(
                            project,
                            user,
                            ActivityLog.ActivityType.PROJECT_STATUS_CHANGED,
                            "PROJECT",
                            projectId,
                            "Status changed from " + oldStatus + " to " + newStatus
                    );
                }
            } catch (Exception e) {
                log.warn("Could not log activity: {}", e.getMessage());
            }

            log.info("Project status successfully updated from {} to {}", oldStatus, newStatus);

        } catch (Exception e) {
            log.error("Error updating project status: {}", e.getMessage(), e);
            // No relanzar la excepción para evitar afectar la transacción padre
        }
    }

    /**
     * Método original con transacción normal (mantener para otros usos)
     */
    @Transactional
    public ProjectDTO.Response updateProjectStatus(Long projectId, String newStatus, Long userId) {
        log.info("Updating project status - Project ID: {}, New Status: {}", projectId, newStatus);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + projectId
                ));

        Project.ProjectStatus oldStatus = project.getStatus();

        Project.ProjectStatus newEnumStatus;
        try {
            newEnumStatus = Project.ProjectStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de proyecto inválido: " + newStatus);
        }

        if (oldStatus == newEnumStatus) {
            return projectMapper.projectToResponse(project);
        }

        project.setStatus(newEnumStatus);
        Project updatedProject = projectRepository.save(project);

        log.info("Project status successfully updated from {} to {}", oldStatus, newStatus);
        return projectMapper.projectToResponse(updatedProject);
    }
    public ProjectDTO.Response addMember(Long projectId, Long userId, Long requestingUserId) {
        log.info("Adding user ID: {} to project ID: {} by user ID: {}",
                userId, projectId, requestingUserId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + projectId
                ));

        if (!project.getCreatedBy().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("Solo el creador puede agregar miembros");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        project.addMember(userToAdd);

        Project updatedProject = projectRepository.save(project);

        // Registrar actividad
        User requester = userRepository.findById(requestingUserId).orElse(null);
        if (requester != null) {
            activityLogService.logActivity(
                    updatedProject,
                    requester,
                    ActivityLog.ActivityType.MEMBER_ADDED,
                    "USER",
                    userId,
                    userToAdd.getFullName()
            );
        }

        log.info("Member added successfully to project ID: {}", projectId);

        return projectMapper.projectToResponse(updatedProject);
    }

    public ProjectDTO.Response removeMember(Long projectId, Long userId, Long requestingUserId) {
        log.info("Removing user ID: {} from project ID: {} by user ID: {}",
                userId, projectId, requestingUserId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + projectId
                ));

        if (!project.getCreatedBy().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("Solo el creador puede remover miembros");
        }

        if (project.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("No se puede remover al creador del proyecto");
        }

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        project.removeMember(userToRemove);

        Project updatedProject = projectRepository.save(project);

        // Registrar actividad
        User requester = userRepository.findById(requestingUserId).orElse(null);
        if (requester != null) {
            activityLogService.logActivity(
                    updatedProject,
                    requester,
                    ActivityLog.ActivityType.MEMBER_REMOVED,
                    "USER",
                    userId,
                    userToRemove.getFullName()
            );
        }

        log.info("Member removed successfully from project ID: {}", projectId);

        return projectMapper.projectToResponse(updatedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO.Summary> searchProjects(String keyword, Long userId) {
        log.debug("Searching projects with keyword: {} for user ID: {}", keyword, userId);

        return projectRepository.findByNameContainingIgnoreCase(keyword).stream()
                .filter(project -> hasUserAccess(project, userId))
                .map(projectMapper::projectToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO.Summary> getProjectsByStatus(
            Project.ProjectStatus status, Long userId) {
        log.debug("Fetching projects with status: {} for user ID: {}", status, userId);

        return projectRepository.findByStatus(status).stream()
                .filter(project -> hasUserAccess(project, userId))
                .map(projectMapper::projectToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO.Summary> getUpcomingDeadlines(Long userId, int days) {
        log.debug("Fetching projects with deadlines in next {} days for user ID: {}",
                days, userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(days);

        return projectRepository.findProjectsWithUpcomingDeadline(now, endDate).stream()
                .filter(project -> hasUserAccess(project, userId))
                .map(projectMapper::projectToSummary)
                .collect(Collectors.toList());
    }

    public void archiveProject(Long id, Long userId) {
        log.info("Archiving project ID: {} by user ID: {}", id, userId);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id
                ));

        if (!project.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el creador puede archivar el proyecto");
        }

        project.setArchived(true);
        projectRepository.save(project);

        // Actualizar contador de proyectos activos (decrementa en 1)
        subscriptionService.updateProjectCount(userId, -1);

        // Registrar actividad
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            activityLogService.logActivity(
                    project,
                    user,
                    ActivityLog.ActivityType.PROJECT_ARCHIVED,
                    "PROJECT",
                    id,
                    project.getName()
            );
        }

        log.info("Project archived successfully with ID: {}", id);
    }

    /**
     * N°3: Desarchiva un proyecto (NUEVO MÉTODO)
     */
    public void unarchiveProject(Long id, Long userId) {
        log.info("Unarchiving project ID: {} by user ID: {}", id, userId);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id
                ));

        // Validación de seguridad: Solo el creador puede desarchivar
        if (!project.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el creador puede desarchivar el proyecto");
        }

        // El proyecto ya debe estar archivado para desarchivarlo
        if (!project.getArchived()) {
            log.warn("Project ID: {} is already unarchived.", id);
            return; // O lanzar una excepción si se prefiere un control estricto.
        }

        project.setArchived(false);
        projectRepository.save(project);

        // Actualizar contador de proyectos activos (incrementa en 1)
        subscriptionService.updateProjectCount(userId, 1);

        // Registrar actividad
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            activityLogService.logActivity(
                    project,
                    user,
                    ActivityLog.ActivityType.PROJECT_UNARCHIVED, // Asumo que tienes este tipo de actividad
                    "PROJECT",
                    id,
                    project.getName()
            );
        }

        log.info("Project unarchived successfully with ID: {}", id);
    }
   // ========== MÉTODO CORREGIDO PARA ProjectService.java ==========

/**
 * 🔥 MÉTODO CORREGIDO: Elimina proyecto con todas sus dependencias
 */
@Transactional
public void deleteProject(Long id, Long userId) {
    log.info("Starting deletion process for project ID: {} by user ID: {}", id, userId);

    Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Proyecto no encontrado con ID: " + id
            ));

    // 1. Verificar Permisos
    if (!project.getCreatedBy().getId().equals(userId)) {
        throw new AccessDeniedException("Solo el creador puede eliminar el proyecto");
    }

    // 2. 🔥 ELIMINAR DEPENDENCIAS EN EL ORDEN CORRECTO

    // a) Eliminar ActivityLogs (CRÍTICO - Causa del error 1451)
    activityLogService.deleteAllByProjectId(id);
    log.debug("Deleted all activity logs for project ID: {}", id);

    // b) Eliminar Tareas
    taskRepository.deleteAllByProjectId(id);
    log.debug("Deleted all tasks associated with project ID: {}", id);

    // c) Eliminar Invitaciones
    invitationRepository.deleteAllByProjectId(id);
    log.debug("Deleted all pending invitations for project ID: {}", id);

    // d) Opcional: Eliminar Procesos (si no se eliminan en cascada)
    // processRepository.deleteAllByProjectId(id);

    // 3. Eliminar el Proyecto Principal
    projectRepository.delete(project);

    // 4. Actualizar contador de suscripción
    if (!project.getArchived()) {
        subscriptionService.updateProjectCount(userId, -1);
    }

    log.info("Project deleted successfully with ID: {}", id);
}

    private void validateUserAccess(Project project, Long userId) {
        if (!hasUserAccess(project, userId)) {
            throw new AccessDeniedException("No tienes acceso a este proyecto");
        }
    }

    private boolean hasUserAccess(Project project, Long userId) {
        return project.getCreatedBy().getId().equals(userId) ||
                project.getMembers().stream()
                        .anyMatch(member -> member.getId().equals(userId));
    }
}

    package com.taskmanager.service;

    import com.taskmanager.dto.TaskDTO;
    import com.taskmanager.exception.ResourceNotFoundException;
    import com.taskmanager.mapper.TaskMapper;
    import com.taskmanager.model.*;
    import com.taskmanager.Repositorios.*;
    import com.taskmanager.model.Process;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.security.access.AccessDeniedException;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Set;
    import java.util.stream.Collectors;

    /**
     * Servicio de Gestión de Tareas
     * * Versión FINAL y CORREGIDA, incluye lógica Kanban, assignUser, unassignUser y Subtareas Anidadas.
     */
    @Service
    @RequiredArgsConstructor
    @Slf4j
    @Transactional
    public class TaskService {

        private final TaskRepository taskRepository;
        private final ProcessRepository processRepository;
        private final ProjectRepository projectRepository;
        private final UserRepository userRepository;
        private final TaskMapper taskMapper;
        private final SubscriptionService subscriptionService;
        private final ProjectService projectService;
        // =================================================================================
        // MÉTODOS DE CREACIÓN, LECTURA Y ACTUALIZACIÓN
        // =================================================================================

        /**
         * N°X: Obtiene todas las tareas asignadas al usuario actual.
         * Este método se usa en la página "Mis Tareas".
         */
        @Transactional(readOnly = true)
        public List<TaskDTO.Response> getMyAssignedTasks(Long userId) {
            log.debug("Fetching tasks assigned to user ID: {}", userId);

            List<Task> tasks = taskRepository.findTasksAssignedToUserWithProject(userId);

            return tasks.stream()
                    .map(taskMapper::taskToResponse)
                    .collect(Collectors.toList());
        }

        /**
         * N°6: Crea una nueva tarea (REINCORPORADA LÓGICA DE SUBTAREAS)
         */
        public TaskDTO.Response createTask(
                Long projectId,
                TaskDTO.CreateRequest request,
                Long userId) {
            log.info("Creating task '{}' in project ID: {}", request.getTitle(), projectId);

            // 1. Validaciones y obtención de recursos (Proyecto, Proceso, Creador)
            Project project = validateProjectAccess(projectId, userId);

            Process process = processRepository.findById(request.getProcessId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Proceso no encontrado con ID: " + request.getProcessId()
                    ));

            if (!process.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("El proceso no pertenece al proyecto");
            }

            User creator = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con ID: " + userId
                    ));

            // 2. CREACIÓN DE LA TAREA PRINCIPAL (PADRE)
            Task task = taskMapper.createRequestToTask(request);
            task.setProcess(process);
            task.setProject(project);
            task.setCreatedBy(creator);

            // Bloque para manejar si la tarea que se crea es hija de otra tarea existente
            if (request.getParentTaskId() != null) {
                Task parentTask = taskRepository.findById(request.getParentTaskId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Tarea padre no encontrada con ID: " + request.getParentTaskId()
                        ));

                if (!parentTask.getProject().getId().equals(projectId)) {
                    throw new IllegalArgumentException(
                            "La tarea padre no pertenece al mismo proyecto"
                    );
                }

                task.setParentTask(parentTask);
            }

            // Manejo de asignados a la Tarea Principal
            if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
                Set<User> assignees = request.getAssigneeIds().stream()
                        .map(id -> userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Usuario no encontrado con ID: " + id
                                )))
                        .collect(Collectors.toSet());

                task.setAssignees(assignees);
            }

            // Manejo de la posición inicial
            long taskCount = taskRepository.countByProcessId(process.getId());
            task.setPosition((int) taskCount);

            // 3. GUARDAR LA TAREA PRINCIPAL para obtener su ID
            Task savedTask = taskRepository.save(task);
            log.info("Task created successfully with ID: {}", savedTask.getId());

            // 4. LÓGICA DE CREACIÓN DE SUBTAREAS (SI SE ENVIARON EN LA REQUEST)
            if (request.getSubtasks() != null && !request.getSubtasks().isEmpty()) {
                log.info("Processing {} subtasks for task ID: {}", request.getSubtasks().size(), savedTask.getId());

                int subtaskPosition = 0;

                for (TaskDTO.SubtaskRequest subtaskRequest : request.getSubtasks()) {

                    if (subtaskRequest.getTitle() == null || subtaskRequest.getTitle().trim().isEmpty()) {
                        continue; // Saltar subtareas sin título
                    }

                    Task subtask = new Task();
                    subtask.setTitle(subtaskRequest.getTitle());
                    // Hereda propiedades del padre
                    subtask.setProcess(process);
                    subtask.setProject(project);
                    subtask.setCreatedBy(creator);
                    subtask.setDueDate(subtaskRequest.getDueDate());

                    // Establecer la relación jerárquica
                    subtask.setParentTask(savedTask);

                    // Las subtareas se inicializan en TODO y tienen su propia posición.
                    subtask.setStatus(Task.TaskStatus.TODO);
                    subtask.setPosition(subtaskPosition++);

                    // Asignar usuarios específicos para la subtarea
                    if (subtaskRequest.getAssigneeIds() != null && !subtaskRequest.getAssigneeIds().isEmpty()) {
                        Set<User> subtaskAssignees = subtaskRequest.getAssigneeIds().stream()
                                .map(id -> userRepository.findById(id)
                                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id)))
                                .collect(Collectors.toSet());
                        subtask.setAssignees(subtaskAssignees);
                    }

                    taskRepository.save(subtask);
                    log.debug("Subtask created: {}", subtask.getTitle());
                }

                // 5. Recalcular el porcentaje de completado de la tarea principal
                updateParentTaskCompletion(savedTask);
            }

            // Si la tarea principal tiene un padre (es una subtarea creada manualmente),
            // actualizamos el progreso del abuelo.
            if (savedTask.getParentTask() != null) {
                updateParentTaskCompletion(savedTask.getParentTask());
            }

            // La respuesta es la tarea principal recién creada
            return taskMapper.taskToResponse(savedTask);
        }

        // ... (El resto de los métodos del servicio se mantienen igual) ...

        /**
         * N°6: Obtiene todas las tareas de un proyecto
         */
        @Transactional(readOnly = true)
        public List<TaskDTO.Response> getProjectTasks(Long projectId, Long userId) {
            log.debug("Fetching tasks for project ID: {}", projectId);

            List<Task> tasks;

            // 1. Verificar si el proyecto existe (lanza 404 en lugar de 403 si no existe)
            if (!projectRepository.existsById(projectId)) {
                throw new ResourceNotFoundException("Proyecto no encontrado con ID: " + projectId);
            }

            // 2. Si el usuario es ADMIN, ignorar todas las restricciones y obtener todas las tareas
            if (this.isAdmin(userId)) {
                tasks = taskRepository.findByProjectId(projectId);
            }
            // 3. Usar la consulta de acceso inclusiva (Miembro/Creador de Tarea/Creador de Proyecto)
            else {
                // Requiere que 'findTasksByProjectAndUserAccess' exista en TaskRepository
                tasks = taskRepository.findTasksByProjectAndUserAccess(projectId, userId);
            }

            return tasks.stream()
                    .map(taskMapper::taskToResponse)
                    .collect(Collectors.toList());
        }

        /**
         * N°6: Obtiene una tarea por ID
         */
        @Transactional(readOnly = true)
        public TaskDTO.Response getTaskById(Long id, Long userId) {
            log.debug("Fetching task ID: {}", id);

            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tarea no encontrada con ID: " + id
                    ));

            // Uso de validateProjectAccess para operaciones de LECTURA DE UNA TAREA ESPECÍFICA
            validateProjectAccess(task.getProject().getId(), userId);

            return taskMapper.taskToResponse(task);
        }

        /**
         * N°6: Actualiza una tarea
         */
        public TaskDTO.Response updateTask(
                Long id,
                TaskDTO.UpdateRequest request,
                Long userId) {
            log.info("Updating task ID: {}", id);

            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tarea no encontrada con ID: " + id
                    ));

            // Uso de validateProjectAccess para operaciones de ESCRITURA
            validateProjectAccess(task.getProject().getId(), userId);

            Task.TaskStatus oldStatus = task.getStatus();

            taskMapper.updateTaskFromDto(request, task);

            if (request.getStatus() != null) {
                Task.TaskStatus newStatus = Task.TaskStatus.valueOf(request.getStatus());
                if (newStatus == Task.TaskStatus.DONE && oldStatus != Task.TaskStatus.DONE) {
                    task.markAsCompleted();
                }
            }

            Task updatedTask = taskRepository.save(task);
            log.info("Task updated successfully with ID: {}", updatedTask.getId());

            if (updatedTask.getParentTask() != null) {
                updateParentTaskCompletion(updatedTask.getParentTask());
            }

            return taskMapper.taskToResponse(updatedTask);
        }

        // =================================================================================
        // LÓGICA DE MOVIMIENTO KANBAN
        // =================================================================================

        /**
         * N°5: Mueve una tarea a otro proceso (columna Kanban)
         */
        /**
         * N°5: Mueve una tarea a otro proceso (columna Kanban)
         */

        @Transactional
        public TaskDTO.Response moveTask(
                Long taskId,
                TaskDTO.MoveRequest request,
                Long userId) {

            log.info("MOVE_REQUEST - TaskId: {}, TargetProcessId: {}, Position: {}",
                    taskId, request.getTargetProcessId(), request.getPosition());

            Long targetProcessId = request.getTargetProcessId();
            Integer newPosition = request.getPosition();

            // 1. Obtener Tarea y Proceso Destino
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + taskId));

            validateProjectAccess(task.getProject().getId(), userId);

            Process targetProcess = processRepository.findById(targetProcessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proceso destino no encontrado con ID: " + targetProcessId));

            if (!targetProcess.getProject().getId().equals(task.getProject().getId())) {
                throw new IllegalArgumentException("El proceso destino no pertenece al mismo proyecto");
            }

            Long originalProcessId = task.getProcess().getId();
            int originalPosition = task.getPosition();
            Task.TaskStatus originalStatus = task.getStatus();

            // 2. Reordenamiento de posiciones
            if (!originalProcessId.equals(targetProcessId)) {
                taskRepository.shiftPositions(originalProcessId, originalPosition + 1, -1);

                if (newPosition == null) {
                    long taskCount = taskRepository.countByProcessId(targetProcessId);
                    newPosition = (int) taskCount;
                }

                taskRepository.shiftPositions(targetProcessId, newPosition, 1);
            } else {
                if (newPosition != null && newPosition != originalPosition) {
                    if (newPosition < originalPosition) {
                        taskRepository.shiftPositionsInSameProcess(
                                targetProcessId, newPosition, originalPosition - 1, 1);
                    } else {
                        taskRepository.shiftPositionsInSameProcess(
                                targetProcessId, originalPosition + 1, newPosition, -1);
                    }
                }
            }

            // 3. Aplicar cambios
            task.setProcess(targetProcess);
            task.setPosition(newPosition != null ? newPosition : 0);

            // 4. Sincronización de estado de la tarea
            if (targetProcess.getIsCompleted()) {
                if (originalStatus != Task.TaskStatus.DONE) {
                    task.markAsCompleted();
                }
            } else if (originalStatus == Task.TaskStatus.DONE) {
                task.setStatus(Task.TaskStatus.IN_PROGRESS);
                task.setCompletedAt(null);
                task.setCompletionPercentage(0);
            } else if (originalStatus == Task.TaskStatus.TODO) {
                task.setStatus(Task.TaskStatus.IN_PROGRESS);
            } else if ("TODO".equalsIgnoreCase(targetProcess.getName())) {
                task.setStatus(Task.TaskStatus.TODO);
                task.setCompletedAt(null);
            }

            // 5. Guardar tarea
            Task movedTask = taskRepository.save(task);

            log.info("Task moved successfully. New Process: {}, New Position: {}, New Status: {}",
                    targetProcessId, movedTask.getPosition(), movedTask.getStatus());

            // 6. Actualizar tarea padre si es subtarea
            if (movedTask.getParentTask() != null) {
                updateParentTaskCompletion(movedTask.getParentTask());
            }

            // 7. 🔥 SINCRONIZAR ESTADO DEL PROYECTO EN TRANSACCIÓN SEPARADA
            Long projectId = movedTask.getProject().getId();
            String newStatus = movedTask.getStatus().name();

            // Usar el método async que no afecta esta transacción
            if (projectService != null) {
                try {
                    projectService.updateProjectStatusAsync(projectId, newStatus, userId);
                } catch (Exception e) {
                    log.warn("Could not update project status asynchronously: {}", e.getMessage());
                    // No afecta el movimiento de la tarea
                }
            }

            return taskMapper.taskToResponse(movedTask);
        }
        // =================================================================================
        // MÉTODOS DE ASIGNACIÓN
        // =================================================================================

        /**
         * Asigna un usuario a una tarea
         */
        public TaskDTO.Response assignUser(Long taskId, Long userId, Long requestingUserId) {
            log.info("Assigning user ID: {} to task ID: {} by user ID: {}", userId, taskId, requestingUserId);

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tarea no encontrada con ID: " + taskId
                    ));

            validateProjectAccess(task.getProject().getId(), requestingUserId);

            User userToAssign = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con ID: " + userId
                    ));

            task.addAssignee(userToAssign);

            Task updatedTask = taskRepository.save(task);
            log.info("User assigned successfully to task ID: {}", taskId);

            return taskMapper.taskToResponse(updatedTask);
        }

        /**
         * Desasigna un usuario de una tarea
         */
        public TaskDTO.Response unassignUser(Long taskId, Long userId, Long requestingUserId) {
            log.info("Unassigning user ID: {} from task ID: {} by user ID: {}", userId, taskId, requestingUserId);

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tarea no encontrada con ID: " + taskId
                    ));

            validateProjectAccess(task.getProject().getId(), requestingUserId);

            User userToUnassign = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con ID: " + userId
                    ));

            task.removeAssignee(userToUnassign);

            Task updatedTask = taskRepository.save(task);
            log.info("User unassigned successfully from task ID: {}", taskId);

            return taskMapper.taskToResponse(updatedTask);
        }

        // =================================================================================
        // MÉTODOS DE BÚSQUEDA Y SOPORTE
        // =================================================================================

        /**
         * N°18: Obtiene subtareas de una tarea
         */
        @Transactional(readOnly = true)
        public List<TaskDTO.Summary> getSubtasks(Long parentTaskId, Long userId) {
            log.debug("Fetching subtasks for task ID: {}", parentTaskId);

            Task parentTask = taskRepository.findById(parentTaskId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tarea no encontrada con ID: " + parentTaskId
                    ));

            validateProjectAccess(parentTask.getProject().getId(), userId);

            return taskRepository.findByParentTaskId(parentTaskId).stream()
                    .map(taskMapper::taskToSummary)
                    .collect(Collectors.toList());
        }

        /**
         * N°13: Busca tareas por palabra clave
         */
        @Transactional(readOnly = true)
        public List<TaskDTO.Response> searchTasks(
                Long projectId,
                String keyword,
                Long userId) {
            log.debug("Searching tasks with keyword: {}", keyword);

            validateProjectAccess(projectId, userId);

            return taskRepository.searchByProjectAndKeyword(projectId, keyword).stream()
                    .map(taskMapper::taskToResponse)
                    .collect(Collectors.toList());
        }

        /**
         * N°17: Obtiene tareas con fecha límite próxima
         */
        @Transactional(readOnly = true)
        public List<TaskDTO.Response> getUpcomingTasks(
                Long projectId,
                int days,
                Long userId) {
            log.debug("Fetching tasks with deadlines in next {} days", days);

            validateProjectAccess(projectId, userId);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = now.plusDays(days);

            return taskRepository.findByDueDateBetween(now, endDate).stream()
                    .filter(task -> task.getProject().getId().equals(projectId))
                    .map(taskMapper::taskToResponse)
                    .collect(Collectors.toList());
        }

        /**
         * N°17: Obtiene tareas vencidas
         */
        @Transactional(readOnly = true)
        public List<TaskDTO.Response> getOverdueTasks(Long projectId, Long userId) {
            log.debug("Fetching overdue tasks for project ID: {}", projectId);

            validateProjectAccess(projectId, userId);

            return taskRepository.findOverdueTasks(
                            LocalDateTime.now(),
                            Task.TaskStatus.DONE,
                            Task.TaskStatus.CANCELLED
                    ).stream()
                    .filter(task -> task.getProject().getId().equals(projectId))
                    .map(taskMapper::taskToResponse)
                    .collect(Collectors.toList());
        }

        /**
         * Elimina una tarea
         */
        public void deleteTask(Long id, Long userId) {
            log.info("Deleting task ID: {}", id);

            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tarea no encontrada con ID: " + id
                    ));

            validateProjectAccess(task.getProject().getId(), userId);

            Task parentTask = task.getParentTask();

            // Cierra el hueco dejado por la tarea eliminada (mantiene la consistencia del Kanban)
            taskRepository.shiftPositions(task.getProcess().getId(), task.getPosition() + 1, -1);

            taskRepository.delete(task);

            log.info("Task deleted successfully with ID: {}", id);

            if (parentTask != null) {
                updateParentTaskCompletion(parentTask);
            }
        }

        /**
         * N°6: Obtiene todas las tareas de un proceso (columna Kanban)
         */
        @Transactional(readOnly = true)
        public List<TaskDTO.Response> getProcessTasks(Long processId, Long userId) {
            log.debug("Fetching tasks for process ID: {}", processId);

            Process process = processRepository.findById(processId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Proceso no encontrado con ID: " + processId
                    ));

            // Valida que el usuario tenga acceso al proyecto al que pertenece el proceso
            validateProjectAccess(process.getProject().getId(), userId);

            // Usa el repositorio para obtener tareas ordenadas por posición (vital para Kanban)
            return taskRepository.findByProcessIdOrderByPositionAsc(processId).stream()
                    .map(taskMapper::taskToResponse)
                    .collect(Collectors.toList());
        }

        // =================================================================================
        // MÉTODOS DE SOPORTE Y UTILIDADES
        // =================================================================================

        /**
         * N°18: Actualiza el porcentaje de completado de una tarea padre
         */
        private void updateParentTaskCompletion(Task parentTask) {
            parentTask.updateCompletionFromSubtasks();
            taskRepository.save(parentTask);
        }

        /**
         * Placeholder para verificar si el usuario es un administrador global.
         */
        private boolean isAdmin(Long userId) {
            return false;
        }

        /**
         * Valida que el proyecto exista y el usuario tenga acceso
         */
        private Project validateProjectAccess(Long projectId, Long userId) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Proyecto no encontrado con ID: " + projectId
                    ));

            // Si el usuario es Admin, conceder acceso inmediato (para operaciones de escritura)
            if (isAdmin(userId)) {
                return project;
            }

            // Si no es Admin, verificar Creador o Miembro
            boolean hasAccess = project.getCreatedBy().getId().equals(userId) ||
                    project.getMembers().stream()
                            .anyMatch(member -> member.getId().equals(userId));

            if (!hasAccess) {
                throw new AccessDeniedException("No tienes acceso a este proyecto");
            }

            return project;
        }

        /**
         * Obtiene la respuesta simple de una tarea (para servicios externos como recordatorios)
         */
        @Transactional(readOnly = true)
        public TaskDTO.SimpleResponse getTaskSimpleById(Long taskId, Long userId) {
            log.info("Fetching task ID: {} for user ID: {}", taskId, userId);

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

            // Verificar acceso del usuario al proyecto
            if (!hasAccessToTask(task, userId)) {
                throw new AccessDeniedException("User does not have access to this task");
            }

            return TaskDTO.SimpleResponse.builder()
                    .id(task.getId())
                    .title(task.getTitle())
                    .dueDate(task.getDueDate())
                    .status(task.getStatus())
                    .build();
        }

        private boolean hasAccessToTask(Task task, Long userId) {
            Project project = task.getProject();
            return isAdmin(userId) ||
                    project.getCreatedBy().getId().equals(userId) ||
                    project.getMembers().stream()
                            .anyMatch(member -> member.getId().equals(userId));
        }
    }
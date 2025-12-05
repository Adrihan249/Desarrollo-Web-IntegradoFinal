// ===================================
// PROCESS SERVICE - CORREGIDO CON REORDENACIÓN EFICIENTE
// ===================================
package com.taskmanager.service;

import com.taskmanager.dto.ProcessDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.ProcessMapper;
import com.taskmanager.model.Process;
import com.taskmanager.model.Project;
import com.taskmanager.Repositorios.ProcessRepository;
import com.taskmanager.Repositorios.ProjectRepository;
import com.taskmanager.Repositorios.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProcessService {

    private final ProcessRepository processRepository;
    private final ProjectRepository projectRepository;
    private final ProcessMapper processMapper;
    private final TaskRepository taskRepository;

    public ProcessDTO.Response createProcess(
            Long projectId,
            ProcessDTO.CreateRequest request,
            Long userId) {
        log.info("Creating process '{}' for project ID: {}", request.getName(), projectId);

        Project project = validateProjectAccess(projectId, userId);

        Process process = processMapper.createRequestToProcess(request);
        process.setProject(project);

        if (request.getPosition() == null) {
            long processCount = processRepository.countByProjectId(projectId);
            process.setPosition((int) processCount);
        } else {
            // Si se especifica una posición, ajustar las existentes
            List<Process> processes = processRepository.findByProjectIdOrderByPositionAsc(projectId);
            for (Process p : processes) {
                if (p.getPosition() >= request.getPosition()) {
                    p.setPosition(p.getPosition() + 1);
                }
            }
            processRepository.saveAll(processes);
            process.setPosition(request.getPosition());
        }

        Process savedProcess = processRepository.save(process);
        log.info("Process created successfully with ID: {}", savedProcess.getId());

        return processMapper.processToResponse(savedProcess);
    }

    @Transactional(readOnly = true)
    public List<ProcessDTO.Response> getProjectProcesses(Long projectId, Long userId) {
        log.debug("Fetching processes for project ID: {}", projectId);

        validateProjectAccess(projectId, userId);

        return processRepository.findByProjectIdOrderByPositionAsc(projectId).stream()
                .map(process -> {
                    ProcessDTO.Response response = processMapper.processToResponse(process);
                    // Agregar conteo de tareas
                    long taskCount = taskRepository.countByProcessId(process.getId());
                    response.setTaskCount((int) taskCount);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProcessDTO.Response getProcessById(Long id, Long userId) {
        log.debug("Fetching process ID: {}", id);

        Process process = processRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + id
                ));

        validateProjectAccess(process.getProject().getId(), userId);

        ProcessDTO.Response response = processMapper.processToResponse(process);
        long taskCount = taskRepository.countByProcessId(id);
        response.setTaskCount((int) taskCount);

        return response;
    }

    public ProcessDTO.Response updateProcess(
            Long id,
            ProcessDTO.UpdateRequest request,
            Long userId) {
        log.info("Updating process ID: {}", id);

        Process process = processRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + id
                ));

        validateProjectOwner(process.getProject(), userId);

        processMapper.updateProcessFromDto(request, process);

        Process updatedProcess = processRepository.save(process);
        log.info("Process updated successfully with ID: {}", updatedProcess.getId());

        return processMapper.processToResponse(updatedProcess);
    }

    /**
     * MÉTODO MEJORADO: Reordena procesos de manera más eficiente
     */
    public List<ProcessDTO.Response> reorderProcesses(
            Long projectId,
            Long processId,
            Integer newPosition,
            Long userId) {
        log.info("Reordering process ID: {} to position: {}", processId, newPosition);

        Project project = validateProjectAccess(projectId, userId);
        validateProjectOwner(project, userId);

        Process processToMove = processRepository.findById(processId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + processId
                ));

        if (!processToMove.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("El proceso no pertenece al proyecto");
        }

        Integer oldPosition = processToMove.getPosition();

        if (oldPosition.equals(newPosition)) {
            return getProjectProcesses(projectId, userId);
        }

        List<Process> processes = processRepository.findByProjectIdOrderByPositionAsc(projectId);

        // Remover el proceso de su posición actual
        processes.remove(processToMove);

        // Insertar en la nueva posición
        processes.add(newPosition, processToMove);

        // Actualizar todas las posiciones
        for (int i = 0; i < processes.size(); i++) {
            processes.get(i).setPosition(i);
        }

        processRepository.saveAll(processes);

        log.info("Processes reordered successfully");

        return getProjectProcesses(projectId, userId);
    }

    /**
     * MÉTODO CORREGIDO: Usa conteo real de tareas
     */
    public void deleteProcess(Long id, Long userId) {
        log.info("Deleting process ID: {}", id);

        Process process = processRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + id
                ));

        validateProjectOwner(process.getProject(), userId);

        // Verificar que no tenga tareas
        long taskCount = taskRepository.countByProcessId(id);
        if (taskCount > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar un proceso que contiene tareas. " +
                            "Mueve las tareas a otro proceso primero."
            );
        }

        Long projectId = process.getProject().getId();
        Integer deletedPosition = process.getPosition();

        processRepository.delete(process);

        // Reajustar las posiciones de los procesos restantes
        List<Process> remainingProcesses = processRepository
                .findByProjectIdOrderByPositionAsc(projectId);

        for (Process p : remainingProcesses) {
            if (p.getPosition() > deletedPosition) {
                p.setPosition(p.getPosition() - 1);
            }
        }

        processRepository.saveAll(remainingProcesses);

        log.info("Process deleted successfully with ID: {}", id);
    }

    public List<ProcessDTO.Response> createDefaultProcesses(Long projectId, Long userId) {
        log.info("Creating default processes for project ID: {}", projectId);

        Project project = validateProjectAccess(projectId, userId);

        int position = 0;
        for (Process.DefaultProcessType type : Process.DefaultProcessType.values()) {
            Process process = Process.builder()
                    .name(type.getName())
                    .description("Proceso " + type.getName())
                    .color(type.getColor())
                    .position(position++)
                    .isCompleted(type.isCompleted())
                    .project(project)
                    .build();

            processRepository.save(process);
        }

        log.info("Default processes created successfully");

        return getProjectProcesses(projectId, userId);
    }

    private Project validateProjectAccess(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + projectId
                ));

        boolean hasAccess = project.getCreatedBy().getId().equals(userId) ||
                project.getMembers().stream()
                        .anyMatch(member -> member.getId().equals(userId));

        if (!hasAccess) {
            throw new AccessDeniedException("No tienes acceso a este proyecto");
        }

        return project;
    }

    private void validateProjectOwner(Project project, Long userId) {
        if (!project.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "Solo el creador del proyecto puede realizar esta acción"
            );
        }
    }
}

package com.taskmanager.service;

import com.taskmanager.dto.AttachmentDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.AttachmentMapper;
import com.taskmanager.model.Attachment;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.Repositorios.AttachmentRepository;
import com.taskmanager.Repositorios.TaskRepository;
import com.taskmanager.Repositorios.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de Gestión de Archivos Adjuntos
 *
 * CUMPLE REQUERIMIENTO N°11: Adjuntar archivos
 *
 * Permite a los usuarios:
 * - Subir archivos a tareas
 * - Descargar archivos
 * - Eliminar archivos
 * - Listar archivos de una tarea
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AttachmentMapper attachmentMapper;

    // Directorio donde se guardarán los archivos
    // En producción podría ser S3, Azure Blob Storage, etc.
    @Value("${file.upload-dir:uploads/attachments}")
    private String uploadDir;

    // Tamaño máximo de archivo en bytes (50MB por defecto)
    @Value("${file.max-size:52428800}")
    private long maxFileSize;

    /**
     * N°11: Sube un archivo a una tarea
     *
     * @param taskId ID de la tarea
     * @param file Archivo a subir (MultipartFile)
     * @param description Descripción opcional
     * @param userId ID del usuario que sube
     * @return DTO del archivo adjunto creado
     */
    public AttachmentDTO.Response uploadFile(
            Long taskId,
            MultipartFile file,
            String description,
            Long userId) throws IOException {
        log.info("Uploading file '{}' to task ID: {}", file.getOriginalFilename(), taskId);

        // Valida que el archivo no esté vacío
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Valida el tamaño del archivo
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("El archivo excede el tamaño máximo permitido de %d MB",
                            maxFileSize / (1024 * 1024))
            );
        }

        // Valida que la tarea exista
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tarea no encontrada con ID: " + taskId
                ));

        // Valida que el usuario tenga acceso
        validateTaskAccess(task, userId);

        // Busca el usuario
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        // Genera nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;

        // Crea el directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Guarda el archivo en el sistema de archivos
        Path filePath = uploadPath.resolve(storedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Calcula hash MD5 del archivo
        String fileHash = calculateMD5(file);

        // Determina si es imagen
        boolean isImage = isImageFile(file.getContentType());

        // Crea la entidad Attachment
        Attachment attachment = Attachment.builder()
                .task(task)
                .uploadedBy(uploader)
                .fileName(originalFilename)
                .storedFileName(storedFileName)
                .filePath(filePath.toString())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .fileExtension(fileExtension)
                .description(description)
                .isImage(isImage)
                .fileHash(fileHash)
                .build();

        // Guarda en la base de datos
        Attachment savedAttachment = attachmentRepository.save(attachment);
        log.info("File uploaded successfully with ID: {}", savedAttachment.getId());

        return attachmentMapper.attachmentToResponse(savedAttachment);
    }

    /**
     * N°11: Obtiene todos los archivos adjuntos de una tarea
     * Usa Query Method sin SQL
     */
    @Transactional(readOnly = true)
    public List<AttachmentDTO.Response> getTaskAttachments(Long taskId, Long userId) {
        log.debug("Fetching attachments for task ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tarea no encontrada con ID: " + taskId
                ));

        validateTaskAccess(task, userId);

        // Query Method: findByTaskIdOrderByCreatedAtDesc
        return attachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(attachmentMapper::attachmentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * N°11: Obtiene un archivo adjunto por ID
     */
    @Transactional(readOnly = true)
    public AttachmentDTO.Response getAttachmentById(Long id, Long userId) {
        log.debug("Fetching attachment ID: {}", id);

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Archivo no encontrado con ID: " + id
                ));

        validateTaskAccess(attachment.getTask(), userId);

        return attachmentMapper.attachmentToResponse(attachment);
    }

    /**
     * N°11: Descarga un archivo
     *
     * @param id ID del archivo
     * @param userId ID del usuario que descarga
     * @return Resource del archivo
     */
    @Transactional
    public Resource downloadFile(Long id, Long userId) throws IOException {
        log.info("Downloading attachment ID: {} by user ID: {}", id, userId);

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Archivo no encontrado con ID: " + id
                ));

        validateTaskAccess(attachment.getTask(), userId);

        // Incrementa el contador de descargas
        attachment.incrementDownloadCount();
        attachmentRepository.save(attachment);

        // Carga el archivo como Resource
        Path filePath = Paths.get(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("No se pudo leer el archivo: " + attachment.getFileName());
        }

        log.info("File downloaded successfully: {}", attachment.getFileName());

        return resource;
    }

    /**
     * N°11: Actualiza la descripción de un archivo
     */
    public AttachmentDTO.Response updateAttachment(
            Long id,
            String description,
            Long userId) {
        log.info("Updating attachment ID: {}", id);

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Archivo no encontrado con ID: " + id
                ));

        validateTaskAccess(attachment.getTask(), userId);

        // Solo el que lo subió o el creador del proyecto pueden actualizar
        boolean isUploader = attachment.getUploadedBy().getId().equals(userId);
        boolean isProjectOwner = attachment.getTask().getProject()
                .getCreatedBy().getId().equals(userId);

        if (!isUploader && !isProjectOwner) {
            throw new AccessDeniedException(
                    "Solo quien subió el archivo o el creador del proyecto pueden actualizarlo"
            );
        }

        attachment.setDescription(description);

        Attachment updatedAttachment = attachmentRepository.save(attachment);
        log.info("Attachment updated successfully with ID: {}", updatedAttachment.getId());

        return attachmentMapper.attachmentToResponse(updatedAttachment);
    }

    /**
     * N°11: Elimina un archivo
     * Elimina tanto el registro en BD como el archivo físico
     */
    public void deleteAttachment(Long id, Long userId) throws IOException {
        log.info("Deleting attachment ID: {}", id);

        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Archivo no encontrado con ID: " + id
                ));

        validateTaskAccess(attachment.getTask(), userId);

        // Solo el que lo subió o el creador del proyecto pueden eliminar
        boolean isUploader = attachment.getUploadedBy().getId().equals(userId);
        boolean isProjectOwner = attachment.getTask().getProject()
                .getCreatedBy().getId().equals(userId);

        if (!isUploader && !isProjectOwner) {
            throw new AccessDeniedException(
                    "Solo quien subió el archivo o el creador del proyecto pueden eliminarlo"
            );
        }

        // Elimina el archivo físico
        Path filePath = Paths.get(attachment.getFilePath());
        try {
            Files.deleteIfExists(filePath);
            log.info("Physical file deleted: {}", attachment.getStoredFileName());
        } catch (IOException e) {
            log.error("Error deleting physical file: {}", e.getMessage());
            // Continúa para eliminar el registro en BD
        }

        // Elimina el registro de la BD
        attachmentRepository.delete(attachment);
        log.info("Attachment deleted successfully with ID: {}", id);
    }

    /**
     * N°11: Busca archivos por nombre
     * Usa Query Method sin SQL
     */
    @Transactional(readOnly = true)
    public List<AttachmentDTO.Response> searchAttachments(
            Long taskId,
            String keyword,
            Long userId) {
        log.debug("Searching attachments with keyword: {}", keyword);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tarea no encontrada con ID: " + taskId
                ));

        validateTaskAccess(task, userId);

        // Query Method: findByFileNameContainingIgnoreCase
        return attachmentRepository.findByFileNameContainingIgnoreCase(keyword).stream()
                .filter(a -> a.getTask().getId().equals(taskId))
                .map(attachmentMapper::attachmentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta archivos de una tarea
     * Usa Query Method sin SQL
     */
    @Transactional(readOnly = true)
    public long countTaskAttachments(Long taskId) {
        // Query Method: countByTaskId
        return attachmentRepository.countByTaskId(taskId);
    }

    /**
     * Calcula el tamaño total de archivos de una tarea
     * Usa Query Method sin SQL
     */
    @Transactional(readOnly = true)
    public long getTotalFileSize(Long taskId) {
        // Query Method: sumFileSizeByTaskId
        Long total = attachmentRepository.sumFileSizeByTaskId(taskId);
        return total != null ? total : 0L;
    }

    /**
     * Valida que el usuario tenga acceso a la tarea
     */
    private void validateTaskAccess(Task task, Long userId) {
        boolean hasAccess = task.getProject().getCreatedBy().getId().equals(userId) ||
                task.getProject().getMembers().stream()
                        .anyMatch(member -> member.getId().equals(userId));

        if (!hasAccess) {
            throw new AccessDeniedException("No tienes acceso a esta tarea");
        }
    }

    /**
     * Extrae la extensión de un archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    /**
     * Verifica si un tipo MIME corresponde a una imagen
     */
    private boolean isImageFile(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Calcula el hash MD5 de un archivo
     */
    private String calculateMD5(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(file.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Error calculating MD5: {}", e.getMessage());
            return null;
        }
    }
}
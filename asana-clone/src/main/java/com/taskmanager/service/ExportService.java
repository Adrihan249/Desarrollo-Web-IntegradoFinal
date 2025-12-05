package com.taskmanager.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.taskmanager.dto.*;
import com.taskmanager.exception.*;
import com.taskmanager.mapper.ExportJobMapper;
import com.taskmanager.model.*;
import com.taskmanager.model.enums.*;
import com.taskmanager.Repositorios.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ SERVICIO CON PDF REAL usando iText7
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final ExportJobRepository exportJobRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ExportJobMapper exportJobMapper;

    @Value("${export.directory:uploads/exports}")
    private String exportDirectory;

    @Value("${export.expiration-days:7}")
    private Integer expirationDays;

    @Transactional
    public ExportDTO.Response requestExport(Long userId, ExportDTO.CreateRequest request) {
        log.info("📦 User {} requesting export for project {}", userId, request.getReferenceId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ExportJob job = exportJobMapper.toEntity(request);
        job.setRequestedBy(user);
        job.setStatus(ExportStatus.PENDING);
        job = exportJobRepository.save(job);

        processExportAsync(job.getId());

        return exportJobMapper.toResponse(job);
    }

    @Async
    @Transactional
    public void processExportAsync(Long jobId) {
        log.info("⚙️ Processing export job {}", jobId);

        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found"));

        try {
            job.setStatus(ExportStatus.PROCESSING);
            job.setProgress(10);
            exportJobRepository.save(job);

            // 🔥 Generar PDF REAL
            byte[] pdfData = generateRealPDF(job);

            job.setProgress(80);
            exportJobRepository.save(job);

            String fileName = generateFileName(job);
            Path filePath = saveFile(fileName, pdfData);

            job.setStatus(ExportStatus.COMPLETED);
            job.setFileName(fileName);
            job.setFilePath(filePath.toString());
            job.setFileSize((long) pdfData.length);
            job.setDownloadUrl("/api/exports/" + job.getId() + "/download");
            job.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));
            job.setCompletedAt(LocalDateTime.now());
            job.setProgress(100);
            exportJobRepository.save(job);

            log.info("✅ Export job {} completed - File: {}", jobId, fileName);

        } catch (Exception e) {
            log.error("❌ Export job {} failed: {}", jobId, e.getMessage(), e);
            job.setStatus(ExportStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            exportJobRepository.save(job);
        }
    }

    /**
     * 🔥 GENERA UN PDF REAL usando iText7
     */
    private byte[] generateRealPDF(ExportJob job) throws Exception {
        log.info("📄 Generating REAL PDF for project {}", job.getReferenceId());

        // Obtener datos
        Project project = projectRepository.findByIdForExport(job.getReferenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectIdForExport(job.getReferenceId());

        // Crear PDF en memoria
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // ==================== HEADER ====================
        Paragraph title = new Paragraph("REPORTE DE PROYECTO")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);

        Paragraph subtitle = new Paragraph("Fecha: " + LocalDateTime.now().format(formatter))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);

        // ==================== INFORMACIÓN DEL PROYECTO ====================
        document.add(new Paragraph("INFORMACIÓN DEL PROYECTO")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(10));

        // Tabla de información del proyecto
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth();

        addInfoRow(infoTable, "Nombre", project.getName());
        addInfoRow(infoTable, "ID", project.getId().toString());
        addInfoRow(infoTable, "Estado", project.getStatus().getDisplayName());
        addInfoRow(infoTable, "Descripción", project.getDescription() != null ? project.getDescription() : "N/A");
        addInfoRow(infoTable, "Creador", project.getCreatedBy().getFullName());
        addInfoRow(infoTable, "Miembros", String.valueOf(project.getMembers().size()));
        addInfoRow(infoTable, "Total de Tareas", String.valueOf(tasks.size()));

        document.add(infoTable);

        // ==================== ESTADÍSTICAS ====================
        document.add(new Paragraph("ESTADÍSTICAS")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

        Map<Task.TaskStatus, Long> statusCount = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();

        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            long count = statusCount.getOrDefault(status, 0L);
            addInfoRow(statsTable, status.getDisplayName(), count + " tareas");
        }

        document.add(statsTable);

        // ==================== LISTADO DE TAREAS ====================
        document.add(new Paragraph("LISTADO DE TAREAS")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

        List<Task> mainTasks = tasks.stream()
                .filter(t -> t.getParentTask() == null)
                .collect(Collectors.toList());

        Map<Long, List<Task>> subtaskMap = tasks.stream()
                .filter(t -> t.getParentTask() != null)
                .collect(Collectors.groupingBy(t -> t.getParentTask().getId()));

        int counter = 0;
        for (Task task : mainTasks) {
            counter++;

            // Título de la tarea
            Paragraph taskTitle = new Paragraph("[" + counter + "] " + task.getTitle())
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(10);
            document.add(taskTitle);

            // Detalles de la tarea
            Table taskTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .useAllAvailableWidth()
                    .setMarginLeft(20);

            addInfoRow(taskTable, "Estado", task.getStatus().getDisplayName());
            addInfoRow(taskTable, "Prioridad", task.getPriority().getDisplayName());

            String assignees = task.getAssignees().stream()
                    .map(User::getFullName)
                    .collect(Collectors.joining(", "));
            addInfoRow(taskTable, "Asignados", assignees.isEmpty() ? "Sin asignar" : assignees);

            if (task.getDueDate() != null) {
                addInfoRow(taskTable, "Fecha Límite", task.getDueDate().format(formatter));
            }

            document.add(taskTable);

            // Subtareas
            List<Task> subtasks = subtaskMap.getOrDefault(task.getId(), Collections.emptyList());
            if (!subtasks.isEmpty()) {
                Paragraph subtaskHeader = new Paragraph("Subtareas (" + subtasks.size() + ")")
                        .setFontSize(10)
                        .setItalic()
                        .setMarginLeft(20)
                        .setMarginTop(5);
                document.add(subtaskHeader);

                for (Task subtask : subtasks) {
                    Paragraph subtaskItem = new Paragraph("• " + subtask.getTitle() + " (" + subtask.getStatus().getDisplayName() + ")")
                            .setFontSize(9)
                            .setMarginLeft(30);
                    document.add(subtaskItem);
                }
            }
        }

        // ==================== FOOTER ====================
        document.add(new Paragraph("\nFIN DEL REPORTE")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30));

        document.close();

        job.setTotalRecords(tasks.size());

        return baos.toByteArray();
    }

    /**
     * Helper para agregar filas a tablas de información
     */
    private void addInfoRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    @Transactional(readOnly = true)
    public List<ExportDTO.Response> getUserExports(Long userId) {
        return exportJobRepository.findByRequestedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(exportJobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExportDTO.Response getExportById(Long jobId, Long userId) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Export not found"));

        if (!job.getRequestedBy().getId().equals(userId)) {
            throw new BadRequestException("Export does not belong to user");
        }

        return exportJobMapper.toResponse(job);
    }

    @Transactional
    public byte[] downloadExport(Long jobId, Long userId) throws IOException {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Export not found"));

        if (!job.getRequestedBy().getId().equals(userId)) {
            throw new BadRequestException("Export does not belong to user");
        }

        if (job.getStatus() != ExportStatus.COMPLETED) {
            throw new BadRequestException("Export is not completed");
        }

        if (job.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Export has expired");
        }

        job.setDownloadCount(job.getDownloadCount() + 1);
        exportJobRepository.save(job);

        return Files.readAllBytes(Paths.get(job.getFilePath()));
    }

    private String generateFileName(ExportJob job) {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );

        // 🔥 Nombre más descriptivo
        Project project = projectRepository.findById(job.getReferenceId()).orElse(null);
        String projectName = project != null ?
                project.getName().replaceAll("[^a-zA-Z0-9]", "_") :
                "proyecto";

        return String.format("%s_%s.pdf", projectName, timestamp);
    }

    private Path saveFile(String fileName, byte[] data) throws IOException {
        Path directory = Paths.get(exportDirectory);
        Files.createDirectories(directory);

        Path filePath = directory.resolve(fileName);
        Files.write(filePath, data);

        log.info("💾 File saved: {}", filePath.toAbsolutePath());

        return filePath;
    }
}
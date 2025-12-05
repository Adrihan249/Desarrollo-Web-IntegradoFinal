package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;

import java.util.List;
/**
/**
 * ===================================================================
 * AttachmentMapper - Mapeo entre Attachment entities y DTOs
 *
 * CUMPLE REQUERIMIENTO N°11: Adjuntar archivos
 * ===================================================================
 */
@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AttachmentMapper {

    /**
     * N°11: Mapea Attachment a Response
     */
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "downloadUrl", expression = "java(getDownloadUrl(attachment))")
    @Mapping(target = "formattedFileSize", expression = "java(attachment.getFormattedFileSize())")
    AttachmentDTO.Response attachmentToResponse(Attachment attachment);

    /**
     * N°11: Mapea Attachment a Summary
     */
    @Mapping(target = "formattedFileSize", expression = "java(attachment.getFormattedFileSize())")
    AttachmentDTO.Summary attachmentToSummary(Attachment attachment);

    /**
     * Lista de Attachments a lista de Responses
     */
    List<AttachmentDTO.Response> attachmentsToResponses(List<Attachment> attachments);

    /**
     * Lista de Attachments a lista de Summaries
     */
    List<AttachmentDTO.Summary> attachmentsToSummaries(List<Attachment> attachments);

    /**
     * Genera URL de descarga
     */
    default String getDownloadUrl(Attachment attachment) {
        return "/api/attachments/" + attachment.getId() + "/download";
    }
}

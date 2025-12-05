package com.taskmanager.mapper;

import com.taskmanager.dto.DirectMessageDTO;
import com.taskmanager.model.DirectMessage;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

/**
 * ===================================================================
 * DirectMessageMapper - Mapeo entre DirectMessage entities y DTOs
 *
 * Adaptado para mapear campos de adjunto directamente.
 * ===================================================================
 */
@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class}, // Eliminamos AttachmentMapper
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DirectMessageMapper {

    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    // Mapeo directo de campos de archivo
    @Mapping(target = "attachmentUrl", source = "attachmentUrl")
    @Mapping(target = "attachmentName", source = "attachmentName")
    @Mapping(target = "attachmentMimeType", source = "attachmentMimeType")
    @Mapping(target = "attachmentSize", source = "attachmentSize")
    DirectMessage sendRequestToMessage(DirectMessageDTO.SendRequest request);

    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "receiver", source = "receiver")
    // Mapeo directo de campos de archivo
    @Mapping(target = "attachmentUrl", source = "attachmentUrl")
    @Mapping(target = "attachmentName", source = "attachmentName")
    @Mapping(target = "attachmentMimeType", source = "attachmentMimeType")
    @Mapping(target = "attachmentSize", source = "attachmentSize")
    // Los reactionCounts deberían ser calculados en el service o un método default
    // Aquí usamos un método helper para simplificar
    @Mapping(target = "reactionCounts", expression = "java(calculateReactionCounts(message))")
    DirectMessageDTO.Response messageToResponse(DirectMessage message);

    List<DirectMessageDTO.Response> messagesToResponses(List<DirectMessage> messages);

    /**
     * Helper para calcular el conteo de reacciones a partir del mapa de Sets de Long.
     */
    default Map<String, Integer> calculateReactionCounts(DirectMessage message) {
        if (message.getReactions() == null) {
            return Map.of();
        }
        return message.getReactions().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                ));
    }
}
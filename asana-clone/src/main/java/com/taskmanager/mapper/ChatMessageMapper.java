package com.taskmanager.mapper;

import com.taskmanager.dto.*;
import com.taskmanager.model.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * ===================================================================
 * ChatMessageMapper - Mapeo entre ChatMessage entities y DTOs
 *
 * CUMPLE REQUERIMIENTO N°14: Chat del proyecto
 * ===================================================================
 */
@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ChatMessageMapper {

    /**
     * N°14: Mapea SendRequest a ChatMessage
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "parentMessage", ignore = true)
    @Mapping(target = "edited", constant = "false")
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "replyCount", constant = "0")
    @Mapping(target = "readByUserIds", ignore = true)
    @Mapping(target = "pinned", constant = "false")
    @Mapping(target = "pinnedAt", ignore = true)
    @Mapping(target = "pinnedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToType")
    ChatMessage sendRequestToMessage(ChatMessageDTO.SendRequest request);

    /**
     * N°14: Mapea ChatMessage a Response
     */
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "parentMessageId", source = "parentMessage.id")
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "mentionedUsers", expression = "java(getMentionedUsers(message))")
    @Mapping(target = "reactionCounts", expression = "java(getReactionCounts(message))")
    ChatMessageDTO.Response messageToResponse(ChatMessage message);

    /**
     * Lista de Messages a Responses
     */
    List<ChatMessageDTO.Response> messagesToResponses(List<ChatMessage> messages);

    /**
     * N°14: Actualiza ChatMessage desde UpdateRequest
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "parentMessage", ignore = true)
    @Mapping(target = "attachmentUrl", ignore = true)
    @Mapping(target = "attachmentName", ignore = true)
    @Mapping(target = "attachmentMimeType", ignore = true)
    @Mapping(target = "attachmentSize", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(target = "readByUserIds", ignore = true)
    @Mapping(target = "pinned", ignore = true)
    @Mapping(target = "pinnedAt", ignore = true)
    @Mapping(target = "pinnedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMessageFromDto(
            ChatMessageDTO.UpdateRequest request,
            @MappingTarget ChatMessage message
    );

    @Named("typeToString")
    default String typeToString(ChatMessage.MessageType type) {
        return type != null ? type.name() : null;
    }

    @Named("stringToType")
    default ChatMessage.MessageType stringToType(String type) {
        if (type == null) return ChatMessage.MessageType.TEXT;
        try {
            return ChatMessage.MessageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ChatMessage.MessageType.TEXT;
        }
    }

    /**
     * Obtiene usuarios mencionados (simplificado)
     */
    default java.util.Set<UserDTO.Summary> getMentionedUsers(ChatMessage message) {
        // En producción, buscar usuarios por IDs
        return new java.util.HashSet<>();
    }

    /**
     * Convierte reacciones a contadores
     */
    default Map<String, Integer> getReactionCounts(ChatMessage message) {
        if (message.getReactions() == null) {
            return new java.util.HashMap<>();
        }
        return message.getReactions().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()
                ));
    }
}
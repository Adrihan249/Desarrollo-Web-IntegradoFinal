// ===================================
// CHAT MESSAGE SERVICE - CORREGIDO
// ===================================
package com.taskmanager.service;

import com.taskmanager.dto.ChatMessageDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.ChatMessageMapper;
import com.taskmanager.model.ChatMessage;
import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.model.Notification;
import com.taskmanager.Repositorios.ChatMessageRepository;
import com.taskmanager.Repositorios.ProjectRepository;
import com.taskmanager.Repositorios.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper messageMapper;
    private final NotificationService notificationService;

    public ChatMessageDTO.Response sendMessage(
            Long projectId,
            ChatMessageDTO.SendRequest request,
            Long userId) {
        log.info("Sending chat message to project ID: {} by user ID: {}", projectId, userId);

        Project project = validateProjectAccess(projectId, userId);

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        ChatMessage message = messageMapper.sendRequestToMessage(request);
        message.setProject(project);
        message.setSender(sender);

        if (request.getParentMessageId() != null) {
            ChatMessage parentMessage = messageRepository.findById(request.getParentMessageId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Mensaje padre no encontrado con ID: " + request.getParentMessageId()
                    ));

            if (!parentMessage.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException(
                        "El mensaje padre no pertenece al mismo proyecto"
                );
            }

            message.setParentMessage(parentMessage);
            parentMessage.incrementReplyCount();
            messageRepository.save(parentMessage);
        }

        message.markAsReadBy(userId);

        ChatMessage savedMessage = messageRepository.save(message);
        log.info("Chat message sent successfully with ID: {}", savedMessage.getId());

        if (request.getMentionedUserIds() != null && !request.getMentionedUserIds().isEmpty()) {
            request.getMentionedUserIds().forEach(mentionedUserId -> {
                if (!mentionedUserId.equals(userId)) {
                    notificationService.createNotification(
                            mentionedUserId,
                            Notification.NotificationType.MENTIONED_IN_COMMENT,
                            "Te mencionaron en el chat",
                            String.format("%s te mencionó en el chat del proyecto", sender.getFullName()),
                            "CHAT_MESSAGE",
                            savedMessage.getId(),
                            userId
                    );
                }
            });
        }

        return messageMapper.messageToResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO.Response> getProjectMessages(
            Long projectId,
            Long userId,
            int page,
            int size) {
        log.debug("Fetching messages for project ID: {}", projectId);

        validateProjectAccess(projectId, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = messageRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId, pageable);

        return messageMapper.messagesToResponses(messages.getContent());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO.Response> getRecentMessages(Long projectId, Long userId) {
        log.debug("Fetching recent messages for project ID: {}", projectId);

        validateProjectAccess(projectId, userId);

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<ChatMessage> messages = messageRepository.findRecentMessages(projectId, since);

        return messageMapper.messagesToResponses(messages);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO.Response> getMessageReplies(
            Long messageId,
            Long userId) {
        log.debug("Fetching replies for message ID: {}", messageId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + messageId
                ));

        validateProjectAccess(message.getProject().getId(), userId);

        List<ChatMessage> replies = messageRepository
                .findByParentMessageIdOrderByCreatedAtAsc(messageId);

        return messageMapper.messagesToResponses(replies);
    }

    public ChatMessageDTO.Response updateMessage(
            Long messageId,
            ChatMessageDTO.UpdateRequest request,
            Long userId) {
        log.info("Updating chat message ID: {} by user ID: {}", messageId, userId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + messageId
                ));

        if (!message.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el autor puede editar el mensaje");
        }

        messageMapper.updateMessageFromDto(request, message);
        message.markAsEdited();

        ChatMessage updated = messageRepository.save(message);
        log.info("Message updated successfully with ID: {}", updated.getId());

        return messageMapper.messageToResponse(updated);
    }

    public void deleteMessage(Long messageId, Long userId) {
        log.info("Deleting chat message ID: {} by user ID: {}", messageId, userId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + messageId
                ));

        boolean isAuthor = message.getSender().getId().equals(userId);
        boolean isProjectOwner = message.getProject().getCreatedBy().getId().equals(userId);

        if (!isAuthor && !isProjectOwner) {
            throw new AccessDeniedException(
                    "Solo el autor o el creador del proyecto pueden eliminar el mensaje"
            );
        }

        message.softDelete();
        messageRepository.save(message);

        log.info("Message deleted successfully (soft delete) with ID: {}", messageId);
    }

    public ChatMessageDTO.Response addReaction(
            Long messageId,
            String emoji,
            Long userId) {
        log.info("Adding reaction {} to message ID: {} by user ID: {}", emoji, messageId, userId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + messageId
                ));

        validateProjectAccess(message.getProject().getId(), userId);

        message.addReaction(emoji, userId);

        ChatMessage updated = messageRepository.save(message);

        return messageMapper.messageToResponse(updated);
    }

    public ChatMessageDTO.Response removeReaction(
            Long messageId,
            String emoji,
            Long userId) {
        log.info("Removing reaction {} from message ID: {} by user ID: {}",
                emoji, messageId, userId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + messageId
                ));

        validateProjectAccess(message.getProject().getId(), userId);

        message.removeReaction(emoji, userId);

        ChatMessage updated = messageRepository.save(message);

        return messageMapper.messageToResponse(updated);
    }

    public ChatMessageDTO.Response pinMessage(Long messageId, Long userId) {
        log.info("Pinning message ID: {} by user ID: {}", messageId, userId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + messageId
                ));

        if (!message.getProject().getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el creador del proyecto puede fijar mensajes");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        message.pin(user);

        ChatMessage updated = messageRepository.save(message);

        return messageMapper.messageToResponse(updated);
    }

    public ChatMessageDTO.Response unpinMessage(Long messageId, Long userId) {
        log.info("Unpinning message ID: {} by user ID: {}", messageId, userId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + messageId
                ));

        if (!message.getProject().getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el creador del proyecto puede desfijar mensajes");
        }

        message.unpin();

        ChatMessage updated = messageRepository.save(message);

        return messageMapper.messageToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO.Response> getPinnedMessages(Long projectId, Long userId) {
        log.debug("Fetching pinned messages for project ID: {}", projectId);

        validateProjectAccess(projectId, userId);

        List<ChatMessage> messages = messageRepository
                .findByProjectIdAndPinnedTrueOrderByPinnedAtDesc(projectId);

        return messageMapper.messagesToResponses(messages);
    }

    public void markAsRead(Long messageId, Long userId) {
        log.debug("Marking message {} as read by user {}", messageId, userId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mensaje no encontrado con ID: " + messageId
                ));

        validateProjectAccess(message.getProject().getId(), userId);

        message.markAsReadBy(userId);
        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO.Response> getUnreadMessages(Long projectId, Long userId) {
        log.debug("Fetching unread messages for project {} and user {}", projectId, userId);

        validateProjectAccess(projectId, userId);

        List<ChatMessage> messages = messageRepository.findUnreadByUserInProject(projectId, userId);

        return messageMapper.messagesToResponses(messages);
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages(Long projectId, Long userId) {
        validateProjectAccess(projectId, userId);

        return messageRepository.countUnreadByUserInProject(projectId, userId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO.Response> searchMessages(
            Long projectId,
            String keyword,
            Long userId) {
        log.debug("Searching messages in project {} with keyword: {}", projectId, keyword);

        validateProjectAccess(projectId, userId);

        List<ChatMessage> messages = messageRepository.searchMessages(projectId, keyword);

        return messageMapper.messagesToResponses(messages);
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
}
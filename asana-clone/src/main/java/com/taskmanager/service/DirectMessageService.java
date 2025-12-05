package com.taskmanager.service;

import com.taskmanager.dto.DirectMessageDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.DirectMessageMapper;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.model.*;
import com.taskmanager.Repositorios.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DirectMessageService {

    private final DirectMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserBlockRepository blockRepository;
    private final ProjectRepository projectRepository;
    private final DirectMessageMapper messageMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public DirectMessageDTO.Response sendMessage(
            DirectMessageDTO.SendRequest request,
            Long senderId) {
        log.info("Sending direct message from user {} to user {}", senderId, request.getReceiverId());

        // Validación adicional
        if (request.getReceiverId() == null) {
            throw new IllegalArgumentException("El ID del destinatario no puede ser nulo");
        }

        // Verificar que no sea el mismo usuario
        if (senderId.equals(request.getReceiverId())) {
            throw new IllegalArgumentException("No puedes enviarte mensajes a ti mismo");
        }

        // Verificar bloqueos
        if (blockRepository.existsByBlockerIdAndBlockedId(request.getReceiverId(), senderId)) {
            throw new AccessDeniedException("Este usuario te ha bloqueado");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + senderId));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Destinatario no encontrado con ID: " + request.getReceiverId()));

        DirectMessage message = messageMapper.sendRequestToMessage(request);
        message.setSender(sender);
        message.setReceiver(receiver);

        DirectMessage saved = messageRepository.save(message);
        log.info("Direct message sent successfully with ID: {}", saved.getId());

        // Crear notificación
        createMessageNotification(saved, sender, receiver);

        return messageMapper.messageToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DirectMessageDTO.Response> getConversationMessages(
            Long userId,
            Long otherUserId,
            int page,
            int size) {
        log.debug("Fetching conversation between {} and {}", userId, otherUserId);

        String conversationId = DirectMessage.generateConversationId(userId, otherUserId);
        Pageable pageable = PageRequest.of(page, size);

        List<DirectMessage> messages = messageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        return messageMapper.messagesToResponses(messages);
    }

    @Transactional(readOnly = true)
    public List<DirectMessageDTO.ConversationSummary> getConversations(Long userId) {
        log.debug("Fetching conversations for user {}", userId);

        List<String> conversationIds = messageRepository.findConversationIdsByUserId(userId);

        return conversationIds.stream()
                .map(convId -> buildConversationSummary(convId, userId))
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getLastMessageAt().compareTo(a.getLastMessageAt()))
                .collect(Collectors.toList());
    }

    public DirectMessageDTO.Response updateMessage(
            Long messageId,
            DirectMessageDTO.UpdateRequest request,
            Long userId) {
        log.info("Updating message {} by user {}", messageId, userId);

        DirectMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        if (!message.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el autor puede editar el mensaje");
        }

        message.setContent(request.getContent());
        message.markAsEdited();

        DirectMessage updated = messageRepository.save(message);
        return messageMapper.messageToResponse(updated);
    }

    public void deleteMessage(Long messageId, Long userId) {
        log.info("Deleting message {} by user {}", messageId, userId);

        DirectMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        if (!message.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("Solo el autor puede eliminar el mensaje");
        }

        message.softDelete();
        messageRepository.save(message);
    }

    public DirectMessageDTO.Response addReaction(
            Long messageId,
            String emoji,
            Long userId) {
        log.info("Adding reaction {} to message {}", emoji, messageId);

        DirectMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        message.addReaction(emoji, userId);
        DirectMessage updated = messageRepository.save(message);

        return messageMapper.messageToResponse(updated);
    }

    public DirectMessageDTO.Response removeReaction(
            Long messageId,
            String emoji,
            Long userId) {
        log.info("Removing reaction {} from message {}", emoji, messageId);

        DirectMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        message.removeReaction(emoji, userId);
        DirectMessage updated = messageRepository.save(message);

        return messageMapper.messageToResponse(updated);
    }

    public void markAsRead(Long messageId, Long userId) {
        DirectMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        if (message.getReceiver().getId().equals(userId) && !message.getIsRead()) {
            message.markAsRead();
            messageRepository.save(message);
        }
    }

    @Transactional
    public void markConversationAsRead(Long userId, Long otherUserId) {
        String conversationId = DirectMessage.generateConversationId(userId, otherUserId);
        messageRepository.markAllAsReadInConversation(conversationId, userId);
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages(Long userId) {
        return messageRepository.countUnreadByUser(userId);
    }

    private DirectMessageDTO.ConversationSummary buildConversationSummary(
            String conversationId,
            Long userId) {

        Optional<DirectMessage> lastMessageOpt = messageRepository
                .findFirstByConversationIdOrderByCreatedAtDesc(conversationId);

        if (lastMessageOpt.isEmpty()) {
            return null;
        }

        DirectMessage lastMessage = lastMessageOpt.get();

        // Determinar el otro usuario
        User otherUser = lastMessage.getSender().getId().equals(userId)
                ? lastMessage.getReceiver()
                : lastMessage.getSender();

        long unreadCount = messageRepository.countUnreadInConversation(conversationId, userId);

        // Buscar proyectos compartidos
        Set<String> sharedProjectNames = findSharedProjects(userId, otherUser.getId());
        boolean isProjectMember = !sharedProjectNames.isEmpty();

        return DirectMessageDTO.ConversationSummary.builder()
                .conversationId(conversationId)
                .otherUser(userMapper.userToSummary(otherUser))
                .lastMessage(messageMapper.messageToResponse(lastMessage))
                .unreadCount(unreadCount)
                .lastMessageAt(lastMessage.getCreatedAt())
                .sharedProjectNames(sharedProjectNames)
                .isProjectMember(isProjectMember)
                .build();
    }

    private Set<String> findSharedProjects(Long userId1, Long userId2) {
        // FIX: Usar el método correcto que existe en ProjectRepository
        List<Project> projects1 = projectRepository.findAllByUserId(userId1);
        List<Project> projects2 = projectRepository.findAllByUserId(userId2);

        // Encontrar proyectos en común comparando por ID
        Set<Long> projectIds1 = projects1.stream()
                .map(Project::getId)
                .collect(Collectors.toSet());

        return projects2.stream()
                .filter(p -> projectIds1.contains(p.getId()))
                .map(Project::getName)
                .collect(Collectors.toSet());
    }

    private void createMessageNotification(DirectMessage message, User sender, User receiver) {
        // Buscar proyectos compartidos
        Set<String> sharedProjects = findSharedProjects(sender.getId(), receiver.getId());

        String title = "Nuevo mensaje de " + sender.getFullName();
        String description;

        if (!sharedProjects.isEmpty()) {
            description = String.format("%s te ha enviado un mensaje. " +
                            "Comparten el proyecto: %s",
                    sender.getFullName(),
                    String.join(", ", sharedProjects));
        } else {
            description = String.format("%s te ha enviado un mensaje. " +
                            "Si no conoces a esta persona, puedes ignorar o bloquear este mensaje.",
                    sender.getFullName());
        }

        notificationService.createNotification(
                receiver.getId(),
                Notification.NotificationType.DIRECT_MESSAGE,
                title,
                description,
                "DIRECT_MESSAGE",
                message.getId(),
                sender.getId()
        );
    }
}
package com.taskmanager.service;

import com.taskmanager.dto.UserDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.model.User;
import com.taskmanager.model.UserBlock;
import com.taskmanager.Repositorios.UserBlockRepository;
import com.taskmanager.Repositorios.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserBlockService {

    private final UserBlockRepository blockRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void blockUser(Long blockerId, Long blockedId, String reason) {
        log.info("User {} blocking user {}", blockerId, blockedId);

        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("No puedes bloquearte a ti mismo");
        }

        // Verificar si ya está bloqueado
        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new IllegalArgumentException("Este usuario ya está bloqueado");
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario a bloquear no encontrado"));

        UserBlock block = UserBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .reason(reason)
                .build();

        blockRepository.save(block);
        log.info("User {} blocked successfully", blockedId);
    }

    public void unblockUser(Long blockerId, Long blockedId) {
        log.info("User {} unblocking user {}", blockerId, blockedId);

        blockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
        log.info("User {} unblocked successfully", blockedId);
    }

    @Transactional(readOnly = true)
    public List<UserDTO.Summary> getBlockedUsers(Long userId) {
        List<UserBlock> blocks = blockRepository.findByBlockerId(userId);

        return blocks.stream()
                .map(block -> userMapper.userToSummary(block.getBlocked()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(Long blockerId, Long blockedId) {
        return blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }
}
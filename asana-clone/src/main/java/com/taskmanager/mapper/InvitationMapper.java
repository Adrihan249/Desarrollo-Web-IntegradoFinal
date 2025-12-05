package com.taskmanager.mapper;

import com.taskmanager.dto.InvitationDTO;
import com.taskmanager.model.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

    // En InvitationMapper.java

// Asumiendo que UserMapper y ProjectMapper están inyectados y son funcionales

    @Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class})
    public interface InvitationMapper {

        @Mapping(source = "sender", target = "sender")
        @Mapping(source = "project", target = "project")
        InvitationDTO.Response invitationToResponse(Invitation invitation);
        InvitationDTO.Response toResponseDTO(Invitation invitation);
        // Si necesitas convertir el DTO de respuesta de invitación a otra cosa, agrégalo aquí.
    }

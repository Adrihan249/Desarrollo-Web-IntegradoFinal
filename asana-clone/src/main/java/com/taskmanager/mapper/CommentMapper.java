package com.taskmanager.mapper;

import com.taskmanager.dto.CommentDTO;
import com.taskmanager.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// 1. Reemplaza @Component con @Mapper.
// 2. Usa componentModel="spring" para que Spring pueda inyectarlo.
// 3. Usa 'uses' para incluir UserMapper, MapStruct gestionará la inyección.
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    // ❌ ELIMINAR: private final UserMapper userMapper;
    // ❌ ELIMINAR: public CommentMapper(UserMapper userMapper) { ... }

    // El método de mapeo debe ser una interfaz (sin cuerpo).
    // Usamos @Mapping para indicar a MapStruct cómo manejar el campo 'author'
    // y mapearlo al campo 'user' del DTO, usando el UserMapper inyectado.
    @Mapping(source = "author", target = "user")
    CommentDTO.Response toResponse(Comment comment);

    // Si necesitas un método para mapear de DTO a Entidad, lo defines aquí también.
}
package com.api.gerenciadorprojetos.Users.Mappers;

import com.api.gerenciadorprojetos.Users.DTO.UserDTO;
import com.api.gerenciadorprojetos.Users.Entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper para converter entidades de usuário em DTOs e vice-versa.
 *
 * @author victor.marcelo
 */
@Mapper
public interface UserMapper {

    UserMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(UserMapper.class);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "nome", target = "nome"),
            @Mapping(source = "email", target = "email")
    })
    UserDTO toDto(User user);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "nome", target = "nome"),
            @Mapping(source = "email", target = "email")
    })
    User toEntity(UserDTO userDTO);
}

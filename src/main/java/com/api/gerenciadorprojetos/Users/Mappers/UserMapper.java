package com.api.gerenciadorprojetos.Users.Mappers;

import com.api.gerenciadorprojetos.Users.DTO.UserDTO;
import com.api.gerenciadorprojetos.Users.Entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(UserMapper.class);


    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "nome", source = "user.nome")
    @Mapping(target = "loginUsuario", source = "user.loginUsuario")
    @Mapping(target = "email", source = "user.email")
    UserDTO toDto(User user);

  /*
    @Mapping(source = "id", target = "id")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "loginUsuario", target = "loginUsuario")
    @Mapping(source = "email", target = "email")
    User toEntity(UserDTO userDTO);
    */

}

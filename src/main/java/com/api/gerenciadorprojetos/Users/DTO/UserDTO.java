package com.api.gerenciadorprojetos.Users.DTO;

import lombok.Data;

/**
 * DTO para representar dados de usuários.
 *
 * @author victor.marcelo
 */
@Data
public class UserDTO {
    private Long id;
    private String nome;
    private String email;
}

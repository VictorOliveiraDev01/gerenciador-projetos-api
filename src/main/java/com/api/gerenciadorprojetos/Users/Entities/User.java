package com.api.gerenciadorprojetos.Users.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa um usuário.
 *
 * @author victor.marcelo
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_usuario", nullable = false, columnDefinition = "VARCHAR")
    @NotBlank(message = "O Nome é obrigatório")
    private String nome;

    @Column(name = "login_usuario", nullable = false, columnDefinition = "VARCHAR")
    @NotBlank(message = "O Login é obrigatório")
    private String loginUsuario;

    @Column(name = "email_usuario", nullable = false, unique = true, columnDefinition = "VARCHAR")
    @NotBlank(message = "O E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;


    @Column(name = "senha_usuario", nullable = false, columnDefinition = "VARCHAR")
    @NotBlank(message = "A Senha é obrigatória")
    private String senha;

    @Column(name = "data_registro_usuario", nullable = false, columnDefinition = "TIMESTAMP(3)")
    private LocalDateTime dataRegistro;

}

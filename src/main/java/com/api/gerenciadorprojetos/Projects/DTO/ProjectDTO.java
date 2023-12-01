package com.api.gerenciadorprojetos.Projects.DTO;

import com.api.gerenciadorprojetos.Users.DTO.UserDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectDTO {
    private Long id;
    private String nomeProjeto;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataTerminoPrevista;
    private String status;
    private UserDTO gerenteProjeto;
    private Double orcamento;
    private String prioridade;
    private List<UserDTO> membrosProjeto;
}

package com.api.gerenciadorprojetos.Tasks.DTO;

import com.api.gerenciadorprojetos.Projects.DTO.ProjectDTO;
import com.api.gerenciadorprojetos.Users.DTO.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    private String nomeTarefa;
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataTerminoPrevista;
    private String status;
    private List<UserDTO> responsaveis;
    private ProjectDTO projeto;
    private LocalDateTime dataConclusao;
    private Integer porcentagemConcluida;
}

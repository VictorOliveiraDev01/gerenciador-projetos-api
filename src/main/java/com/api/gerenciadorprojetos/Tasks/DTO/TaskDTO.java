package com.api.gerenciadorprojetos.Tasks.DTO;

import com.api.gerenciadorprojetos.Projects.DTO.ProjectDTO;
import com.api.gerenciadorprojetos.Tasks.Enums.StatusTarefa;
import com.api.gerenciadorprojetos.Users.DTO.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar dados de Tarefas de Projeto.
 *
 * @author victor.marcelo
 */
@Data
public class TaskDTO {
    private Long id;
    private String nomeTarefa;
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataTerminoPrevista;
    private StatusTarefa status;
    private List<UserDTO> responsaveis;
    private ProjectDTO projeto;
    private LocalDateTime dataConclusao;
    private Integer porcentagemConcluida;
}

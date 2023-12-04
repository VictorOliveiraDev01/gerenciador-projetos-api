package com.api.gerenciadorprojetos.Tasks.Entities;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Tasks.Enums.StatusTarefa;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.api.gerenciadorprojetos.Users.Entities.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entidade que representa uma Tarefa de Projeto.
 *
 * @author victor.marcelo
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tarefas_projeto")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O Nome da Tarefa é obrigatório")
    @Size(max = 255, message = "O Nome da Tarefa deve ter no máximo 255 caracteres")
    @Column(name = "nome_tarefa", nullable = false, length = 255)
    private String nomeTarefa;

    @NotBlank(message = "A Descrição é obrigatória")
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "A Data de Início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @NotNull(message = "A Data de Término Prevista é obrigatória")
    @Column(name = "data_termino_prevista", nullable = false)
    private LocalDate dataTerminoPrevista;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O Status é obrigatório")
    @Column(name = "status", nullable = false)
    private StatusTarefa status;

    @ManyToMany
    @JoinTable(
            name = "responsaveis_tarefa",
            joinColumns = @JoinColumn(name = "id_tarefa"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private Set<User> responsaveis;

    @ManyToOne
    @JoinColumn(name = "id_projeto", nullable = false)
    @NotNull(message = "O Projeto é obrigatório")
    private Project projeto;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "porcentagem_concluida")
    @Min(value = 0, message = "A Porcentagem Concluída não pode ser negativa")
    @Max(value = 100, message = "A Porcentagem Concluída não pode ser superior a 100")
    private Integer porcentagemConcluida;
}

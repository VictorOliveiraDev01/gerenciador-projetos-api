package com.api.gerenciadorprojetos.Projects.Entities;

import com.api.gerenciadorprojetos.Projects.Enums.StatusProjeto;
import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import com.api.gerenciadorprojetos.Users.Entities.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade que representa um projeto.
 *
 * @author victor.marcelo
 */

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "projetos")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O Nome do Projeto é obrigatório")
    @Size(max = 255, message = "O Nome do Projeto deve ter no máximo 255 caracteres")
    @Column(name = "nome_projeto", nullable = false, length = 255)
    private String nomeProjeto;

    @NotBlank(message = "A Descrição é obrigatória")
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "A Data de Início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @NotNull(message = "A Data de Término Prevista é obrigatória")
    @Column(name = "data_termino_prevista", nullable = false)
    private LocalDate dataTerminoPrevista;

    @Column(name = "data_criacao_projeto")
    private LocalDateTime dataCriacaoProjeto;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusProjeto status;

    @ManyToOne
    @JoinColumn(name = "id_gerente_projeto")
    private User gerenteProjeto;

    @ManyToOne
    @JoinColumn(name = "id_criador_projeto")
    private User criadorProjeto;

    @DecimalMin(value = "0.0", message = "O Orçamento não pode ser negativo")
    @Column(name = "orcamento")
    private Double orcamento;

    @Size(max = 50, message = "A Prioridade deve ter no máximo 50 caracteres")
    @Column(name = "prioridade", length = 50)
    private String prioridade;

    @ManyToMany
    @JoinTable(
            name = "membros_projeto",
            joinColumns = @JoinColumn(name = "id_projeto"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private List<User> membrosProjeto;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL)
    private List<Task> tarefas;

    @Column(name = "porcentagem_concluida")
    @Min(value = 0, message = "A Porcentagem Concluída não pode ser negativa")
    @Max(value = 100, message = "A Porcentagem Concluída não pode ser superior a 100")
    private Integer porcentagemConcluida;




}

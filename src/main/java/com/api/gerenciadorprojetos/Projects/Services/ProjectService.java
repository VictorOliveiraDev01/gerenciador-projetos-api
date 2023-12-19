package com.api.gerenciadorprojetos.Projects.Services;

import com.api.gerenciadorprojetos.Exceptions.ProjectValidationException;
import com.api.gerenciadorprojetos.Exceptions.UserValidationException;
import com.api.gerenciadorprojetos.Projects.DTO.ProjectDTO;
import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Projects.Enums.StatusProjeto;
import com.api.gerenciadorprojetos.Projects.Mappers.ProjectMapper;
import com.api.gerenciadorprojetos.Projects.Repositories.ProjectRepository;
import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.Users.Repositories.UserRepository;
import com.api.gerenciadorprojetos.audit.Services.AuditLogService;
import com.api.gerenciadorprojetos.config.RequestInfo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe de serviço para gerenciar operações relacionadas a projetos.
 *
 * @author victor.marcelo
 */
@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private static final int CONCLUIDO_PERCENTAGE = 100;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ProjectMapper projectMapper;
    private final Validator validator;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, AuditLogService auditLogService, ProjectMapper projectMapper, Validator validator) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.projectMapper = projectMapper;
        this.validator = validator;
    }

    /**
     * Recupera uma lista de todos os projetos.
     *
     * @return Lista de ProjectDTOs representando todos os projetos.
     */
    public List<ProjectDTO> findAllProjects() {
        log.info("Listando todos os projetos.");
        return projectRepository.findAll()
                .stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Recupera um Projeto pelo seu ID.
     *
     * @param projectId O ID do projeto a ser recuperado.
     * @return ProjectDTO representando o projeto com o ID especificado.
     * @throws IllegalArgumentException     Se o ID fornecido for nulo.
     * @throws EntityNotFoundException      Se nenhum projeto for encontrado com o ID fornecido.
     */
    public ProjectDTO findProjectById(Long projectId) {
        if (projectId == null) {
            log.error("ID do projeto não fornecido.");
            throw new IllegalArgumentException("Id do projeto não fornecido");
        }

        log.info("Recuperando projeto com ID: {}", projectId);

        return projectMapper.toDto(getProjectById(projectId));
    }

    /**
     * Recupera uma lista de projetos associados a um usuário.
     *
     * @param userId O ID do usuário.
     * @return Lista de ProjectDTOs representando os projetos associados ao usuário.
     * @throws IllegalArgumentException     Se o ID do usuário fornecido for nulo.
     * @throws EntityNotFoundException      Se o usuário não for encontrado.
     */
    public List<ProjectDTO> findProjectsByUser(Long userId) {
        if (userId == null) {
            log.error("ID do usuário não fornecido.");
            throw new IllegalArgumentException("Id de usuário não fornecido");
        }

        log.info("Recuperando projetos associados ao usuário com ID: {}", userId);

        //Verifica se o usuário existe
        getUserById(userId);

        return projectRepository.findProjectsByUser_Id(userId)
                .stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Adiciona um novo projeto.
     *
     * @param project Novo projeto a ser adicionado.
     * @param userId  ID do usuário que está criando o projeto / EXECUTANDO A AÇÃO.
     * @return Projeto recém-criado.
     * @throws UserValidationException     Se a validação do usuário falhar.
     * @throws EntityNotFoundException     Se o usuário não for encontrado.
     * @throws ProjectValidationException  Se a validação do projeto falhar.
     */
    @Transactional
    public Project addNewProject(Project project, Long userId, RequestInfo requestInfo) {
        project.setDataCriacaoProjeto(LocalDateTime.now());

        log.info("Adicionando novo projeto: {}", project);

        validateProject(project);

        User userExecuteAction = getUserById(userId);

        project.setStatus(StatusProjeto.CRIADO);
        project.setPorcentagemConcluida(0);
        project.setCriadorProjeto(userExecuteAction);


        //Adiciona Log do audit
        auditLogService.addAudit(userExecuteAction, "Criação de um novo projeto", null, "Projeto", requestInfo);

        return projectRepository.save(project);
    }

    /**
     * Atualiza um projeto existente.
     *
     * @param projectId   ID do projeto a ser atualizado.
     * @param project     Projeto com as informações atualizadas.
     * @return Projeto atualizado.
     * @throws IllegalArgumentException     Se o ID do projeto fornecido for nulo.
     * @throws EntityNotFoundException      Se o projeto não for encontrado.
     * @throws UserValidationException      Se a validação do usuário falhar.
     * @throws ProjectValidationException   Se a validação do projeto falhar.
     */
    @Transactional
    public Project updateProject(Long projectId, Long userId, Project project, RequestInfo requestInfo) {
        if (projectId == null) {
            log.error("Id de projeto não fornecido");
            throw new IllegalArgumentException("Id de projeto não fornecido");
        }

        log.info("Atualizando projeto com ID: {}", projectId);

        Project projectToUpdate = getProjectById(projectId);

        User userExecuteAction = getUserById(userId);

        validateProject(project);

        projectToUpdate.setNomeProjeto(project.getNomeProjeto());
        projectToUpdate.setDescricao(project.getDescricao());
        projectToUpdate.setDataInicio(project.getDataInicio());
        projectToUpdate.setDataTerminoPrevista(project.getDataTerminoPrevista());
        projectToUpdate.setGerenteProjeto(project.getGerenteProjeto());
        projectToUpdate.setOrcamento(project.getOrcamento());
        projectToUpdate.setPrioridade(project.getPrioridade());
        projectToUpdate.setPorcentagemConcluida(project.getPorcentagemConcluida());

        if (project.getPorcentagemConcluida() > 0) {
            projectToUpdate.setStatus(StatusProjeto.EM_ANDAMENTO);
        } else if (project.getPorcentagemConcluida() == CONCLUIDO_PERCENTAGE) {
            projectToUpdate.setStatus(StatusProjeto.CONCLUIDO);
        }

        String detalhesAlteracao = buildDetalhesAlteracao(projectToUpdate, project);

        //Adiciona log do audit
        auditLogService.addAudit(userExecuteAction,
                "Atualização de um projeto existente. Id do projeto: " + projectId,
                detalhesAlteracao,
                "Projeto",
                requestInfo
        );

        return projectRepository.save(projectToUpdate);
    }

    /**
     * Adiciona um usuário a um projeto.
     *
     * @param userIdAdd  ID do usuário a ser associado ao projeto.
     * @param userId  ID DO usuário que está realizando a ação
     * @param projectId ID do projeto ao qual o usuário será associado.
     * @return Projeto atualizado.
     * @throws IllegalArgumentException       Se os IDs de usuário e projeto não forem fornecidos.
     * @throws EntityNotFoundException        Se o usuário ou o projeto não forem encontrados.
     */
    @Transactional
    public Project addUserToProject(Long userIdAdd, Long userId, Long projectId, RequestInfo requestInfo) throws Exception {
        if (userId == null || projectId == null) {
            log.error("IDs não fornecidos: IDs solicitados: IDs de usuário e projeto");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: Ids de usuário e projeto");
        }

        log.info("Associando usuário com ID {} ao projeto com ID {}", userIdAdd, projectId);

        //Usuário a ser adicionado
        User userForAdd = getUserById(userIdAdd);

        //Usuário que está executando a ação
        User userExecuteAction = getUserById(userId);

        Project projectFilter = getProjectById(projectId);

        List<User> usuariosProjeto = projectFilter.getMembrosProjeto();

        if (!usuariosProjeto.contains(userForAdd)) {
            usuariosProjeto.add(userForAdd);
            projectFilter.setMembrosProjeto(usuariosProjeto);

            auditLogService.addAudit(
                    userExecuteAction,
                    "Adicionando usuario a um projeto ",
                    "Id do usuário adicionado: " + userIdAdd + "," + " Id do projeto: " + projectId,
                    "Projeto",
                    requestInfo
            );

            return projectRepository.save(projectFilter);
        } else {
            log.info("Usuário não adicionado ao projeto. Motivo: Usuário já associado ao projeto.");
            throw new Exception("Usuário já associado ao projeto");
        }
    }

    /**
     * Remove um usuário de um projeto.
     *
     * @param userId    ID do usuário a ser removido do projeto.
     * @param projectId ID do projeto do qual o usuário será removido.
     * @return Projeto atualizado.
     * @throws IllegalArgumentException Se os IDs de usuário e projeto não forem fornecidos.
     * @throws EntityNotFoundException  Se o usuário ou o projeto não forem encontrados.
     */
    @Transactional
    public Project removeUserFromProject(Long userIdRemove, Long userId, Long projectId, RequestInfo requestInfo) {
        if (userId == null || projectId == null) {
            log.error("IDs não fornecidos: IDs solicitados: IDs de usuário e projeto");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: Ids de usuário e projeto");
        }

        log.info("Removendo usuário com ID {} do projeto com ID {}", userId, projectId);

        User userForRemove = getUserById(userIdRemove);

        User userExecuteAction = getUserById(userId);

        Project projectFilter = getProjectById(projectId);

        List<User> usuariosProjeto = projectFilter.getMembrosProjeto();

        if (usuariosProjeto.contains(userForRemove)) {
            usuariosProjeto.remove(userForRemove);
            projectFilter.setMembrosProjeto(usuariosProjeto);
        }

        auditLogService.addAudit(
                userExecuteAction,
                "Removendo usuario de um projeto ",
                "Id do usuário adicionado: " + userIdRemove + "," + " Id do projeto: " + projectId,
                "Projeto",
                requestInfo
        );

        return projectRepository.save(projectFilter);
    }

    /**
     * Define um usuário como gerente de um projeto.
     *
     * @param userId    ID do usuário a ser definido como gerente do projeto.
     * @param projectId ID do projeto ao qual o usuário será definido como gerente.
     * @return Projeto atualizado.
     * @throws IllegalArgumentException Se os IDs de usuário e projeto não forem fornecidos.
     * @throws EntityNotFoundException  Se o usuário ou o projeto não forem encontrados.
     */
    @Transactional
    public Project addProjectManager(Long userIdProjectManager, Long userId, Long projectId, RequestInfo requestInfo) {
        if (userIdProjectManager == null || projectId == null || userId == null) {
            log.error("IDs não fornecidos: IDs solicitados: IDs de usuário e projeto");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: Ids de usuário e projeto");
        }

        log.info("Definindo usuário com ID {} como gerente do projeto com ID {}", userIdProjectManager, projectId);

        User userForAdd = getUserById(userIdProjectManager);

        User userExecuteAction = getUserById(userId);

        Project projectFilter = getProjectById(projectId);

        projectFilter.setGerenteProjeto(userForAdd);

        auditLogService.addAudit(
                userExecuteAction,
                "Adicionando usuario como gerente de um projeto ",
                "Id do usuário adicionado: " + userIdProjectManager + "," + " Id do projeto: " + projectId,
                "Projeto",
                requestInfo
        );

        return projectRepository.save(projectFilter);
    }


    /**
     * Filtra todos os projetos por um status específico.
     *
     * @param status O status pelo qual filtrar os projetos.
     * @return Lista de ProjectDTOs representando projetos filtrados pelo status.
     */
    public List<ProjectDTO> findProjectsByStatus(StatusProjeto status) {
        if(status == null || !EnumUtils.isValidEnum(StatusProjeto.class, status.name())){
            log.info("Status inválido: {}", status);
            throw new IllegalArgumentException("Statua inválido: " + status);
        }
        return projectRepository.findProjectsByStatus(status)
                .stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Filtra projetos associados a um usuário específico por um status específico.
     *
     * @param userId O ID do usuário para o qual recuperar projetos.
     * @param status O status pelo qual filtrar os projetos do usuário.
     * @return Lista de ProjectDTOs representando projetos filtrados pelo status associados ao usuário.
     * @throws IllegalArgumentException     Se o ID do usuário fornecido for nulo.
     * @throws EntityNotFoundException      Se o usuário não for encontrado.
     */
    public List<ProjectDTO> findUserProjectsByStatus(Long userId, StatusProjeto status) {
        if (userId == null) {
            log.info("Id de usuário não fornecido");
            throw new IllegalArgumentException("Id do usuário não fornecido");
        }

        if(status == null || !EnumUtils.isValidEnum(StatusProjeto.class, status.name())){
            log.error("Status inválido: {}", status);
            throw new IllegalArgumentException("Status inválido: " + status);
        }

        log.info("Recuperando projetos do usuário com ID {} com status {}", userId, status);

        //verificando se o usuário existe
        getUserById(userId);

        return projectRepository.findProjectsByUser_IdAndStatus(userId, status)
                .stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }


    /**
     * Deleta um projeto pelo seu ID.
     *
     * @param userId      O ID do Usuário que está executando a ação.
     * @param projectId   O ID do projeto a ser excluído.
     * @param requestInfo Informações da solicitação.
     * @throws IllegalArgumentException   Se o ID do projeto fornecido for nulo.
     * @throws EntityNotFoundException    Se o projeto não for encontrado para exclusão.
     * @throws RuntimeException           Se ocorrer um erro ao deletar o projeto.
     */
    public void deleteProjectById(Long userId, Long projectId, RequestInfo requestInfo) {
        if (projectId == null) {
            log.error("Id do projeto não fornecido. Id fornecido: {} ", projectId);
            throw new IllegalArgumentException("Id do projeto não fornecido");
        }

        User userExecuteAction = getUserById(userId);

        try {
            Project projectToDelete = getProjectById(projectId);

            projectRepository.delete(projectToDelete);

            auditLogService.addAudit(
                    userExecuteAction,
                    "Deletando projeto",
                    "Id do projeto deletado: " + projectId,
                    "Projeto",
                    requestInfo
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao deletar projeto. Causa: " + ex.getMessage(), ex);
        }
    }


    /**
     * Atualiza automaticamente o status dos projetos atrasados.
     * Este método é chamado por um JOB todos os dias à 00:00h.
     */
    @Transactional
    public void updateLateProjectsStatus() {
        log.info("Atualizando status dos projetos atrasados. JOB executado todos os dias a 00:00h");

        LocalDate currentDate = LocalDate.now();

        List<Project> lateProjects = projectRepository.findAll()
                .stream()
                .filter(p -> p.getDataTerminoPrevista() != null && p.getDataTerminoPrevista().isBefore(currentDate))
                .toList();

        for (Project project : lateProjects) {
            project.setStatus(StatusProjeto.ATRASADO);
            projectRepository.save(project);
        }
    }

    /**
     * Valida um projeto antes de adicioná-lo.
     *
     * @param project O projeto a ser validado.
     * @throws ProjectValidationException       Se o projeto não atender às validações.
     */
    private void validateProject(Project project) throws ProjectValidationException {
        log.info("Validando projeto: {}", project);

        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        if (!violations.isEmpty()) {
            log.info("Projeto inválido. Motivos: {}", violations);
            throw new ProjectValidationException("Erro de validação ao adicionar um novo projeto", violations);
        }
    }

    /**
     * Constrói uma mensagem detalhada indicando quais campos foram alterados em um projeto (PARA ARMAZENAMENTO EM LOG DE AUDIT).
     *
     * @param projetoAntigo O projeto antes da atualização.
     * @param projetoNovo   O projeto após a atualização.
     * @return Uma mensagem detalhada das alterações nos campos.
     */
    private String buildDetalhesAlteracao(Project projetoAntigo, Project projetoNovo) {
        StringBuilder detalhesAlteracao = new StringBuilder("Campos Alterados: ");

        if (!Objects.equals(projetoAntigo.getNomeProjeto(), projetoNovo.getNomeProjeto())) {
            detalhesAlteracao.append("Nome: ").append(projetoNovo.getNomeProjeto()).append(", ");
        }
        if (!Objects.equals(projetoAntigo.getDescricao(), projetoNovo.getDescricao())) {
            detalhesAlteracao.append("Descrição: ").append(projetoNovo.getDescricao()).append(", ");
        }
        if (!Objects.equals(projetoAntigo.getDataInicio(), projetoNovo.getDataInicio())) {
            detalhesAlteracao.append("Data de Início: ").append(projetoNovo.getDataInicio()).append(", ");
        }
        if (!Objects.equals(projetoAntigo.getDataTerminoPrevista(), projetoNovo.getDataTerminoPrevista())) {
            detalhesAlteracao.append("Data de Término Prevista: ").append(projetoNovo.getDataTerminoPrevista()).append(", ");
        }
        if (!Objects.equals(projetoAntigo.getGerenteProjeto(), projetoNovo.getGerenteProjeto())) {
            detalhesAlteracao.append("Gerente do Projeto: ").append(projetoNovo.getGerenteProjeto()).append(", ");
        }
        if (!Objects.equals(projetoAntigo.getOrcamento(), projetoNovo.getOrcamento())) {
            detalhesAlteracao.append("Orçamento: ").append(projetoNovo.getOrcamento()).append(", ");
        }
        if (!Objects.equals(projetoAntigo.getPrioridade(), projetoNovo.getPrioridade())) {
            detalhesAlteracao.append("Prioridade: ").append(projetoNovo.getPrioridade()).append(", ");
        }
        if (!Objects.equals(projetoAntigo.getPorcentagemConcluida(), projetoNovo.getPorcentagemConcluida())) {
            detalhesAlteracao.append("Porcentagem Concluída: ").append(projetoNovo.getPorcentagemConcluida()).append(", ");
        }

        return detalhesAlteracao.toString();
    }

    /**
     * Obtém o usuário pelo ID.
     *
     * @param userId O ID do usuário a ser recuperado.
     * @return O objeto User correspondente ao ID fornecido.
     * @throws IllegalArgumentException Se o ID do usuário não for informado.
     * @throws EntityNotFoundException Se nenhum usuário for encontrado com o ID fornecido.
     */
    private User getUserById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Id de usuário não informado");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Usuário informado não encontrado. Id fornecido {}", userId);
                    return new EntityNotFoundException("Usuário não encontrado");
                });
    }

    /**
     * Obtém o projeto pelo ID.
     *
     * @param projectId O ID do projeto a ser recuperado.
     * @return O objeto Project correspondente ao ID fornecido.
     * @throws IllegalArgumentException Se o ID do projeto não for informado.
     * @throws EntityNotFoundException Se nenhum projeto for encontrado com o ID fornecido.
     */
    private Project getProjectById(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Id de projeto não informado");
        }

        return projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.info("Projeto informado não encontrado. Id fornecido {}", projectId);
                    return new EntityNotFoundException("Projeto não encontrado");
                });
    }
}
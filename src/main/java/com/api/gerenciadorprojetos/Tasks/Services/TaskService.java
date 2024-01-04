package com.api.gerenciadorprojetos.Tasks.Services;

import com.api.gerenciadorprojetos.Exceptions.TaskValidationException;
import com.api.gerenciadorprojetos.Exceptions.UnauthorizedException;
import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Projects.Repositories.ProjectRepository;
import com.api.gerenciadorprojetos.Tasks.DTO.TaskDTO;
import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import com.api.gerenciadorprojetos.Tasks.Enums.StatusTarefa;
import com.api.gerenciadorprojetos.Tasks.Mappers.TaskMapper;
import com.api.gerenciadorprojetos.Tasks.Repositories.TaskRepository;
import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.Users.Repositories.UserRepository;
import com.api.gerenciadorprojetos.Utils.EntityServiceUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe de serviço para gerenciar operações relacionadas a tarefas de projeto.
 *
 * @author victor.marcelo
 */
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private final EntityServiceUtils entityServiceUtils;
    private final AuditLogService auditLogService;
    private final TaskMapper taskMapper;
    private final Validator validator;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       AuditLogService auditLogService,
                       TaskMapper taskMapper,
                       Validator validator,
                       EntityServiceUtils entityServiceUtils)
    {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.taskMapper = taskMapper;
        this.validator = validator;
        this.entityServiceUtils = entityServiceUtils;
    }

    /**
     * Recupera uma lista de todas as tarefas.
     *
     * @return Lista de TaskDTOs representando todas as tarefas.
     */
    public List<TaskDTO> findAllTasks() {
        log.info("Listando todas as tarefas.");
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Recupera uma tarefa pelo seu ID.
     *
     * @param taskId O ID da tarefa a ser recuperada.
     * @return TaskDTO representando a tarefa com o ID especificado.
     * @throws IllegalArgumentException Se o ID fornecido for nulo.
     * @throws EntityNotFoundException  Se nenhuma tarefa for encontrada com o ID fornecido.
     */
    public TaskDTO findTaskById(Long taskId) {
        if (taskId == null) {
            log.error("ID da tarefa não fornecido.");
            throw new IllegalArgumentException("Id da tarefa não fornecido");
        }

        log.info("Recuperando tarefa com ID: {}", taskId);

        return taskMapper.toDto(entityServiceUtils.getTaskById(taskId));
    }

    /**
     * Adiciona uma nova tarefa ao projeto, permitindo que o gerente do projeto escolha o responsável.
     *
     * @param task         Nova tarefa a ser adicionada.
     * @param userId       ID do usuário que está criando a tarefa (gerente do projeto).
     * @param responsibleId ID do usuário que será responsável pela tarefa.
     * @return Tarefa recém-criada.
     * @throws EntityNotFoundException Se o usuário, o projeto associado à tarefa ou o usuário responsável não for encontrado.
     * @throws TaskValidationException Se a validação da tarefa falhar.
     * @throws UnauthorizedException  Se o usuário não for o gerente do projeto associado.
     */
    @Transactional
    public Task addNewTask(Task task, Long userId, Long responsibleId, RequestInfo requestInfo) {

        Project projetoAssociado = entityServiceUtils.getProjectById(task.getProjeto().getId());

        if (!projetoAssociado.getGerenteProjeto().getId().equals(userId)) {
            log.info("Usuário não autorizado para criar tarefas neste projeto.");
            throw new UnauthorizedException("Usuário não autorizado para criar tarefas neste projeto");
        }

        task.setStatus(StatusTarefa.PENDENTE);
        task.setDataConclusao(null);

        log.info("Adicionando nova tarefa: {}", task);

        validateTask(task);

        User usuarioResponsavel = entityServiceUtils.getUserById(responsibleId);

        User userExecuteAction = entityServiceUtils.getUserById(userId);

        task.setResponsaveis(Set.of(usuarioResponsavel));

        // Define o projeto associado à tarefa
        task.setProjeto(projetoAssociado);

        auditLogService.addAudit(userExecuteAction,
                "Criação de uma nova tarefa no projeto",
                "Id do projeto: " + projetoAssociado.getId(),
                "Projeto", requestInfo
        );

        return taskRepository.save(task);
    }


    /**
     * Atualiza uma tarefa existente.
     *
     * @param taskId O ID da tarefa a ser atualizada.
     * @param userId O ID do usuário que está executando a ação
     * @param task   Tarefa com as informações atualizadas.
     * @param requestInfo Informações do usuário que fez a solicitação.
     *
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException Se o ID da tarefa fornecido for nulo.
     * @throws EntityNotFoundException  Se a tarefa não for encontrada.
     * @throws TaskValidationException  Se a validação da tarefa falhar.
     */
    @Transactional
    public Task updateTask(Long taskId, Long userId, Task task, RequestInfo requestInfo) {
        if (taskId == null) {
            log.error("Id da tarefa não fornecido");
            throw new IllegalArgumentException("Id da tarefa não fornecido");
        }

        log.info("Atualizando tarefa com ID: {}", taskId);

        Task taskToUpdate = entityServiceUtils.getTaskById(taskId);

        User userExecuteAction = entityServiceUtils.getUserById(userId);

        Project project = entityServiceUtils.getProjectById(taskToUpdate.getProjeto().getId());

        validateTask(task);

        taskToUpdate.setNomeTarefa(task.getNomeTarefa());
        taskToUpdate.setDescricao(task.getDescricao());
        taskToUpdate.setDataInicio(task.getDataInicio());
        taskToUpdate.setDataTerminoPrevista(task.getDataTerminoPrevista());
        taskToUpdate.setProjeto(task.getProjeto());
        taskToUpdate.setPorcentagemConcluida(task.getPorcentagemConcluida());

        if (task.getPorcentagemConcluida() != null && task.getPorcentagemConcluida() == 100) {
            taskToUpdate.setStatus(StatusTarefa.CONCLUIDA);
            taskToUpdate.setDataConclusao(LocalDateTime.now());
        } else {
            taskToUpdate.setStatus(StatusTarefa.EM_ANDAMENTO);
            taskToUpdate.setDataConclusao(null);
        }

        String detalhesAlteracao = buildDetalhesAlteracao(taskToUpdate, task);

        auditLogService.addAudit(userExecuteAction,
                "Atualização de uma tarefa de projeto existente. Id da tarefa: " + taskId + ", Id do projeto a qual a tarefa pertence: " + project.getId(),
                "Campos Alterados: " + detalhesAlteracao,
                "Tarefa",
                requestInfo
        );

        return taskRepository.save(taskToUpdate);
    }

    /**
     * Adiciona usuários responsáveis a uma tarefa.
     *
     * @param taskId  O ID da tarefa.
     * @param userId  O ID do usuário que está executando a ação
     * @param userIds Lista de IDs de usuários a serem associados à tarefa.
     * @param requestInfo Informações sobre a requisição (Para armazenamento no audit)
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException Se o ID da tarefa ou a lista de IDs de usuário não forem fornecidos.
     * @throws EntityNotFoundException  Se a tarefa ou algum usuário não for encontrado.
     */
    @Transactional
    public Task addUsersToTask(Long taskId, Long userId, List<Long> userIds, RequestInfo requestInfo) {
        if (taskId == null || userIds == null || userIds.isEmpty()) {
            log.error("IDs não fornecidos: IDs solicitados: IDs de tarefa e usuários");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: Ids de tarefa e usuários");
        }

        log.info("Associando usuários com IDs {} à tarefa com ID {}", userIds, taskId);

        Task taskToAddUsers = entityServiceUtils.getTaskById(taskId);

        User userExecuteAction = entityServiceUtils.getUserById(userId);

        List<User> usuariosResponsaveis = userRepository.findAllById(userIds);

        if (usuariosResponsaveis.isEmpty()) {
            log.info("Nenhum usuário encontrado para associar à tarefa.");
            throw new EntityNotFoundException("Nenhum usuário encontrado para associar à tarefa");
        }

        taskToAddUsers.getResponsaveis().addAll(usuariosResponsaveis);

        auditLogService.addAudit(
                userExecuteAction,
                "Adicionando usuários a tarefa",
                "Ids dos usuários Adicionados: " + Arrays.toString(userIds.toArray()) + ". Id da Tarefa a qual foram adicionados: " + taskId,
                "Tarefa",
                requestInfo
        );

        return taskRepository.save(taskToAddUsers);
    }

    /**
     * Remove usuários responsáveis de uma tarefa.
     *
     * @param taskId  O ID da tarefa.
     * @param userId  O ID do usuário que está executando a ação.
     * @param userIds Lista de IDs de usuários a serem removidos da tarefa.
     * @param requestInfo Informações sobre a requisição (Para armazenamento no audit).
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException Se o ID da tarefa ou a lista de IDs de usuário não forem fornecidos.
     * @throws EntityNotFoundException  Se a tarefa ou algum usuário não for encontrado.
     */
    @Transactional
    public Task removeUsersFromTask(Long taskId, Long userId, List<Long> userIds, RequestInfo requestInfo) {
        if (taskId == null || userIds == null || userIds.isEmpty()) {
            log.error("IDs não fornecidos: IDs solicitados: IDs de tarefa e usuários");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: Ids de tarefa e usuários");
        }

        log.info("Removendo usuários com IDs {} da tarefa com ID {}", userIds, taskId);

        Task taskToRemoveUsers = entityServiceUtils.getTaskById(taskId);

        User userExecuteAction = entityServiceUtils.getUserById(userId);

        List<User> usuariosResponsaveis = userRepository.findAllById(userIds);

        if (usuariosResponsaveis.isEmpty()) {
            log.info("Nenhum usuário encontrado para remover da tarefa.");
            throw new EntityNotFoundException("Nenhum usuário encontrado para remover da tarefa");
        }

        taskToRemoveUsers.getResponsaveis().removeAll(usuariosResponsaveis);

        auditLogService.addAudit(
                userExecuteAction,
                "Removendo usuários da tarefa",
                "Ids dos usuários Removidos: " + Arrays.toString(userIds.toArray()) + ". Id da Tarefa a qual foram removidos: " + taskId,
                "Tarefa",
                requestInfo
        );

        return taskRepository.save(taskToRemoveUsers);
    }


    /**
     * Define uma tarefa como concluída.
     *
     * @param taskId O ID da tarefa.
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException    Se o ID da tarefa não for fornecido.
     * @throws EntityNotFoundException     Se a tarefa não for encontrada.
     * @throws TaskValidationException      Se a validação da tarefa falhar.
     */
    @Transactional
    public Task completeTask(Long taskId) {
        if (taskId == null) {
            log.error("ID da tarefa não fornecido.");
            throw new IllegalArgumentException("Id da tarefa não fornecido");
        }

        log.info("Marcando tarefa com ID {} como concluída", taskId);

        Task taskToComplete = entityServiceUtils.getTaskById(taskId);

        validateTask(taskToComplete);

        taskToComplete.setStatus(StatusTarefa.CONCLUIDA);
        taskToComplete.setDataConclusao(LocalDateTime.now());

        return taskRepository.save(taskToComplete);
    }



    /**
     * Recupera as tarefas de projeto associadas a um usuário específico por um status específico.
     *
     * @param userId O ID do usuário para o qual recuperar projetos.
     * @param projectId O ID do projeto a qual pertencem as tarefas.
     * @param status O status pelo qual filtrar as tarefas de projeto do usuário.
     * @return Lista de ProjectDTOs representando projetos filtrados pelo status associados ao usuário.
     * @throws IllegalArgumentException     Se o ID do usuário fornecido for nulo.
     * @throws EntityNotFoundException      Se o usuário não for encontrado.
     */
    public List<TaskDTO> findUserTasksByStatusAndProject(Long userId, Long projectId, StatusTarefa status) {
        if (userId == null || projectId == null) {
            log.error("IDs não fornecidos: IDs solicitados: ID de projeto e usuário");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: ID de projeto e usuário");
        }

        if (status == null || !EnumUtils.isValidEnum(StatusTarefa.class, status.name())) {
            log.error("Status inválido: {}", status);
            throw new IllegalArgumentException("Status inválido: " + status);
        }

        log.info("Recuperando tarefas do projeto com ID {}, do usuário com ID {}, com status {}", projectId, userId, status);

        //Verifica se usuário existe
        entityServiceUtils.getProjectById(userId);

        //Verifica se o projeto existe
        entityServiceUtils.getProjectById(projectId);

        return taskRepository.findUserTasksByStatusAndProject(userId, projectId, status)
                .stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }


    /**
     * Recupera uma lista de tarefas de projeto associadas a um usuário.
     *
     * @param userId O ID do usuário.
     * @param projectId O ID do projeto.
     * @return Lista de TasksDTO representando as tarefas de projeto associados ao usuário e ao projeto.
     * @throws IllegalArgumentException     Se o ID do usuário fornecido for nulo.
     * @throws EntityNotFoundException      Se o usuário não for encontrado.
     */
    public List<TaskDTO> findTasksByUserAndProject(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            log.error("IDs não fornecidos: IDs solicitados: ID de projeto e usuário");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: ID de projeto e usuário");
        }

        log.info("Recuperando tarefas de projeto associadas ao usuário com ID: {}", userId);

        //Verifica se o usuário existe
        entityServiceUtils.getUserById(userId);

        return taskRepository.findByUserIdAndProjectId(userId, projectId)
                .stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Deleta um projeto pelo seu ID.
     *
     * @param userId O ID do usuário que esta executando a ação
     * @param taskId O ID da tarefa a ser excluída.
     * @param requestInfo Informações sobre a requisição (Para armazenamento no audit).
     *
     * @throws IllegalArgumentException Se o ID da tarefa fornecido for nulo.
     * @throws EntityNotFoundException Se a Tarefa não for encontrada para exclusão.
     * @throws RuntimeException Se ocorrer um erro ao deletar a tarefa.
     */
    public void deleteTaskById(Long userId, Long taskId, RequestInfo requestInfo) {
        if (taskId == null || userId == null) {
            log.error("Ids não fornecidos. Ids solicitados: Id da tarefa e Id do usuário");
            throw new IllegalArgumentException("Ids não fornecidos. Ids solicitados: Id da tarefa e Id do usuário");
        }

        User userExecuteAction = entityServiceUtils.getUserById(userId);

        try {
            Task taskToDelete = entityServiceUtils.getTaskById(taskId);

            taskRepository.deleteById(taskId);
            auditLogService.addAudit(
                    userExecuteAction,
                    "Deletando Tarefa de projeto",
                    "Id da tarefa projeto deletada: " + taskId + ". Id do projeto a qual a tarefa pertencia: " + taskToDelete.getProjeto().getId(),
                    "Tarefa",
                    requestInfo
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao deletar tarefa. Causa: " + ex.getMessage(), ex);
        }
    }

    /**
     * Valida uma tarefa.
     *
     * @param task A tarefa a ser validada.
     * @throws TaskValidationException Se a validação da tarefa falhar.
     */
    private void validateTask(Task task) throws TaskValidationException {
        log.info("Validando tarefa: {}", task);

        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        if (!violations.isEmpty()) {
            log.info("Tarefa inválida. Motivos: {}", violations);
            throw new TaskValidationException("Erro de validação na tarefa. Motivos: {}", violations);
        }
    }

    /**
     * Atualiza automaticamente o status das tarefas de projeto atrasadas.
     * Este método é chamado por um JOB todos os dias à 00:00h.
     */
    @Transactional
    public void updateLateTaskStatus() {
        log.info("Atualizando status das tarefas atrasadas. JOB executado todos os dias a 00:00h");

        LocalDate currentDate = LocalDate.now();

        List <Task> lateTasks = taskRepository.findAll()
                .stream()
                .filter(p -> p.getDataTerminoPrevista() != null && p.getDataTerminoPrevista().isBefore(currentDate))
                .collect(Collectors.toList());

        for (Task task : lateTasks) {
            task.setStatus(StatusTarefa.ATRASADA);
            taskRepository.save(task);
        }
    }


    /**
     * Constrói uma mensagem detalhada indicando quais campos foram alterados em uma tarefa (PARA ARMAZENAMENTO EM LOG DE AUDIT).
     *
     * @param tarefaAntiga A tarefa antes da atualização.
     * @param tarefaNova   A tarefa após a atualização.
     * @return Uma mensagem detalhada das alterações nos campos.
     */
    public String buildDetalhesAlteracao(Task tarefaAntiga, Task tarefaNova) {
        StringBuilder detalhesAlteracao = new StringBuilder("Campos Alterados: ");

        if (!Objects.equals(tarefaAntiga.getNomeTarefa(), tarefaNova.getNomeTarefa())) {
            detalhesAlteracao.append("Nome da Tarefa: ").append(tarefaNova.getNomeTarefa()).append(", ");
        }
        if (!Objects.equals(tarefaAntiga.getDescricao(), tarefaNova.getDescricao())) {
            detalhesAlteracao.append("Descrição: ").append(tarefaNova.getDescricao()).append(", ");
        }
        if (!Objects.equals(tarefaAntiga.getDataInicio(), tarefaNova.getDataInicio())) {
            detalhesAlteracao.append("Data de Início: ").append(tarefaNova.getDataInicio()).append(", ");
        }
        if (!Objects.equals(tarefaAntiga.getDataTerminoPrevista(), tarefaNova.getDataTerminoPrevista())) {
            detalhesAlteracao.append("Data de Término Prevista: ").append(tarefaNova.getDataTerminoPrevista()).append(", ");
        }
        if (!Objects.equals(tarefaAntiga.getStatus(), tarefaNova.getStatus())) {
            detalhesAlteracao.append("Status: ").append(tarefaNova.getStatus()).append(", ");
        }
        if (!Objects.equals(tarefaAntiga.getResponsaveis(), tarefaNova.getResponsaveis())) {
            detalhesAlteracao.append("Responsáveis: ").append(tarefaNova.getResponsaveis()).append(", ");
        }
        if (!Objects.equals(tarefaAntiga.getProjeto(), tarefaNova.getProjeto())) {
            detalhesAlteracao.append("Projeto: ").append(tarefaNova.getProjeto()).append(", ");
        }
        if (!Objects.equals(tarefaAntiga.getDataConclusao(), tarefaNova.getDataConclusao())) {
            detalhesAlteracao.append("Data de Conclusão: ").append(tarefaNova.getDataConclusao()).append(", ");
        }
        if (!Objects.equals(tarefaAntiga.getPorcentagemConcluida(), tarefaNova.getPorcentagemConcluida())) {
            detalhesAlteracao.append("Porcentagem Concluída: ").append(tarefaNova.getPorcentagemConcluida()).append(", ");
        }

        return detalhesAlteracao.toString();
    }

}
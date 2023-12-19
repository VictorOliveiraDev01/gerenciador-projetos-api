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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe de serviço para gerenciar operações relacionadas a tarefas.
 *
 * @author victor.marcelo
 */
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private final AuditLogService auditLogService;
    private final TaskMapper taskMapper;
    private final Validator validator;

    @Autowired
    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository, AuditLogService auditLogService, TaskMapper taskMapper, Validator validator) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.taskMapper = taskMapper;
        this.validator = validator;
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

        return taskMapper.toDto(
                taskRepository.findById(taskId)
                        .orElseThrow(() -> {
                            log.info("Tarefa não encontrada. Id fornecido: {}", taskId);
                            return new EntityNotFoundException("Tarefa não encontrada. Id fornecido: " + taskId);
                        })
        );
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
    public Task addNewTask(Task task, Long userId, Long responsibleId) {

        Project projetoAssociado = projectRepository.findById(task.getProjeto().getId())
                .orElseThrow(() -> {
                    log.info("Projeto não encontrado.");
                    return new EntityNotFoundException("Projeto não encontrado");
                });

        if (!projetoAssociado.getGerenteProjeto().getId().equals(userId)) {
            log.info("Usuário não autorizado para criar tarefas neste projeto.");
            throw new UnauthorizedException("Usuário não autorizado para criar tarefas neste projeto");
        }

        task.setStatus(StatusTarefa.PENDENTE);
        task.setDataConclusao(null);

        log.info("Adicionando nova tarefa: {}", task);

        validateTask(task);

        User usuarioResponsavel = userRepository.findById(responsibleId)
                .orElseThrow(() -> {
                    log.info("Usuário responsável não encontrado.");
                    return new EntityNotFoundException("Usuário responsável não encontrado");
                });

        task.setResponsaveis(Set.of(usuarioResponsavel));

        // Define o projeto associado à tarefa
        task.setProjeto(projetoAssociado);

        return taskRepository.save(task);
    }


    /**
     * Atualiza uma tarefa existente.
     *
     * @param taskId O ID da tarefa a ser atualizada.
     * @param task   Tarefa com as informações atualizadas.
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException Se o ID da tarefa fornecido for nulo.
     * @throws EntityNotFoundException  Se a tarefa não for encontrada.
     * @throws TaskValidationException  Se a validação da tarefa falhar.
     */
    @Transactional
    public Task updateTask(Long taskId, Task task) {
        if (taskId == null) {
            log.error("Id da tarefa não fornecido");
            throw new IllegalArgumentException("Id da tarefa não fornecido");
        }

        log.info("Atualizando tarefa com ID: {}", taskId);

        Task tarefaEncontrada = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.info("Tarefa não encontrada.");
                    return new EntityNotFoundException("Tarefa não encontrada");
                });

        validateTask(task);

        tarefaEncontrada.setNomeTarefa(task.getNomeTarefa());
        tarefaEncontrada.setDescricao(task.getDescricao());
        tarefaEncontrada.setDataInicio(task.getDataInicio());
        tarefaEncontrada.setDataTerminoPrevista(task.getDataTerminoPrevista());
        tarefaEncontrada.setProjeto(task.getProjeto());
        tarefaEncontrada.setPorcentagemConcluida(task.getPorcentagemConcluida());

        if (task.getPorcentagemConcluida() != null && task.getPorcentagemConcluida() == 100) {
            tarefaEncontrada.setStatus(StatusTarefa.CONCLUIDA);
            tarefaEncontrada.setDataConclusao(LocalDateTime.now());
        } else {
            tarefaEncontrada.setStatus(StatusTarefa.EM_ANDAMENTO);
            tarefaEncontrada.setDataConclusao(null);
        }

        return taskRepository.save(tarefaEncontrada);
    }

    /**
     * Adiciona usuários responsáveis a uma tarefa.
     *
     * @param taskId  O ID da tarefa.
     * @param userIds Lista de IDs de usuários a serem associados à tarefa.
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException Se o ID da tarefa ou a lista de IDs de usuário não forem fornecidos.
     * @throws EntityNotFoundException  Se a tarefa ou algum usuário não for encontrado.
     */
    @Transactional
    public Task addUsersToTask(Long taskId, List<Long> userIds) {
        if (taskId == null || userIds == null || userIds.isEmpty()) {
            log.error("IDs não fornecidos: IDs solicitados: IDs de tarefa e usuários");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: Ids de tarefa e usuários");
        }

        log.info("Associando usuários com IDs {} à tarefa com ID {}", userIds, taskId);

        Task tarefaEncontrada = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.info("Tarefa não encontrada.");
                    return new EntityNotFoundException("Tarefa não encontrada");
                });

        List<User> usuariosResponsaveis = userRepository.findAllById(userIds);

        if (usuariosResponsaveis.isEmpty()) {
            log.info("Nenhum usuário encontrado para associar à tarefa.");
            throw new EntityNotFoundException("Nenhum usuário encontrado para associar à tarefa");
        }

        tarefaEncontrada.getResponsaveis().addAll(usuariosResponsaveis);

        return taskRepository.save(tarefaEncontrada);
    }

    /**
     * Remove usuários responsáveis de uma tarefa.
     *
     * @param taskId  O ID da tarefa.
     * @param userIds Lista de IDs de usuários a serem removidos da tarefa.
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException Se o ID da tarefa ou a lista de IDs de usuário não forem fornecidos.
     * @throws EntityNotFoundException  Se a tarefa ou algum usuário não for encontrado.
     */
    @Transactional
    public Task removeUsersFromTask(Long taskId, List<Long> userIds) {
        if (taskId == null || userIds == null || userIds.isEmpty()) {
            log.error("IDs não fornecidos: IDs solicitados: IDs de tarefa e usuários");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: Ids de tarefa e usuários");
        }

        log.info("Removendo usuários com IDs {} da tarefa com ID {}", userIds, taskId);

        Task tarefaEncontrada = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.info("Tarefa não encontrada.");
                    return new EntityNotFoundException("Tarefa não encontrada");
                });

        List<User> usuariosResponsaveis = userRepository.findAllById(userIds);

        if (usuariosResponsaveis.isEmpty()) {
            log.info("Nenhum usuário encontrado para remover da tarefa.");
            throw new EntityNotFoundException("Nenhum usuário encontrado para remover da tarefa");
        }

        tarefaEncontrada.getResponsaveis().removeAll(usuariosResponsaveis);

        return taskRepository.save(tarefaEncontrada);
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

        Task tarefaEncontrada = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.info("Tarefa não encontrada.");
                    return new EntityNotFoundException("Tarefa não encontrada");
                });

        validateTask(tarefaEncontrada);

        tarefaEncontrada.setStatus(StatusTarefa.CONCLUIDA);
        tarefaEncontrada.setDataConclusao(LocalDateTime.now());

        return taskRepository.save(tarefaEncontrada);
    }

    /**
     * Adiciona um responsável à tarefa.
     *
     * @param taskId O ID da tarefa.
     * @param userIdAdd O ID do usuário a ser adicionado como responsável.
     * @param userId O ID do usuário que está executando a ação.
     * @param requestInfo Informações sobre a requisição (Para armazenamento no audit)
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException    Se o ID da tarefa ou do usuário não forem fornecidos.
     * @throws EntityNotFoundException     Se a tarefa ou o usuário não for encontrado.
     * @throws TaskValidationException      Se a validação da tarefa falhar.
     */
    @Transactional
    public Task addResponsibleToTask(Long taskId, Long userIdAdd, Long userId, RequestInfo requestInfo) {
        if (taskId == null || userIdAdd == null || userId == null) {
            log.error("IDs não fornecidos: IDs solicitados: ID de tarefa e usuário");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: ID de tarefa e usuário");
        }

        log.info("Adicionando usuário com ID {} como responsável à tarefa com ID {}", userId, taskId);

        Task tarefa = getTaskById(taskId);

        User usuarioResponsavel = getUserById(userIdAdd);

        User userExecuteAction = getUserById(userId);

        tarefa.getResponsaveis().add(usuarioResponsavel);

        auditLogService.addAudit(
                userExecuteAction,
                "Adicionando usuário a tarefa", "Id do usuário Adicionado: " + userIdAdd + ". Id da Tarefa a qual foi adicionado: " + taskId,
                "Tarefa",
                requestInfo
        );

        return taskRepository.save(tarefa);
    }

    /**
     * Remove um responsável de uma tarefa.
     *
     * @param taskId O ID da tarefa.
     * @param userIdRemove O ID do usuário a ser removido como responsável.
     * @param userId O ID do usuário que está executando a ação.
     * @param requestInfo Informações sobre a requisição (Para armazenamento no audit)
     * @return Tarefa atualizada.
     * @throws IllegalArgumentException    Se o ID da tarefa ou do usuário não forem fornecidos.
     * @throws EntityNotFoundException     Se a tarefa ou o usuário não for encontrado.
     */
    @Transactional
    public Task removeResponsibleFromTask(Long taskId, Long userIdRemove, Long userId, RequestInfo requestInfo) {
        if (taskId == null || userIdRemove == null || userId == null) {
            log.error("IDs não fornecidos: IDs solicitados: ID de tarefa e usuário");
            throw new IllegalArgumentException("Ids não fornecidos: Ids solicitados: ID de tarefa e usuário");
        }

        log.info("Removendo usuário com ID {} como responsável da tarefa com ID {}", userId, taskId);

        Task tarefa = getTaskById(taskId);

        User usuarioResponsavel = getUserById(userId);

        User userExecuteAction = getUserById(userId);

        tarefa.getResponsaveis().remove(usuarioResponsavel);

        auditLogService.addAudit(
                userExecuteAction,
                "Removendo usuário da tarefa", "Id do usuário Removido: " + userIdRemove + ". Id da Tarefa a qual pertencia: " + taskId,
                "Tarefa",
                requestInfo
        );

        return taskRepository.save(tarefa);
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
        getProjectById(userId);

        //Verifica se o projeto existe
        getProjectById(projectId);

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
        getUserById(userId);

        return taskRepository.findByUserIdAndProjectId(userId, projectId)
                .stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Deleta um projeto pelo seu ID.
     *
     * @param taskId O ID da tarefa a ser excluída.
     * @throws IllegalArgumentException Se o ID da tarefa fornecido for nulo.
     * @throws EntityNotFoundException Se a Tarefa não for encontrada para exclusão.
     * @throws RuntimeException Se ocorrer um erro ao deletar a tarefa.
     */
    public void deleteTaskById(Long userId, Long taskId, RequestInfo requestInfo) {
        if (taskId == null || userId == null) {
            log.error("Ids não fornecidos. Ids solicitados: Id da tarefa e Id do usuário");
            throw new IllegalArgumentException("Ids não fornecidos. Ids solicitados: Id da tarefa e Id do usuário");
        }

        User userExecuteAction = getUserById(userId);

        try {
            Task taskToDelete = getTaskById(taskId);

            taskRepository.deleteById(taskId);
            auditLogService.addAudit(
                    userExecuteAction,
                    "Deletando Tarefa de projeto",
                    "Id da tarefa projeto deletada: " + taskId + ". Id do projeto a qual a tarefa pertencia: " + taskToDelete.getProjeto().getId(),
                    "Projeto",
                    requestInfo
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao deletar tarefa. Causa: " + ex.getMessage(), ex);
        }
    }

    /**
     * Valida a conclusão de uma tarefa.
     *
     * @param task A tarefa a ser validada.
     * @throws TaskValidationException Se a validação da tarefa falhar.
     */
    private void validateTask(Task task) throws TaskValidationException {
        log.info("Validando conclusão da tarefa: {}", task);

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

    /**
     * Obtém o projeto pelo ID.
     *
     * @param taskId O ID da tarefa de projeto a ser recuperada.
     * @return O objeto Task correspondente ao ID fornecido.
     * @throws IllegalArgumentException Se o ID da tarefa não for informado.
     * @throws EntityNotFoundException Se nenhuma tarefa de projeto for encontrada com o ID fornecido.
     */
    private Task getTaskById(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Id de tarefa não informado");
        }

        return taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.info("Tarefa informada não encontrada. Id fornecido {}", taskId);
                    return new EntityNotFoundException("Tarefa não encontrads");
                });
    }


}
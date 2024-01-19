package com.api.gerenciadorprojetos.UserPerformance.Services;

import com.api.gerenciadorprojetos.Projects.Enums.StatusProjeto;
import com.api.gerenciadorprojetos.Projects.Repositories.ProjetoJpaRepository;
import com.api.gerenciadorprojetos.Tasks.Enums.StatusTarefa;
import com.api.gerenciadorprojetos.Tasks.Repositories.TaskRepository;
import com.api.gerenciadorprojetos.Users.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserPerformanceService {

    private final UserRepository userRepository;
    private final ProjetoJpaRepository projectRepository;
    private final TaskRepository taskRepository;

    private static final Logger log = LoggerFactory.getLogger(UserPerformanceService.class);

    @Autowired
    public UserPerformanceService(UserRepository userRepository, ProjetoJpaRepository projectRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    private Long countProjectsByUserIdAndStatus(Long userId, StatusProjeto status) {
        try {
            return Optional.ofNullable(projectRepository.countByUser_IdAndStatus(userId, status))
                    .orElse(0L);
        } catch (Exception ex) {
            log.error("Erro ao contar projetos por usuário e status.", ex);
            throw new RuntimeException("Erro ao processar a requisição.", ex);
        }
    }

    private Long countTasksByUserIdAndStatus(Long userId, StatusTarefa status) {
        try {
            return Optional.ofNullable(taskRepository.countByResponsaveis_IdAndStatus(userId, status))
                    .orElse(0L);
        } catch (Exception ex) {
            log.error("Erro ao contar tarefas por usuário e status.", ex);
            throw new RuntimeException("Erro ao processar a requisição.", ex);
        }
    }

    public Long getNumberOfLateProjectsByUser(Long userId) {
        return countProjectsByUserIdAndStatus(userId, StatusProjeto.ATRASADO);
    }

    public Long getNumberOfCompletedProjectsByUser(Long userId) {
        return countProjectsByUserIdAndStatus(userId, StatusProjeto.CONCLUIDO);
    }

    public Long getNumberOfOnGoingProjectsByUser(Long userId) {
        return countProjectsByUserIdAndStatus(userId, StatusProjeto.EM_ANDAMENTO);
    }

    public Long getNumberOfToDoProjectsByUser(Long userId) {
        return countProjectsByUserIdAndStatus(userId, StatusProjeto.CRIADO);
    }

    public Long getNumberOfTasksUserWorkedOn(Long userId) {
        return countTasksByUserIdAndStatus(userId, StatusTarefa.EM_ANDAMENTO);
    }

    public Long getNumberOfCompletedTasksUserWorkedOn(Long userId) {
        return countTasksByUserIdAndStatus(userId, StatusTarefa.CONCLUIDA);
    }

    public Long getNumberOfPendingTasksUserWorkedOn(Long userId) {
        return countTasksByUserIdAndStatus(userId, StatusTarefa.PENDENTE);
    }

    public Long getNumberOfDelayedTasksUserWorkedOn(Long userId) {
        return countTasksByUserIdAndStatus(userId, StatusTarefa.ATRASADA);
    }
}

package com.api.gerenciadorprojetos.Utils;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Projects.Repositories.ProjetoJpaRepository;
import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import com.api.gerenciadorprojetos.Tasks.Repositories.TaskRepository;
import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.Users.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Classe utilitária para obtenção de entidades por ID.
 *
 * @author victor.marcelo
 */
@Component
public class EntityServiceUtils {

    private final UserRepository userRepository;

    private final ProjetoJpaRepository projectRepository;

    private final TaskRepository taskRepository;

    private final Logger log = LoggerFactory.getLogger(EntityServiceUtils.class);
    @Autowired
    public EntityServiceUtils(UserRepository userRepository, ProjetoJpaRepository projectRepository, TaskRepository taskRepository){
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Obtém o usuário pelo ID.
     *
     * @param userId O ID do usuário a ser recuperado.
     * @return O objeto User correspondente ao ID fornecido.
     * @throws IllegalArgumentException Se o ID do usuário não for informado.
     * @throws EntityNotFoundException Se nenhum usuário for encontrado com o ID fornecido.
     */
    public User getUserById(Long userId) {
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
    public Project getProjectById(Long projectId) {
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
     * Obtém a tarefa de projeto pelo ID.
     *
     * @param taskId O ID da tarefa de projeto a ser recuperada.
     * @return O objeto Task correspondente ao ID fornecido.
     * @throws IllegalArgumentException Se o ID da tarefa não for informado.
     * @throws EntityNotFoundException Se nenhuma tarefa de projeto for encontrada com o ID fornecido.
     */
    public Task getTaskById(Long taskId) {
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

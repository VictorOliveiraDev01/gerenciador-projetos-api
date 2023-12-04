package com.api.gerenciadorprojetos.Jobs;

import com.api.gerenciadorprojetos.Projects.Services.ProjectService;
import com.api.gerenciadorprojetos.Tasks.Services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
/**
 * Classe que define tarefas agendadas (jobs) para execução automática em intervalos específicos.
 * As tarefas incluem a atualização do status de projetos e tarefas vencidas.
 *
 * @see ProjectService
 * @see TaskService
 */
public class Scheduleds {

    private final ProjectService projectService;
    private final TaskService taskService;

    @Autowired
    public Scheduleds(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    /**
     * Job executado todos os dias à meia-noite para atualizar o status de projetos vencidos.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateLateProjectsStatus() {
        projectService.updateLateProjectsStatus();
    }

    /**
     * Job executado todos os dias à meia-noite para atualizar o status de tarefas vencidas.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateLateTaskStatus() {
        taskService.updateLateTaskStatus();
    }
}


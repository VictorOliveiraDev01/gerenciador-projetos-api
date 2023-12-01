package com.api.gerenciadorprojetos.Jobs;

import com.api.gerenciadorprojetos.Projects.Services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class Scheduleds {
    private final ProjectService projectService;

    @Autowired
    public Scheduleds(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Executa diariamente à meia-noite
    public void updateLateProjectsStatus() {
        projectService.updateLateProjectsStatus();
    }
}

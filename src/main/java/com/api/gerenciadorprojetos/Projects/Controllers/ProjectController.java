package com.api.gerenciadorprojetos.Projects.Controllers;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Projects.Services.ProjectService;
import com.api.gerenciadorprojetos.Utils.Response;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projetos")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<?> findAllProjects() {
        return ResponseEntity.ok(new Response<>(projectService.findAllProjects()));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<?> findToDoProjects() {
        return ResponseEntity.ok(new Response<>(projectService.findToDoProjects()));
    }

    @GetMapping("/andamento")
    public ResponseEntity<?> findOngoingProjects() {
        return ResponseEntity.ok(new Response<>(projectService.findOngoingProjects()));
    }

    @GetMapping("/concluidos")
    public ResponseEntity<?> findCompletedProjects() {
        return ResponseEntity.ok(new Response<>(projectService.findCompletedProjects()));
    }

    @GetMapping("/atrasados")
    public ResponseEntity<?> findLateProjects() {
        return ResponseEntity.ok(new Response<>(projectService.findLateProjects()));
    }

    @GetMapping("/pendentes/{userId}")
    public ResponseEntity<?> findUserToDoProjects(@PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(projectService.findUserToDoProjects(userId)));
    }

    @GetMapping("/andamento/{userId}")
    public ResponseEntity<?> findUserOngoingProjects(@PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(projectService.findUserOngoingProjects(userId)));
    }

    @GetMapping("/concluidos/{userId}")
    public ResponseEntity<?> findUserCompletedProjects(@PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(projectService.findUserCompletedProjects(userId)));
    }

    @GetMapping("/atrasados/{userId}")
    public ResponseEntity<?> findUserLateProjects(@PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(projectService.findUserLateProjects(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(new Response<>(projectService.findProjectById(id)));
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<?> findProjectsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(projectService.findProjectsByUser(userId)));
    }

    @PostMapping
    public ResponseEntity<?> addNewProject(@PathVariable Long userId, @RequestBody Project project) {
        return ResponseEntity.ok(new Response<>(projectService.addNewProject(project, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Project project) {
        return ResponseEntity.ok(new Response<>(projectService.updateProject(id, project)));
    }

    @PostMapping("/adicionar-usuario/{userId}/{projectId}")
    public ResponseEntity<?> addUserToProject(@PathVariable Long userId, @PathVariable Long projectId) throws Exception {
        return ResponseEntity.ok(new Response<>(projectService.addUserToProject(userId, projectId)));
    }

    @PostMapping("/remover-usuario/{userId}/{projectId}")
    public ResponseEntity<?> removeUserFromProject(@PathVariable Long userId, @PathVariable Long projectId) {
        return ResponseEntity.ok(new Response<>(projectService.removeUserFromProject(userId, projectId)));
    }

    @PostMapping("/adicionar-gerente/{userId}/{projectId}")
    public ResponseEntity<?> addProjectManager(@PathVariable Long userId, @PathVariable Long projectId) {
        return ResponseEntity.ok(new Response<>(projectService.addProjectManager(userId, projectId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProjectById(@PathVariable Long id) {
        try {
            projectService.deleteProjectById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Projeto excluído com sucesso");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

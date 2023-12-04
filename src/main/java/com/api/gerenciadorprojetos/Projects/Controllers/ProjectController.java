package com.api.gerenciadorprojetos.Projects.Controllers;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Projects.Enums.StatusProjeto;
import com.api.gerenciadorprojetos.Projects.Services.ProjectService;
import com.api.gerenciadorprojetos.Utils.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável por lidar com as operações relacionadas a Projetos na API.
 *
 * @author victor.marcelo
 *
 * @see Project
 * @see ProjectService
 * @see Response
 */
@RestController
@RequestMapping("/projetos")
@Api(value = "Projeto Controller", tags = "Operações de Gerenciamento de Projetos")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @ApiOperation("Recupera todos os projetos")
    @GetMapping
    public ResponseEntity<?> findAllProjects() {
        return ResponseEntity.ok(new Response<>(projectService.findAllProjects()));
    }

    @ApiOperation("Recupera projetos pelo seu status")
    @GetMapping(value = "/status/{statusProjeto}")
    public ResponseEntity<?> findProjectsByStatus(
            @ApiParam(value = "Status do projeto", required = true) @PathVariable StatusProjeto statusProjeto){
        return ResponseEntity.ok(new Response<>(projectService.findProjectsByStatus(statusProjeto)));
    }

    @ApiOperation("Recupera um projeto pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> findProjectById(
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(new Response<>(projectService.findProjectById(id)));
    }

    @ApiOperation("Recupera projetos de um usuário")
    @GetMapping("/usuario/{userId}")
    public ResponseEntity<?> findProjectsByUser(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(projectService.findProjectsByUser(userId)));
    }

    @ApiOperation("Recupera projetos do usuário solicitado pelo status do projeto")
    @GetMapping("/status/{statusProjeto}/{userId}")
    public ResponseEntity<?> findUserProjectsByStatus(
            @ApiParam(value = "ID do usuário", required = true)
            @PathVariable StatusProjeto statusProjeto,
            @PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(projectService.findUserProjectsByStatus(userId, statusProjeto)));
    }

    @ApiOperation("Adiciona um novo projeto")
    @PostMapping
    public ResponseEntity<?> addNewProject(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "Novo projeto", required = true) @RequestBody Project project) {
        return ResponseEntity.ok(new Response<>(projectService.addNewProject(project, userId)));
    }

    @ApiOperation("Atualiza um projeto pelo ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long id,
            @ApiParam(value = "Projeto atualizado", required = true) @RequestBody Project project) {
        return ResponseEntity.ok(new Response<>(projectService.updateProject(id, project)));
    }

    @ApiOperation("Adiciona um usuário a um projeto")
    @PostMapping("/adicionar-usuario/{userId}/{projectId}")
    public ResponseEntity<?> addUserToProject(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long projectId) throws Exception {
        return ResponseEntity.ok(new Response<>(projectService.addUserToProject(userId, projectId)));
    }

    @ApiOperation("Remove um usuário de um projeto")
    @PostMapping("/remover-usuario/{userId}/{projectId}")
    public ResponseEntity<?> removeUserFromProject(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long projectId) {
        return ResponseEntity.ok(new Response<>(projectService.removeUserFromProject(userId, projectId)));
    }

    @ApiOperation("Adiciona um gerente a um projeto")
    @PostMapping("/adicionar-gerente/{userId}/{projectId}")
    public ResponseEntity<?> addProjectManager(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long projectId) {
        return ResponseEntity.ok(new Response<>(projectService.addProjectManager(userId, projectId)));
    }

    @ApiOperation("Exclui um projeto pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProjectById(
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long id) {
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

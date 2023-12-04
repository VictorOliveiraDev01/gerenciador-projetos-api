package com.api.gerenciadorprojetos.Tasks.Controllers;

import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import com.api.gerenciadorprojetos.Tasks.Enums.StatusTarefa;
import com.api.gerenciadorprojetos.Tasks.Services.TaskService;
import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.Users.Services.UserService;
import com.api.gerenciadorprojetos.Utils.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável por lidar com as operações relacionadas a Tarefas de Projeto na API.
 *
 * @author victor.marcelo
 *
 * @see Task
 * @see TaskService
 * @see Response
 */
@RestController
@RequestMapping("/tarefas")
@Api(value = "Task Controller", tags = "Operações de Gerenciamento de Tarefas")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @ApiOperation("Recupera todas as tarefas")
    @GetMapping
    public ResponseEntity<?> findAllTasks() {
        return ResponseEntity.ok(new Response<>(taskService.findAllTasks()));
    }

    @ApiOperation("Recupera uma tarefa pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> findTaskById(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(new Response<>(taskService.findTaskById(id)));
    }

    @ApiOperation("Adiciona uma nova tarefa")
    @PostMapping("/{userId}/{responsibleId}")
    public ResponseEntity<?> addNewTask(
            @ApiParam(value = "Tarefa a ser adicionada", required = true) @RequestBody Task task,
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do responsável pela tarefa", required = true) @PathVariable Long responsibleId) {
        return ResponseEntity.ok(new Response<>(taskService.addNewTask(task, userId, responsibleId)));
    }

    @ApiOperation("Atualiza uma tarefa pelo ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long id,
            @ApiParam(value = "Tarefa atualizada", required = true) @RequestBody Task task) {
        return ResponseEntity.ok(new Response<>(taskService.updateTask(id, task)));
    }

    @ApiOperation("Completa uma tarefa pelo ID")
    @PostMapping("/completar/{id}")
    public ResponseEntity<?> completeTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(new Response<>(taskService.completeTask(id)));
    }

    @ApiOperation("Adiciona usuários responsáveis a uma tarefa")
    @PostMapping("/adicionar-usuarios/{taskId}")
    public ResponseEntity<?> addUsersToTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long taskId,
            @ApiParam(value = "Lista de IDs de usuários", required = true) @RequestBody List<Long> userIds) {
        return ResponseEntity.ok(new Response<>(taskService.addUsersToTask(taskId, userIds)));
    }

    @ApiOperation("Remove usuários responsáveis de uma tarefa")
    @PostMapping("/remover-usuarios/{taskId}")
    public ResponseEntity<?> removeUsersFromTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long taskId,
            @ApiParam(value = "Lista de IDs de usuários", required = true) @RequestBody List<Long> userIds) {
        return ResponseEntity.ok(new Response<>(taskService.removeUsersFromTask(taskId, userIds)));
    }

    @ApiOperation("Adiciona um responsável à tarefa")
    @PostMapping("/adicionar-responsavel/{taskId}/{userId}")
    public ResponseEntity<?> addResponsibleToTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long taskId,
            @ApiParam(value = "ID do usuário responsável", required = true) @PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(taskService.addResponsibleToTask(taskId, userId)));
    }

    @ApiOperation("Remove um responsável de uma tarefa")
    @PostMapping("/remover-responsavel/{taskId}/{userId}")
    public ResponseEntity<?> removeResponsibleFromTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long taskId,
            @ApiParam(value = "ID do usuário responsável", required = true) @PathVariable Long userId) {
        return ResponseEntity.ok(new Response<>(taskService.removeResponsibleFromTask(taskId, userId)));
    }

    @ApiOperation("Recupera tarefas de projeto associadas a um usuário por status e projeto")
    @GetMapping("/usuario/{userId}/projeto/{projectId}/status/{status}")
    public ResponseEntity<?> findUserTasksByStatusAndProject(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long projectId,
            @ApiParam(value = "Status da tarefa", required = true) @PathVariable StatusTarefa status) {
        return ResponseEntity.ok(new Response<>(taskService.findUserTasksByStatusAndProject(userId, projectId, status)));
    }

    @ApiOperation("Recupera tarefas de projeto associadas a um usuário e projeto")
    @GetMapping("/usuario/{userId}/projeto/{projectId}")
    public ResponseEntity<?> findTasksByUserAndProject(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long projectId) {
        return ResponseEntity.ok(new Response<>(taskService.findTasksByUserAndProject(userId, projectId)));
    }

    @ApiOperation("Exclui uma tarefa pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTaskById(
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long id) {
        try {
            taskService.deleteTaskById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Tarefa excluída com sucesso");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

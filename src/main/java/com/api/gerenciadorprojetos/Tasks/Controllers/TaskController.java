package com.api.gerenciadorprojetos.Tasks.Controllers;

import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import com.api.gerenciadorprojetos.Tasks.Enums.StatusTarefa;
import com.api.gerenciadorprojetos.Tasks.Services.TaskService;
import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.Users.Services.UserService;
import com.api.gerenciadorprojetos.Utils.Response;
import com.api.gerenciadorprojetos.config.CustomRequestInterceptor;
import com.api.gerenciadorprojetos.config.RequestInfo;
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
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long id,
            @RequestHeader("Authorization") String token)
    {
        return ResponseEntity.ok(new Response<>(taskService.findTaskById(id)));
    }

    @ApiOperation("Adiciona uma nova tarefa")
    @PostMapping("/{userId}/{responsibleId}")
    public ResponseEntity<?> addNewTask(
            @ApiParam(value = "Tarefa a ser adicionada", required = true) @RequestBody Task task,
            @ApiParam(value = "ID do usuário que está executando a ação", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do responsável pela tarefa", required = true) @PathVariable Long responsibleId,
            @RequestHeader("Authorization") String token)
    {
        RequestInfo requestInfo = getRequestInfo();
        return ResponseEntity.ok(new Response<>(taskService.addNewTask(task, userId, responsibleId, getRequestInfo())));
    }

    @ApiOperation("Atualiza uma tarefa pelo ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long taskId,
            @ApiParam(value = "ID do Usuário que está executandoa a ação", required = true) @PathVariable Long userId,
            @ApiParam(value = "Tarefa atualizada", required = true) @RequestBody Task task,
            @RequestHeader("Authorization") String token)
    {
        RequestInfo requestInfo = getRequestInfo();
        return ResponseEntity.ok(new Response<>(taskService.updateTask(taskId, userId, task, requestInfo)));
    }

    @ApiOperation("Completa uma tarefa pelo ID")
    @PostMapping("/completar/{id}")
    public ResponseEntity<?> completeTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long id,
            @RequestHeader("Authorization") String token)
    {
        return ResponseEntity.ok(new Response<>(taskService.completeTask(id)));
    }

    @ApiOperation("Adiciona usuários responsáveis a uma tarefa")
    @PostMapping("/adicionar-usuarios/{taskId}")
    public ResponseEntity<?> addUsersToTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long taskId,
            @ApiParam(value = "ID do usuário que está executandoa a ação", required = true) @PathVariable Long userId,
            @ApiParam(value = "Lista de IDs de usuários", required = true) @RequestBody List<Long> userIds,
            @RequestHeader("Authorization") String token)
    {
        RequestInfo requestInfo = getRequestInfo();
        return ResponseEntity.ok(new Response<>(taskService.addUsersToTask(taskId, userId, userIds, requestInfo)));
    }

    @ApiOperation("Remove usuários responsáveis de uma tarefa")
    @PostMapping("/remover-usuarios/{taskId}")
    public ResponseEntity<?> removeUsersFromTask(
            @ApiParam(value = "ID da tarefa", required = true) @PathVariable Long taskId,
            @ApiParam(value = "ID do usuário que está executandoa a ação", required = true) @PathVariable Long userId,
            @ApiParam(value = "Lista de IDs de usuários", required = true) @RequestBody List<Long> userIds,
            @RequestHeader("Authorization") String token)
    {
        RequestInfo requestInfo = getRequestInfo();
        return ResponseEntity.ok(new Response<>(taskService.removeUsersFromTask(taskId, userId, userIds, requestInfo)));
    }


    @ApiOperation("Recupera tarefas de projeto associadas a um usuário por status e projeto")
    @GetMapping("/usuario/{userId}/projeto/{projectId}/status/{status}")
    public ResponseEntity<?> findUserTasksByStatusAndProject(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long projectId,
            @ApiParam(value = "Status da tarefa", required = true) @PathVariable StatusTarefa status,
            @RequestHeader("Authorization") String token)
    {
        return ResponseEntity.ok(new Response<>(taskService.findUserTasksByStatusAndProject(userId, projectId, status)));
    }

    @ApiOperation("Recupera tarefas de projeto associadas a um usuário e projeto")
    @GetMapping("/usuario/{userId}/projeto/{projectId}")
    public ResponseEntity<?> findTasksByUserAndProject(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long projectId,
            @RequestHeader("Authorization") String token)
    {
        return ResponseEntity.ok(new Response<>(taskService.findTasksByUserAndProject(userId, projectId)));
    }

    @ApiOperation("Exclui uma tarefa pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTaskById(
            @ApiParam(value = "ID do usuário que está executando a ação", required = true) @PathVariable Long userId,
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long id,
            @RequestHeader("Authorization") String token)
    {
        try {
            RequestInfo requestInfo = getRequestInfo();
            taskService.deleteTaskById(userId, id, requestInfo);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Tarefa excluída com sucesso");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private RequestInfo getRequestInfo() {
        return CustomRequestInterceptor.getRequestInfo();
    }
}

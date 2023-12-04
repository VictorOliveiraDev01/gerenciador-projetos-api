package com.api.gerenciadorprojetos.Users.Controllers;

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

/**
 * Controllerresponsável por lidar com as operações relacionadas a usuários na API.
 *
 * @author victor.marcelo
 *
 * @see User
 * @see UserService
 * @see Response
 */

@RestController
@RequestMapping(value = "/usuarios")
@Api(value = "Usuário Controller", tags = "Operações de Gerenciamento de Usuários")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation("Recupera todos os usuários")
    @GetMapping
    public ResponseEntity<?> findAllUsers() {
        return ResponseEntity.ok(new Response<>(userService.findAllUsers()));
    }

    @ApiOperation("Recupera um usuário pelo ID")
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> findUserById(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(new Response<>(userService.findUserById(id)));
    }

    @ApiOperation("Recupera usuários associados a um projeto")
    @GetMapping(value = "/projeto/{projectId}")
    public ResponseEntity<?> findUsersByProject(
            @ApiParam(value = "ID do projeto", required = true) @PathVariable Long projectId) {
        return ResponseEntity.ok(new Response<>(userService.findUsersByProject(projectId)));
    }

    @ApiOperation("Adiciona um novo usuário")
    @PostMapping
    public ResponseEntity<?> addNewUser(
            @ApiParam(value = "Novo usuário", required = true) @RequestBody User user) throws Exception {
        return ResponseEntity.ok(new Response<>(userService.addNewUser(user)));
    }

    @ApiOperation("Atualiza um usuário pelo ID")
    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updateUser(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long id,
            @ApiParam(value = "Usuário atualizado", required = true) @RequestBody User user) {
        return ResponseEntity.ok(new Response<>(userService.updateUser(user, id)));
    }

    @ApiOperation("Exclui um usuário pelo ID")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteUserById(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable Long id) {
        try {
            userService.deleteUserById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Usuário excluído com sucesso");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

package com.api.gerenciadorprojetos.Users.Controllers;

import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.Users.Services.UserService;
import com.api.gerenciadorprojetos.Utils.Response;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/usuarios")
public class UserController {

    private final UserService userService;
    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> findAllUsers(){
        return ResponseEntity.ok(new Response<>(userService.findAllUsers()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> findUserById(@PathVariable Long id){
        return ResponseEntity.ok(new Response<>(userService.findUserById(id)));
    }

    @GetMapping(value = "/projeto/{projectId}")
    public ResponseEntity<?> findUsersByProject(@PathVariable Long projectId){
        return ResponseEntity.ok(new Response<>(userService.findUsersByProject(projectId)));
    }

    @PostMapping
    public ResponseEntity<?> addNewUser(@RequestBody User user) throws Exception {
        return ResponseEntity.ok(new Response<>(userService.addNewUser(user)));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user){
        return ResponseEntity.ok(new Response<>(userService.updateUser(user, id)));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id){
        try {
            userService.deleteUserById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Usuário Excluido com sucesso");
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}

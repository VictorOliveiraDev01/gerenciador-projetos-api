package com.api.gerenciadorprojetos.Users.Services;

import com.api.gerenciadorprojetos.Exceptions.UserValidationException;
import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Projects.Repositories.ProjectRepository;
import com.api.gerenciadorprojetos.Users.DTO.UserDTO;
import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.Users.Mappers.UserMapper;
import com.api.gerenciadorprojetos.Users.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe de serviço para gerenciar operações relacionadas a usuários.
 *
 * @author victor.marcelo
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserMapper userMapper;
    private final jakarta.validation.Validator validator;

    /**
     * Construtor para UserService.
     *
     * @param userRepository O repositório para entidades de usuário.
     * @param userMapper O mapper para converter entidades de usuário em DTOs.
     * @param validator O validador para validar entidades de usuário.
     */
    @Autowired
    public UserService(UserRepository userRepository, ProjectRepository projectRepository, UserMapper userMapper, Validator validator){
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.userMapper = userMapper;
        this.validator = validator;
    }

    /**
     * Recupera uma lista de todos os usuários.
     *
     * @return Lista de UserDTOs representando todos os usuários.
     */
    public List<UserDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Recupera um usuário pelo seu ID.
     *
     * @param id O ID do usuário a ser recuperado.
     * @return UserDTO representando o usuário com o ID especificado.
     * @throws IllegalArgumentException Se o ID fornecido for nulo.
     * @throws EntityNotFoundException Se nenhum usuário for encontrado com o ID fornecido.
     */
    public UserDTO findUserById(Long id){
        if(id == null){
            throw new IllegalArgumentException("Id do usuário não fornecido");
        }
        return userMapper.toDto(
                userRepository.findById(id)
                        .orElseThrow( () -> new EntityNotFoundException("Usuário não encontrado. Id Fornecido: " + id)));
    }


    /**
     * Recupera os usuários a partir do projeto.
     *
     * @param projectId O ID do projeto para buscar os usuários.
     * @return Lista de UserDTOs representando todos os usuários a partir do projeto.
     * @throws IllegalArgumentException Se o ID fornecido for nulo.
     * @throws EntityNotFoundException Se nenhum projeto for encontrado com o ID fornecido.
     */
    public List<UserDTO> findUsersByProject(Long projectId){
        if(projectId == null){
            throw new IllegalArgumentException("Id do projeto não fornecido");
        }

        Project projetoEncontrado = projectRepository.findById(projectId)
                .orElseThrow( () -> new EntityNotFoundException("Preojeto não encontrado"));

        return userRepository.findByProjects_Id(projectId)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Adiciona um novo usuário ao sistema.
     *
     * @param user A entidade de usuário a ser adicionada.
     * @return A entidade de usuário adicionada.
     * @throws UserValidationException Se houver erros de validação nos dados fornecidos do usuário.
     * @throws Exception Se um usuário com o mesmo e-mail já existir.
     */
    public User addNewUser(User user) throws Exception{
        validateUser(user);

        Optional<User> checkEmail = userRepository.findUserByEmail(user.getEmail());

        if (checkEmail.isPresent()) {
            throw new Exception("Já existe um usuário cadastrado com este e-mail");
        }

        return userRepository.save(user);
    }

    /**
     * Atualiza um usuário existente no sistema.
     *
     * @param user A entidade usuário com informações atualizadas.
     * @param id O ID do usuário a ser atualizado.
     * @return A entidade usuário atualizada.
     * @throws IllegalArgumentException Se o ID fornecido for nulo.
     * @throws EntityNotFoundException Se nenhum usuário for encontrado com o ID fornecido.
     * @throws UserValidationException Se houver erros de validação nos dados fornecidos do usuário.
     */
    public User updateUser(User user, Long id){
        if(id == null){
            throw new IllegalArgumentException("Id de usuário não fornecido");
        }

        User usuarioEncontrado = userRepository.findById(id)
                .orElseThrow( () -> new EntityNotFoundException("Usuário não encontrado. Id Fornecido: "  + id));

        validateUser(user);

        usuarioEncontrado.setNome(user.getNome());
        usuarioEncontrado.setEmail(user.getEmail());
        usuarioEncontrado.setSenha(user.getSenha());

        return userRepository.save(usuarioEncontrado);
    }

    /**
     * Exclui um usuário pelo seu ID.
     *
     * @param id O ID do usuário a ser excluído.
     * @throws IllegalArgumentException Se o ID fornecido for nulo.
     * @throws EntityNotFoundException Se nenhum usuário for encontrado com o ID fornecido.
     * @throws RuntimeException Se ocorrer um erro durante o processo de exclusão.
     */
    public void deleteUserById(Long id){
        if(id == null){
            throw new IllegalArgumentException("Id de usuário não fornecido");
        }
        try{
            userRepository.findById(id)
                    .ifPresentOrElse(
                            user -> userRepository.delete(user),
                            () -> {
                                throw new EntityNotFoundException("Usuário não encontrado. Id Fornecido: " + id);
                            }
                    );
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao deletar usuário. Causa: " + ex.getMessage(), ex);
        }
    }



    /**
     * Valida o objeto usuário passado no corpo da requisição.
     *
     * @param user Entidade Usuario.
     * @throws UserValidationException Se houver erros de validação nos dados fornecidos do usuário.
     */
    private void validateUser(User user) throws UserValidationException {

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new UserValidationException("Erro de validação ao adicionar um novo usuário", violations);
        }
    }

}




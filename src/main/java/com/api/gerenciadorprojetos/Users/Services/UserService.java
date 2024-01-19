package com.api.gerenciadorprojetos.Users.Services;

import com.api.gerenciadorprojetos.Exceptions.UserValidationException;
import com.api.gerenciadorprojetos.Infra.Security.JwtTokenProvider;
import com.api.gerenciadorprojetos.Projects.Repositories.ProjetoJpaRepository;
import com.api.gerenciadorprojetos.Users.DTO.UserDTO;
import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.Users.Repositories.UserRepository;
import com.api.gerenciadorprojetos.Utils.AuthenticationResponse;
import com.api.gerenciadorprojetos.Utils.EntityServiceUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
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

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ProjetoJpaRepository projectRepository;
    private final ModelMapper modelMapper;
    private final Validator validator;
    private final EntityServiceUtils entityServiceUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationResponse authResponseUser;

    @Autowired
    public UserService(UserRepository userRepository,
                       ProjetoJpaRepository projectRepository,
                       ModelMapper modelMapper,
                       Validator validator,
                       EntityServiceUtils entityServiceUtils,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationResponse authResponseUser)
    {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.modelMapper = modelMapper;
        this.validator = validator;
        this.entityServiceUtils = entityServiceUtils;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authResponseUser = authResponseUser;
    }

    /**
     * Recupera uma lista de todos os usuários.
     *
     * @return Lista de UserDTOs representando todos os usuários.
     */
    public List<UserDTO> findAllUsers() {
        log.info("Recuperando todos os usuários.");
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
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
    public UserDTO findUserById(Long id) {
        if (id == null) {
            log.error("ID do usuário não fornecido.");
            throw new IllegalArgumentException("Id do usuário não fornecido");
        }

        log.info("Recuperando usuário com ID: {}", id);

        return modelMapper.map(entityServiceUtils.getUserById(id), UserDTO.class);
    }

    /**
     * Recupera os usuários a partir do projeto.
     *
     * @param projectId O ID do projeto para buscar os usuários.
     * @return Lista de UserDTOs representando todos os usuários a partir do projeto.
     * @throws IllegalArgumentException Se o ID fornecido for nulo.
     * @throws EntityNotFoundException Se nenhum projeto for encontrado com o ID fornecido.
     */
    public List<UserDTO> findUsersByProject(Long projectId) {
        if (projectId == null) {
            log.error("ID do projeto não fornecido.");
            throw new IllegalArgumentException("Id do projeto não fornecido");
        }

        log.info("Recuperando usuários do projeto com ID: {}", projectId);

        entityServiceUtils.getProjectById(projectId);

        return userRepository.findByProjects_Id(projectId)
                .stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
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
    public User addNewUser(User user) throws Exception {
        log.info("Adicionando novo usuário: {}", user);

        validateUser(user);

        Optional<User> checkEmail = userRepository.findUserByEmail(user.getEmail());

        if (checkEmail.isPresent()) {
            log.info("E-mail fornecido pelo usuário já em uso");
            throw new Exception("Já existe um usuário cadastrado com este e-mail");
        }

        // Hash
        String hashedPassword = passwordEncoder.encode(user.getSenha());
        user.setSenha(hashedPassword);

        return userRepository.save(user);
    }

    /**
     * Atualiza um usuário existente no sistema.
     *
     * @param user A entidade usuário com informações atualizadas.
     * @param userId O ID do usuário a ser atualizado.
     * @return A entidade usuário atualizada.
     * @throws IllegalArgumentException Se o ID fornecido for nulo.
     * @throws EntityNotFoundException Se nenhum usuário for encontrado com o ID fornecido.
     * @throws UserValidationException Se houver erros de validação nos dados fornecidos do usuário.
     */
    public User updateUser(User user, Long userId) {
        if (userId == null) {
            log.error("ID de usuário não fornecido.");
            throw new IllegalArgumentException("Id de usuário não fornecido");
        }

        log.info("Atualizando usuário com ID: {}", userId);

        User userToUpdate = entityServiceUtils.getUserById(userId);

        validateUser(user);

        userToUpdate.setNome(user.getNome());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setSenha(user.getSenha());

        return userRepository.save(userToUpdate);
    }

    /**
     * Exclui um usuário pelo seu ID.
     *
     * @param id O ID do usuário a ser excluído.
     * @throws IllegalArgumentException Se o ID fornecido for nulo.
     * @throws EntityNotFoundException Se nenhum usuário for encontrado com o ID fornecido.
     * @throws RuntimeException Se ocorrer um erro durante o processo de exclusão.
     */
    public void deleteUserById(Long id) {
        if (id == null) {
            log.error("ID de usuário não fornecido.");
            throw new IllegalArgumentException("Id de usuário não fornecido");
        }

        log.info("Excluindo usuário com ID: {}", id);

        try {
            userRepository.findById(id)
                    .ifPresentOrElse(
                            user -> userRepository.delete(user),
                            () -> {
                                log.info("Usuário não encontrado. Id fornecido: {}", id);
                                throw new EntityNotFoundException("Usuário não encontrado. Id fornecido: " + id);
                            }
                    );
        } catch (Exception ex) {
            log.error("Erro ao deletar usuário. Causa: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro ao deletar usuário. Causa: " + ex.getMessage(), ex);
        }
    }


    /**
     * Autentica um usuário com base no email e senha fornecidos, gerando um token JWT em caso de sucesso.
     *
     * @param email O email do usuário para autenticação.
     * @param password A senha do usuário para autenticação.
     * @return Um objeto {@code AuthenticationResponse} contendo o usuário autenticado e o token JWT.
     * @throws AuthenticationException Se a autenticação falhar devido a senha incorreta.
     * @throws IllegalArgumentException Se o email ou senha fornecidos forem nulos.
     */
    public AuthenticationResponse userAuthentication(String email, String password) throws AuthenticationException {
        if (email == null || password == null) {
            log.error("Email ou senha não fornecidos.");
            throw new IllegalArgumentException("Email ou senha não fornecidos");
        }

        log.info("Autenticando usuário com email: {}", email);

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.info("Usuário não encontrado com email: {}", email);
                    return new EntityNotFoundException("Usuário não encontrado com email: " + email);
                });

        if (passwordEncoder.matches(password, user.getSenha())) {
            String token = jwtTokenProvider.generateToken(email);
            return new AuthenticationResponse(user, token);

        } else {
            log.info("Senha incorreta para o usuário com email: {}", email);
            throw new AuthenticationException("Senha incorreta");
        }
    }

    /**
     * Valida o objeto usuário passado no corpo da requisição.
     *
     * @param user Entidade Usuario.
     * @throws UserValidationException Se houver erros de validação nos dados fornecidos do usuário.
     */
    private void validateUser(User user) throws UserValidationException {
        log.info("Validando usuário: {}", user);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            log.info("Usuário inválido. Motivos: {}", violations);
            throw new UserValidationException("Erro de validação ao adicionar um novo usuário", violations);
        }
    }
}

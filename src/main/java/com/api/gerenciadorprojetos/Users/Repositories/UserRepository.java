package com.api.gerenciadorprojetos.Users.Repositories;

import com.api.gerenciadorprojetos.Users.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);

    Optional<User> findByProjects_Id (Long projectId);
}

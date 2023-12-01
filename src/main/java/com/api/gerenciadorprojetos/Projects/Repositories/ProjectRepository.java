package com.api.gerenciadorprojetos.Projects.Repositories;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findProjectsByUser_Id (Long userId);
}

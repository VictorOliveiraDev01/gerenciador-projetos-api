package com.api.gerenciadorprojetos.Projects.Repositories;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Projects.Enums.StatusProjeto;
import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import com.api.gerenciadorprojetos.Tasks.Enums.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findProjectsByUser_Id (Long userId);

    Optional<Project> findProjectsByStatus(StatusProjeto statusProjeto);

    @Query("SELECT p FROM Project p " +
            "JOIN p.membrosProjeto u " +
            "WHERE u.id = :userId AND p.status = :status")
    Optional<Project> findProjectsByUser_IdAndStatus(
            @Param("userId") Long userId,
            @Param("status") StatusProjeto status);

    Long countByUser_IdAndStatus(Long userId, StatusProjeto status);
}

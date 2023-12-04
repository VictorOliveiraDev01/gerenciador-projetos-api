package com.api.gerenciadorprojetos.Tasks.Repositories;

import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import com.api.gerenciadorprojetos.Tasks.Enums.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByUserIdAndProjectId(Long userId, Long projectId);
    @Query("SELECT t FROM Task t " +
            "WHERE t.projeto.id = :projectId " +
            "AND t.responsaveis.id = :userId " +
            "AND t.status = :status")
    Optional<Task> findUserTasksByStatusAndProject(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId,
            @Param("status") StatusTarefa statusTarefa);

}

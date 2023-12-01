package com.api.gerenciadorprojetos.Tasks.Repositories;

import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}

package com.vektorcontext.repository;

import com.vektorcontext.models.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {
    List<TaskCompletion> findByTaskIdAndCompletedOnBetween(Long taskId, LocalDate from, LocalDate to);
    int countByTaskIdAndCompletedOnBetween(Long taskId, LocalDate from, LocalDate to);

    @Modifying
    @Query("DELETE FROM TaskCompletion tc WHERE tc.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
}

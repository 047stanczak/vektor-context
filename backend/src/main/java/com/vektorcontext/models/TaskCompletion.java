package com.vektorcontext.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "task_completion")
public class TaskCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "completed_on", nullable = false)
    private LocalDate completedOn;

    public Long getId() { return id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public LocalDate getCompletedOn() { return completedOn; }
    public void setCompletedOn(LocalDate completedOn) { this.completedOn = completedOn; }
}

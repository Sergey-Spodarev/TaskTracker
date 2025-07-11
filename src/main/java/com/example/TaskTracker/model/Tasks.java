package com.example.TaskTracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    private String title;//Заголовок задачи
    private String description;//Описание задачи
    private LocalDate deadline;//Срок выполнения задачи
    private String priority;//Приоритет: "низкий", "средний", "высокий"
    private LocalDate createdAt;//Дата создания задачи

    @ManyToOne
    private User creator;//Кто создал задачу (ссылка на users.id)
    @ManyToOne
    private User assignee;//Кому назначена задача (ссылка на users.id)
}

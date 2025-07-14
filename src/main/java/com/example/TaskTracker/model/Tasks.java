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
    private LocalDate startDate;//страт выполнения задачи
    private LocalDate endData;//конец выполнения задачи
    private String priority;//Приоритет: "низкий", "средний", "высокий"

    @ManyToOne
    private Users creator;//Кто создал задачу (ссылка на users.id)
    @ManyToOne
    private Users assignee;//Кому назначена задача (ссылка на users.id)
}

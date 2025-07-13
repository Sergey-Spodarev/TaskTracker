package com.example.TaskTracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TaskStatusUpdates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Tasks task;//К какой задаче относится обновление

    @ManyToOne
    private Users user;//Кто обновил статус (пользователь)

    private String status;//Новый статус: "новая", "в работе", "ожидает проверки", "завершена"
    private String comment;//Комментарий при обновлении статуса
    private LocalDateTime updatedAt;//Время обновления статуса
}

package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;


// Сущности БД (классы-таблицы):данные пользователя.
@Data//lombok здесь реализуется get,set,tostring,equals,hascod
@Entity//Помечает класс как сущность БД.
@Table(name = "users") //(опционально) – Указывает имя таблицы, если оно отличается от имени класса.
public class User {
    @Id //Обозначает первичный ключ.
    @Column(name = "user_id", unique = false)//unique - говорит что должно быть уникальное значение
    @GeneratedValue(strategy = GenerationType.IDENTITY)//Указывает стратегию генерации ID (автоинкремент, sequence и т. д.).
    private Long userId;

    @Column(name = "userName", nullable = false, unique = true) //Позволяет задать имя столбца, ограничения и другие параметры.
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;
}

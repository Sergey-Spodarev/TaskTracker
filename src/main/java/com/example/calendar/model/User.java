package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


// Сущности БД (классы-таблицы):данные пользователя.
@Data//lombok здесь реализуется get,set,tostring,equals,hascod
@Entity//Помечает класс как сущность БД.
@Table(name = "users") //(опционально) – Указывает имя таблицы, если оно отличается от имени класса.
public class User {
    @Id //Обозначает первичный ключ.
    @GeneratedValue(strategy = GenerationType.IDENTITY)//Указывает стратегию генерации ID (автоинкремент, sequence и т. д.).
    @Column(name = "user_id")//unique - говорит что должно быть уникальное значение
    private Long userId;

    @Column(name = "userName", nullable = false, unique = true) //Позволяет задать имя столбца, ограничения и другие параметры.
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();
}

package com.example.calendar.repository;

import com.example.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Интерфейсы для работы с БД (наследуются от JpaRepository):запросы к таблице пользователей.
@Repository//Помечает интерфейс как Spring-репозиторий (не обязательно, т. к. Spring автоматически распознаёт его через наследование от JpaRepository).
public interface UserRepository extends JpaRepository<User, Long> {//JpaRepository - автоматически создаст реализацию.
}

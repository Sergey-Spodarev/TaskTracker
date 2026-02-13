package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Department;
import com.example.calendar.model.Role;
import com.example.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Интерфейсы для работы с БД (наследуются от JpaRepository):запросы к таблице пользователей.
@Repository//Помечает интерфейс как Spring-репозиторий (не обязательно, т. к. Spring автоматически распознаёт его через наследование от JpaRepository).
public interface UserRepository extends JpaRepository<User, Long> {//JpaRepository - автоматически создаст реализацию.
    Optional<User> findByEmail(String email);
    Optional<User>  findByUserName(String username);
    List<User> findByRoleAndCompany(Role role, Company company);
    List<User> findByCompany(Company company);
    List<User> findByDepartment(Department department);
    boolean existsByEmail(String email);
}

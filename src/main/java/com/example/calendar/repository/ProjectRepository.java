package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Department;
import com.example.calendar.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Поиск по ID и компании (через department)
    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.department.company = :company")
    Optional<Project> findByIdAndDepartment_Company(@Param("id") Long id,
                                                    @Param("company") Company company);

    // Проверка уникальности ключа в компании
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Project p WHERE p.projectKey = :projectKey AND p.department.company = :company")
    boolean existsByProjectKeyAndDepartment_Company(@Param("projectKey") String projectKey,
                                                    @Param("company") Company company);

    // Поиск проектов по компании
    @Query("SELECT p FROM Project p WHERE p.department.company = :company")
    List<Project> findByDepartment_Company(@Param("company") Company company);

    // Поиск проектов по отделу
    List<Project> findByDepartment(Department department);

    // Поиск проектов по ID компании (альтернатива)
    @Query("SELECT p FROM Project p WHERE p.department.company.id = :companyId")
    List<Project> findByCompanyId(@Param("companyId") Long companyId);
}

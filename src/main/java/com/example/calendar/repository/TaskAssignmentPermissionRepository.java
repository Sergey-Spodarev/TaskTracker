package com.example.calendar.repository;

import com.example.calendar.model.Department;
import com.example.calendar.model.RoleLevel;
import com.example.calendar.model.TaskAssignmentPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskAssignmentPermissionRepository extends JpaRepository<TaskAssignmentPermission, Long> {

    @Query("SELECT task FROM TaskAssignmentPermission task WHERE task.targetDepartment = :targetDepartment")
    List<TaskAssignmentPermission> findByTargetDepartment(@Param("targetDepartment") Department targetDepartment);

    @Query("SELECT task FROM TaskAssignmentPermission task WHERE task.roleLevel = :roleLevel")
    List<TaskAssignmentPermission> findByRoleLevel(@Param("roleLevel") RoleLevel roleLevel);

    List<TaskAssignmentPermission> findByRoleLevelAndAllowedTrue(RoleLevel roleLevel);
}

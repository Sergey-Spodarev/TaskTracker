package com.example.calendar.DTO;

import com.example.calendar.model.SystemRole;
import lombok.Data;

@Data
public class UserWithRoleDTO {
    private Long id;
    private String userName;
    private String email;
    private SystemRole systemRole;

    // Эти поля заполняются из кода
    private Long companyId;
    private Long departmentId;
    private String departmentName;
    private Long roleId;
    private String roleName;
    private String roleCode;
    private Long roleLevelId;
    private Integer roleLevel;
    private String roleLevelName;
}
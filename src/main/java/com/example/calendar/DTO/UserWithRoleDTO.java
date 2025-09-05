package com.example.calendar.DTO;

import lombok.Data;

@Data
public class UserWithRoleDTO {
    private Long id;
    private String userName;
    private String email;
    private Long companyId;
    private Long departmentId;
    private Long roleId;
    private String roleName;
    private String roleCode;
}
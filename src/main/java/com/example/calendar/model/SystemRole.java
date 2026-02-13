package com.example.calendar.model;

public enum SystemRole {
    COMPANY_OWNER,    // Владелец компании (может всё)
    DEPARTMENT_HEAD,  // Руководитель отдела (в своём отделе)
    TEAM_LEAD,        // Лидер команды
    TEAM_MEMBER       // Обычный сотрудник
}

package com.example.calendar.DTO;

import lombok.Data;

@Data
public class CompanyRegistrationDTO {
    private Long id;
    private String name;
    private String INN;

    private String workEmail;
    private String smtpHost;
    private Integer smtpPort;
    private String emailPassword;

    private String adminName;
    private String adminEmail;
    private String adminPassword;
}

package com.example.calendar.DTO;

import lombok.Data;

@Data
public class CompanyRegistrationDTO {
    private Long id;
    private String name;
    private Long INN;

    private String workEmail;
    private String smtpHost;
    private Integer smtpPort;
    private String emailPassword;
}

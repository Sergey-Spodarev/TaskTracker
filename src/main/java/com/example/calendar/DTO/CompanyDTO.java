package com.example.calendar.DTO;

import lombok.Data;

@Data
public class CompanyDTO {
    private Long id;
    private String name;
    private String INN;

    private String workEmail;
    private String smtpHost;
    private Integer smtpPort;
}
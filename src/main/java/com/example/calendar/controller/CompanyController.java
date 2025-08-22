package com.example.calendar.controller;

import com.example.calendar.DTO.CompanyDTO;
import com.example.calendar.DTO.CompanyRegistrationDTO;
import com.example.calendar.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/company")
public class CompanyController {
    private final CompanyService companyService;
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDTO> createCompany(@RequestBody CompanyRegistrationDTO companyDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(companyService.createCompany(companyDTO));
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDTO> updateCompany(@RequestBody CompanyDTO companyDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(companyService.updateCompany(companyDTO));
    }
}

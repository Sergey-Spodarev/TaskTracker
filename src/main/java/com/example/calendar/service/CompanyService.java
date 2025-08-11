package com.example.calendar.service;

import com.example.calendar.DTO.CompanyDTO;
import com.example.calendar.model.Company;
import com.example.calendar.model.User;
import com.example.calendar.repository.CompanyRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {
    private final CompanyRepository companyRepository;
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyDTO createCompany(CompanyDTO companyDTO) {
        if (companyRepository.existsByINN(companyDTO.getId())) {
            throw new UsernameNotFoundException("Company with this INN exists");
        }

        Company company = new Company();
        company.setName(companyDTO.getName());
        company.setINN(companyDTO.getINN());

        return convertToDTO(companyRepository.save(company));
    }

    public CompanyDTO updateCompany(CompanyDTO companyDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!"ADMIN".equals(user.getRole().getCode())) {
            throw new UsernameNotFoundException("Only the administrator can change the company name");
        }
        if (!companyDTO.getINN().equals(user.getCompany().getINN())){
            throw new AccessDeniedException("You cannot change the name of another company.");
        }

        Company company = companyRepository.findByINN(companyDTO.getINN())
                .orElseThrow(() -> new UsernameNotFoundException("Company with this INN does not exist"));
        company.setName(companyDTO.getName());
        return convertToDTO(companyRepository.save(company));
    }

    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setName(company.getName());
        companyDTO.setINN(company.getINN());
        return companyDTO;
    }
}

package com.example.calendar.controller;

import com.example.calendar.DTO.AssignmentRuleDTO;
import com.example.calendar.service.AssignmentRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignmentRuleService")
public class AssignmentRuleController {
    private final AssignmentRuleService assignmentRuleService;
    public AssignmentRuleController(AssignmentRuleService assignmentRuleService) {
        this.assignmentRuleService = assignmentRuleService;
    }

    @PostMapping("/add")
    public ResponseEntity<AssignmentRuleDTO> addAssignmentRule(@RequestBody AssignmentRuleDTO assignmentRuleDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assignmentRuleService.createAssignmentRule(assignmentRuleDTO));
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> delAssignmentRule(@PathVariable Long ruleId) {
        assignmentRuleService.deleteAssignmentRule(ruleId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/getByRole")
    public ResponseEntity<List<AssignmentRuleDTO>> getRulesByRole(@RequestParam String role) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assignmentRuleService.getRulesByRole(role));
    }

    @GetMapping("/getAllRoleCompany")
    public ResponseEntity<List<AssignmentRuleDTO>> getAssignmentRules() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assignmentRuleService.getAllAssignmentRules());
    }
}

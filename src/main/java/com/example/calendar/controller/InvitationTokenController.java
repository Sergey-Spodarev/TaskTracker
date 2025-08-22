package com.example.calendar.controller;

import com.example.calendar.DTO.InvitationTokenDTO;
import com.example.calendar.service.InvitationTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/InvitationToken")
public class InvitationTokenController {
    private final InvitationTokenService invitationTokenService;
    public InvitationTokenController(InvitationTokenService invitationTokenService) {
        this.invitationTokenService = invitationTokenService;
    }

    @PostMapping("/addInvitationToken")
    public ResponseEntity<InvitationTokenDTO> addInvitationToken(String email) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invitationTokenService.createInvitationToken(email));
    }

    @PostMapping("/completeRegistration")
    public ResponseEntity<InvitationTokenDTO> completeRegistration(String token, String userName, String password) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(invitationTokenService.completeRegistration(token, userName, password));
    }
}

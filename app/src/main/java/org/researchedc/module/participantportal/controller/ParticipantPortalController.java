package org.researchedc.module.participantportal.controller;

import org.researchedc.module.participantportal.dto.ParticipantPortalDTO;
import org.researchedc.module.participantportal.service.ParticipantPortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/participant-portal")
public class ParticipantPortalController {

    private final ParticipantPortalService participantPortalService;

    public ParticipantPortalController(ParticipantPortalService participantPortalService) {
        this.participantPortalService = participantPortalService;
    }

    @GetMapping("/public/bootstrap")
    public ResponseEntity<ParticipantPortalDTO> bootstrap(@RequestParam String token) {
        return ResponseEntity.ok(participantPortalService.bootstrap(token));
    }
}

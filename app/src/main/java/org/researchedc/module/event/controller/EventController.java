package org.researchedc.module.event.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.researchedc.module.event.dto.EventCrfDTO;
import org.researchedc.module.event.dto.EventDefinitionDTO;
import org.researchedc.module.event.dto.ScheduleEventRequest;
import org.researchedc.module.event.dto.StudyEventDTO;
import org.researchedc.module.event.dto.UpdateEventRequest;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.event.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final CurrentUserUtils currentUserUtils;

    public EventController(EventService eventService, CurrentUserUtils currentUserUtils) {
        this.eventService = eventService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/definitions")
    public ResponseEntity<List<EventDefinitionDTO>> listDefinitions(
            @RequestParam Integer studyId) {
        return ResponseEntity.ok(eventService.listEventDefinitions(studyId));
    }

    @GetMapping("/by-subject")
    public ResponseEntity<List<StudyEventDTO>> listSubjectEvents(
            @RequestParam Integer studySubjectId) {
        return ResponseEntity.ok(eventService.listSubjectEvents(studySubjectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyEventDTO> getStudyEvent(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.getStudyEvent(id));
    }

    @GetMapping("/{id}/crfs")
    public ResponseEntity<List<EventCrfDTO>> listEventCrfs(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.listEventCrfs(id));
    }

    @PostMapping
    public ResponseEntity<StudyEventDTO> scheduleEvent(
            @Valid @RequestBody ScheduleEventRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        StudyEventDTO dto = eventService.scheduleEvent(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyEventDTO> updateEvent(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEventRequest request) {
        Integer userId = currentUserUtils.getCurrentUserId();
        StudyEventDTO dto = eventService.updateEvent(id, request, userId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeEvent(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        eventService.completeEvent(id, userId);
        return ResponseEntity.ok().build();
    }
}

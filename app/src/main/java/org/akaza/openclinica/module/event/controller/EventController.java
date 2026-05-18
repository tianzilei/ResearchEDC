package org.akaza.openclinica.module.event.controller;

import java.util.List;
import org.akaza.openclinica.module.event.dto.EventCrfDTO;
import org.akaza.openclinica.module.event.dto.EventDefinitionDTO;
import org.akaza.openclinica.module.event.dto.StudyEventDTO;
import org.akaza.openclinica.module.event.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
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
}

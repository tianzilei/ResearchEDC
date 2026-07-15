package org.researchedc.module.sdv.controller;

import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.sdv.dto.CreateSdvReviewRequest;
import org.researchedc.module.sdv.dto.SdvReviewDTO;
import org.researchedc.module.sdv.dto.UpdateSdvReviewRequest;
import org.researchedc.module.sdv.enums.SdvStatus;
import org.researchedc.module.sdv.service.SdvService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sdv")
public class SdvController {

    private final SdvService sdvService;
    private final CurrentUserUtils currentUserUtils;

    public SdvController(SdvService sdvService, CurrentUserUtils currentUserUtils) {
        this.sdvService = sdvService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/reviews")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<SdvReviewDTO>> listReviews(
            @RequestParam Integer studyId,
            @RequestParam(required = false) SdvStatus status) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(sdvService.listReviews(studyId, status, currentUserId));
    }

    @PostMapping("/reviews")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SdvReviewDTO> createReview(@RequestBody CreateSdvReviewRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sdvService.createOrGetReview(request.getEventCrfId(), currentUserId));
    }

    @PostMapping("/reviews/{reviewId}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SdvReviewDTO> updateReview(
            @PathVariable Long reviewId,
            @RequestBody UpdateSdvReviewRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(sdvService.updateReview(reviewId, request, currentUserId));
    }
}

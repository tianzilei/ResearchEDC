package org.researchedc.module.legacy.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.researchedc.module.dataimport.dto.ImportJobDTO;
import org.researchedc.module.dataimport.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Legacy compatibility endpoint for import file upload.
 * Delegates to the canonical {@link ImportService#uploadFile} path.
 * Kept for backward compatibility with any remaining JSP-based import workflows.
 */
@RestController
@RequestMapping("/api/legacy/import")
public class ImportUploadController {

    private static final Logger log = LoggerFactory.getLogger(ImportUploadController.class);

    private final ImportService importService;

    public ImportUploadController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "importType", defaultValue = "CRF_DATA") String importType,
            @RequestParam(value = "studyId", required = false) Integer studyId,
            HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        try {
            Integer requestedBy = resolveRequestedBy(request);
            ImportJobDTO job = importService.uploadFile(file, importType, studyId,
                    file.getOriginalFilename(), requestedBy);

            String sessionId = request.getSession().getId();
            request.getSession().setAttribute("importFilePath", job.getStoredFilePath());
            request.getSession().setAttribute("importFileName", job.getFileName());
            request.getSession().setAttribute("importJobId", job.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("fileName", job.getFileName());
            response.put("fileSize", file.getSize());
            response.put("storedAs", job.getStoredFilePath());
            response.put("sessionId", sessionId);
            response.put("importJobId", job.getId());
            response.put("importJob", job);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to store import file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to store file: " + e.getMessage()));
        }
    }

    private Integer resolveRequestedBy(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return readPositiveIntegerId(session.getAttribute("userBean"));
    }

    private Integer readPositiveIntegerId(Object candidate) {
        if (candidate == null) {
            return null;
        }
        try {
            Object value = candidate.getClass().getMethod("getId").invoke(candidate);
            if (value instanceof Number number && number.intValue() > 0) {
                return number.intValue();
            }
        } catch (ReflectiveOperationException | SecurityException e) {
            log.debug("Legacy import upload session user has no readable positive id: {}",
                    candidate.getClass().getName());
        }
        return null;
    }
}

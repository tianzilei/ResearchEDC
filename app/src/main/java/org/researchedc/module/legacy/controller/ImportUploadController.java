package org.researchedc.module.legacy.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/legacy/import")
public class ImportUploadController {

    private static final Logger log = LoggerFactory.getLogger(ImportUploadController.class);

    private final Path uploadDir;

    public ImportUploadController() {
        String dataDir = System.getProperty("user.home") + "/ResearchEDC/data/imports";
        this.uploadDir = Path.of(dataDir);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.warn("Could not create upload directory: {}", uploadDir);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + (originalName != null ? originalName : "import.dat");

        try {
            Path target = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            log.info("Import file uploaded: {} -> {} ({} bytes)", originalName, target, file.getSize());

            String sessionId = request.getSession().getId();
            request.getSession().setAttribute("importFilePath", target.toString());
            request.getSession().setAttribute("importFileName", originalName);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "fileName", originalName,
                    "fileSize", file.getSize(),
                    "storedAs", storedName,
                    "sessionId", sessionId
            ));
        } catch (IOException e) {
            log.error("Failed to store import file: {}", originalName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to store file: " + e.getMessage()));
        }
    }
}

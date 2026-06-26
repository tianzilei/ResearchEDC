package org.researchedc.module.export.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExportArtifactWriter {

    private static final Logger log = LoggerFactory.getLogger(ExportArtifactWriter.class);

    private final Path exportBaseDir;

    public ExportArtifactWriter(
            @Value("${export.artifact.base-dir:./exports}") String baseDir) {
        this.exportBaseDir = Paths.get(baseDir);
    }

    public record ArtifactResult(String filePath, long fileSize) {
    }

    public ArtifactResult writeOdmXml(Long jobId, String xmlContent) throws IOException {
        Path jobDir = exportBaseDir.resolve("odm").resolve(String.valueOf(jobId));
        Files.createDirectories(jobDir);

        String filename = "export_" + jobId + ".xml";
        Path filePath = jobDir.resolve(filename);

        byte[] bytes = xmlContent.getBytes(StandardCharsets.UTF_8);
        Files.write(filePath, bytes);

        log.info("Export artifact written: {} ({} bytes)", filePath, bytes.length);
        return new ArtifactResult(filePath.toAbsolutePath().toString(), bytes.length);
    }
}

package org.researchedc.module.export.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportArtifactWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writeOdmXml_createsFileWithCorrectContent() throws IOException {
        ExportArtifactWriter writer = new ExportArtifactWriter(tempDir.toString());
        String xml = "<ODM FileType=\"Snapshot\" FileOID=\"test\">data</ODM>";

        ExportArtifactWriter.ArtifactResult result = writer.writeOdmXml(42L, xml);

        assertNotNull(result.filePath());
        assertTrue(result.fileSize() > 0);

        Path writtenFile = Path.of(result.filePath());
        assertTrue(Files.exists(writtenFile), "Export file should exist on disk");
        assertEquals(xml, Files.readString(writtenFile), "File content should match input");
    }

    @Test
    void writeOdmXml_createsCorrectDirectoryStructure() throws IOException {
        ExportArtifactWriter writer = new ExportArtifactWriter(tempDir.toString());

        ExportArtifactWriter.ArtifactResult result = writer.writeOdmXml(99L, "<ODM/>");

        Path writtenFile = Path.of(result.filePath());
        assertTrue(writtenFile.toString().contains("odm"), "Path should contain 'odm' directory");
        assertTrue(writtenFile.toString().contains("99"), "Path should contain job ID");
        assertTrue(writtenFile.getFileName().toString().equals("export_99.xml"),
                "Filename should be export_<jobId>.xml");
    }

    @Test
    void writeOdmXml_fileSizeMatchesContentLength() throws IOException {
        ExportArtifactWriter writer = new ExportArtifactWriter(tempDir.toString());
        String xml = "<ODM>Short content</ODM>";

        ExportArtifactWriter.ArtifactResult result = writer.writeOdmXml(1L, xml);

        assertEquals(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8).length, result.fileSize(),
                "Reported file size should match actual byte length");
    }

    @Test
    void writeOdmXml_utf8Content_preservesEncoding() throws IOException {
        ExportArtifactWriter writer = new ExportArtifactWriter(tempDir.toString());
        String xml = "<ODM><Name>研究数据</Name></ODM>";

        ExportArtifactWriter.ArtifactResult result = writer.writeOdmXml(2L, xml);

        assertEquals(xml, Files.readString(Path.of(result.filePath()), java.nio.charset.StandardCharsets.UTF_8),
                "UTF-8 content should be preserved");
    }

    @Test
    void writeOdmXml_emptyContent_createsZeroByteFile() throws IOException {
        ExportArtifactWriter writer = new ExportArtifactWriter(tempDir.toString());

        ExportArtifactWriter.ArtifactResult result = writer.writeOdmXml(3L, "");

        assertEquals(0L, result.fileSize());
        assertTrue(Files.exists(Path.of(result.filePath())));
    }
}

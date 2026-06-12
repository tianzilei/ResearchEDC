package org.researchedc.module.datacapture.dto;

public record AttachmentDTO(
        String id,
        String fileName,
        long size
) {
}

package org.researchedc.module.audit.dto;

public record DatabaseChangeLogDTO(
        String id,
        String author,
        String fileName,
        String dateExecuted,
        String md5Sum,
        String description,
        String comments,
        String tag,
        String liquibase) {
}

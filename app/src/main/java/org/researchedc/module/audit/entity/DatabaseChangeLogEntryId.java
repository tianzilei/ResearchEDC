package org.researchedc.module.audit.entity;

import java.io.Serializable;

public class DatabaseChangeLogEntryId implements Serializable {

    private String id;
    private String author;
    private String fileName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

package org.researchedc.module.dataimport.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDTO {

    private String status;
    private Integer eventCrfs = 0;
    private Integer items = 0;
    private List<String> warnings = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public static ImportResultDTO committed(int eventCrfs, int items) {
        ImportResultDTO result = new ImportResultDTO();
        result.setStatus("committed");
        result.setEventCrfs(eventCrfs);
        result.setItems(items);
        return result;
    }

    public static ImportResultDTO failed(String message) {
        ImportResultDTO result = new ImportResultDTO();
        result.setStatus("failed");
        result.getErrors().add(message);
        return result;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getEventCrfs() { return eventCrfs; }
    public void setEventCrfs(Integer eventCrfs) { this.eventCrfs = eventCrfs; }

    public Integer getItems() { return items; }
    public void setItems(Integer items) { this.items = items; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings != null ? warnings : new ArrayList<>(); }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors != null ? errors : new ArrayList<>(); }
}

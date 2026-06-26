package org.researchedc.module.dataimport.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportPreviewDTO {

    private String status;
    private Integer eventCrfs = 0;
    private Integer totalItems = 0;
    private Integer editCheckErrors = 0;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public static ImportPreviewDTO valid(int eventCrfs, int totalItems, int editCheckErrors) {
        ImportPreviewDTO preview = new ImportPreviewDTO();
        preview.setStatus("validated");
        preview.setEventCrfs(eventCrfs);
        preview.setTotalItems(totalItems);
        preview.setEditCheckErrors(editCheckErrors);
        if (eventCrfs == 0) {
            preview.getWarnings().add("No event CRFs were found in the import file.");
        }
        if (editCheckErrors > 0) {
            preview.getWarnings().add(editCheckErrors + " edit-check error(s) were found.");
        }
        return preview;
    }

    public static ImportPreviewDTO invalid(List<String> errors) {
        ImportPreviewDTO preview = new ImportPreviewDTO();
        preview.setStatus("invalid");
        preview.setErrors(errors != null ? errors : List.of());
        return preview;
    }

    public static ImportPreviewDTO blocked(String reason) {
        ImportPreviewDTO preview = new ImportPreviewDTO();
        preview.setStatus("blocked");
        preview.getErrors().add(reason);
        return preview;
    }

    public static ImportPreviewDTO failed(String message) {
        ImportPreviewDTO preview = new ImportPreviewDTO();
        preview.setStatus("failed");
        preview.getErrors().add(message);
        return preview;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getEventCrfs() { return eventCrfs; }
    public void setEventCrfs(Integer eventCrfs) { this.eventCrfs = eventCrfs; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public Integer getEditCheckErrors() { return editCheckErrors; }
    public void setEditCheckErrors(Integer editCheckErrors) { this.editCheckErrors = editCheckErrors; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors != null ? errors : new ArrayList<>(); }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings != null ? warnings : new ArrayList<>(); }
}

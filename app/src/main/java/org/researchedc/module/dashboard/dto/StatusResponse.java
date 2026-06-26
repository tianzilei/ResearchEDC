package org.researchedc.module.dashboard.dto;

/**
 * Response payload for GET /api/v1/dashboard/status.
 * Reports the health of core system components.
 */
public class StatusResponse {

    private String database;
    private String backgroundTasks;
    private String lastBackup;

    public StatusResponse() {
    }

    public StatusResponse(String database, String backgroundTasks, String lastBackup) {
        this.database = database;
        this.backgroundTasks = backgroundTasks;
        this.lastBackup = lastBackup;
    }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }

    public String getBackgroundTasks() { return backgroundTasks; }
    public void setBackgroundTasks(String backgroundTasks) { this.backgroundTasks = backgroundTasks; }

    public String getLastBackup() { return lastBackup; }
    public void setLastBackup(String lastBackup) { this.lastBackup = lastBackup; }
}

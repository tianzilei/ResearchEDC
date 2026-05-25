package org.researchedc.module.dashboard.dto;

/**
 * Response payload for GET /api/v1/dashboard/tasks.
 * Returns aggregate counts of pending items that require user attention.
 */
public class TasksResponse {

    private int pendingCrfs;
    private int pendingQueries;
    private int pendingReviews;
    private int pendingAccountModifications;

    public TasksResponse() {
    }

    public TasksResponse(int pendingCrfs, int pendingQueries,
                         int pendingReviews, int pendingAccountModifications) {
        this.pendingCrfs = pendingCrfs;
        this.pendingQueries = pendingQueries;
        this.pendingReviews = pendingReviews;
        this.pendingAccountModifications = pendingAccountModifications;
    }

    public int getPendingCrfs() { return pendingCrfs; }
    public void setPendingCrfs(int pendingCrfs) { this.pendingCrfs = pendingCrfs; }

    public int getPendingQueries() { return pendingQueries; }
    public void setPendingQueries(int pendingQueries) { this.pendingQueries = pendingQueries; }

    public int getPendingReviews() { return pendingReviews; }
    public void setPendingReviews(int pendingReviews) { this.pendingReviews = pendingReviews; }

    public int getPendingAccountModifications() { return pendingAccountModifications; }
    public void setPendingAccountModifications(int v) { this.pendingAccountModifications = v; }
}

package org.researchedc.module.dashboard.dto;

import java.time.LocalDateTime;

/**
 * A single entry in the recent-activity feed returned by
 * GET /api/v1/dashboard/recent.
 */
public class RecentActivityItem {

    private String type;
    private String description;
    private LocalDateTime timestamp;
    private String link;

    public RecentActivityItem() {
    }

    public RecentActivityItem(String type, String description,
                              LocalDateTime timestamp, String link) {
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
        this.link = link;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}

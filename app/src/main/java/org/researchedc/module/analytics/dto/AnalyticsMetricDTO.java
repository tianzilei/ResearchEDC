package org.researchedc.module.analytics.dto;

public class AnalyticsMetricDTO {
    private String key;
    private String label;
    private long value;
    private String unit;

    public AnalyticsMetricDTO() {
    }

    public AnalyticsMetricDTO(String key, String label, long value, String unit) {
        this.key = key;
        this.label = label;
        this.value = value;
        this.unit = unit;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}

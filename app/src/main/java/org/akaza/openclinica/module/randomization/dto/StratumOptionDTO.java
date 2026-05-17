package org.akaza.openclinica.module.randomization.dto;

public class StratumOptionDTO {

    private Long id;
    private String label;
    private String value;
    private Integer orderNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }
}

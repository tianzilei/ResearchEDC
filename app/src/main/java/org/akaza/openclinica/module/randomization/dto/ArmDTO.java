package org.akaza.openclinica.module.randomization.dto;

public class ArmDTO {

    private Long id;
    private String name;
    private String displayName;
    private Integer ratio;
    private Integer orderNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Integer getRatio() { return ratio; }
    public void setRatio(Integer ratio) { this.ratio = ratio; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }
}

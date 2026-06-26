package org.researchedc.module.randomization.dto;

import java.util.List;
import org.researchedc.module.randomization.enums.StratumType;

public class StratumDTO {

    private Long id;
    private String name;
    private StratumType stratumType;
    private Integer orderNumber;
    private List<StratumOptionDTO> options;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StratumType getStratumType() { return stratumType; }
    public void setStratumType(StratumType stratumType) { this.stratumType = stratumType; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }

    public List<StratumOptionDTO> getOptions() { return options; }
    public void setOptions(List<StratumOptionDTO> options) { this.options = options; }
}

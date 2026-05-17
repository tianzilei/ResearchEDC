package org.akaza.openclinica.module.randomization.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "randomization_stratum_option")
public class RandomizationStratumOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stratum_id", nullable = false)
    private RandomizationStratum stratum;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String value;

    @Column(name = "order_number")
    private Integer orderNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RandomizationStratum getStratum() { return stratum; }
    public void setStratum(RandomizationStratum stratum) { this.stratum = stratum; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }
}

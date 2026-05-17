package org.akaza.openclinica.module.randomization.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "randomization_arm")
public class RandomizationArm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private RandomizationScheme scheme;

    @Column(nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private Integer ratio = 1;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RandomizationScheme getScheme() { return scheme; }
    public void setScheme(RandomizationScheme scheme) { this.scheme = scheme; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Integer getRatio() { return ratio; }
    public void setRatio(Integer ratio) { this.ratio = ratio; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }
}

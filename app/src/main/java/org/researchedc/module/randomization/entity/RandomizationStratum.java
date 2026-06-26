package org.researchedc.module.randomization.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.researchedc.module.randomization.enums.StratumType;

@Entity
@Table(name = "randomization_stratum")
public class RandomizationStratum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private RandomizationScheme scheme;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "stratum_type", nullable = false)
    private StratumType stratumType;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @OneToMany(mappedBy = "stratum", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNumber")
    private List<RandomizationStratumOption> options = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RandomizationScheme getScheme() { return scheme; }
    public void setScheme(RandomizationScheme scheme) { this.scheme = scheme; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StratumType getStratumType() { return stratumType; }
    public void setStratumType(StratumType stratumType) { this.stratumType = stratumType; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }

    public List<RandomizationStratumOption> getOptions() { return options; }
    public void setOptions(List<RandomizationStratumOption> options) { this.options = options; }
}

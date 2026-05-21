package org.researchedc.module.filter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleFilter")
@Table(name = "filter")
public class FilterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "filter_seq")
    @SequenceGenerator(name = "filter_seq", sequenceName = "filter_filter_id_seq", allocationSize = 1)
    @Column(name = "filter_id")
    private Integer filterId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "sql_statement", length = 4000)
    private String sqlStatement;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

    public Integer getFilterId() { return filterId; }
    public void setFilterId(Integer v) { this.filterId = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }

    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }

    public String getSqlStatement() { return sqlStatement; }
    public void setSqlStatement(String v) { this.sqlStatement = v; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }

    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
}

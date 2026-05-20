package org.researchedc.module.crf.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleCrf")
@Table(name = "crf")
public class CrfEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crf_seq")
    @SequenceGenerator(name = "crf_seq", sequenceName = "crf_crf_id_seq", allocationSize = 1)
    @Column(name = "crf_id")
    private Integer crfId;

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "oc_oid", length = 40)
    private String ocOid;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "source_study_id")
    private Integer sourceStudyId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "version")
    private Integer version;

    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }

    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }

    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }

    public Integer getSourceStudyId() { return sourceStudyId; }
    public void setSourceStudyId(Integer v) { this.sourceStudyId = v; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }

    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer v) { this.version = v; }
}

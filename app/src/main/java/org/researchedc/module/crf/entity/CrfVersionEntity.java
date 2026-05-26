package org.researchedc.module.crf.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleCrfVersion")
@Table(name = "module_crf_version")
public class CrfVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_crf_version_seq")
    @SequenceGenerator(name = "module_crf_version_seq", sequenceName = "module_crf_version_id_seq", allocationSize = 1)
    @Column(name = "crf_version_id")
    private Integer crfVersionId;

    @Column(name = "crf_id")
    private Integer crfId;

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "revision_notes")
    private String revisionNotes;

    @Column(name = "oc_oid", length = 40)
    private String ocOid;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "xform")
    private String xform;

    @Column(name = "xform_name")
    private String xformName;

    public Integer getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(Integer v) { this.crfVersionId = v; }

    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }

    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }

    public String getRevisionNotes() { return revisionNotes; }
    public void setRevisionNotes(String v) { this.revisionNotes = v; }

    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }

    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }

    public String getXform() { return xform; }
    public void setXform(String v) { this.xform = v; }

    public String getXformName() { return xformName; }
    public void setXformName(String v) { this.xformName = v; }
}

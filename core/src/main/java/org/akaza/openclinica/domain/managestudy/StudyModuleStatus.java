package org.akaza.openclinica.domain.managestudy;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Date;

@Entity(name = "managestudy_study_module_status")
@Table(name = "study_module_status")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "study_module_status_id_seq") })
public class StudyModuleStatus extends AbstractAuditableMutableDomainObject {
    public static final int NOT_STARTED = 1;
    public static final int IN_PROGRESS = 2;
    public static final int COMPLETED = 3; 

    private int studyId;
    private int study;
    private int crf;
    private int eventDefinition;
    private int subjectGroup;
    private int rule;
    private int site;
    private int users;

    private transient int studyStatus;

    @Column(name = "study_id")
    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    @Column(name = "study")
    public int getStudy() {
        return study;
    }

    public void setStudy(int study) {
        this.study = study;
    }

    @Column(name = "crf")
    public int getCrf() {
        return crf;
    }

    public void setCrf(int crf) {
        this.crf = crf;
    }

    @Column(name = "event_definition")
    public int getEventDefinition() {
        return eventDefinition;
    }

    public void setEventDefinition(int eventDefinition) {
        this.eventDefinition = eventDefinition;
    }

    @Column(name = "subject_group")
    public int getSubjectGroup() {
        return subjectGroup;
    }

    public void setSubjectGroup(int subjectGroup) {
        this.subjectGroup = subjectGroup;
    }

    @Column(name = "rule")
    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    @Column(name = "site")
    public int getSite() {
        return site;
    }

    public void setSite(int site) {
        this.site = site;
    }

    @Column(name = "users")
    public int getUsers() {
        return users;
    }

    public void setUsers(int user) {
        this.users = user;
    }

    @Transient
    public int getStudyStatus() {
        return studyStatus;
    }

    public void setStudyStatus(int studyStatus) {
        this.studyStatus = studyStatus;
    }
}

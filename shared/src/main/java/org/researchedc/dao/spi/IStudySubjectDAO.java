package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface IStudySubjectDAO {
    EntityBean findByPK(int ID);
    ArrayList findAllByStudy(StudyBean study);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    Object getEntityFromHashMap(HashMap hm);
    ArrayList findAllByStudyOrderByLabel(StudyBean sb);
    ArrayList findAllActiveByStudyOrderByLabel(StudyBean sb);
    ArrayList findAllWithStudyEvent(StudyBean currentStudy);
    ArrayList findAllBySubjectId(int subjectId);
    EntityBean findAnotherBySameLabel(String label, int studyId, int studySubjectId);
    EntityBean findAnotherBySameLabelInSites(String label, int studyId, int studySubjectId);
    StudySubjectBean findSameByLabelAndStudy(String label, int studyId, int id);
    StudySubjectBean findByOidAndStudy(String oid, int studyId);
    StudySubjectBean findByOid(String oid);
    String findStudySubjectIdsByStudyIds(String studyIds);
    StudySubjectBean findBySubjectIdAndStudy(int subjectId, StudyBean study);
    ArrayList findAllByStudyId(int studyId);
    ArrayList findAllByStudyIdAndLimit(int studyId, boolean isLimited);
    int findTheGreatestLabel();
    StudySubjectBean create(StudySubjectBean sb, boolean withGroup);
    StudySubjectBean createWithGroup(StudySubjectBean sb);
    StudySubjectBean createWithoutGroup(StudySubjectBean sb);
    EntityBean update(EntityBean eb, java.sql.Connection con);
    ArrayList getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId);    Integer getTotalEventCrfCountForCrfMigration(CRFVersionBean sourceCrfVersionBean, CRFVersionBean targetCrfVersionBean, ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist);
    Integer getTotalCountStudySubjectForCrfMigration(CRFVersionBean sourceCrfVersionBean, CRFVersionBean targetCrfVersionBean, ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist);

    Integer getCountofStudySubjectsAtStudyOrSite(StudyBean currentStudy);
    Integer getCountofStudySubjectsAtStudy(StudyBean currentStudy);
    Integer getCountofStudySubjects(StudyBean currentStudy);
    Integer getCountofStudySubjectsBasedOnStatus(StudyBean currentStudy, Status status);}

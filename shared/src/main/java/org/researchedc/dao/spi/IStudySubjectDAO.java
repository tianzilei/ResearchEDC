package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.managestudy.FindSubjectsFilter;
import org.researchedc.dao.managestudy.FindSubjectsSort;
import org.researchedc.dao.managestudy.StudyAuditLogFilter;
import org.researchedc.dao.managestudy.StudyAuditLogSort;
import org.researchedc.domain.datamap.StudySubject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.researchedc.dao.managestudy.ListDiscNotesSubjectFilter;
import org.researchedc.dao.managestudy.ListDiscNotesSubjectSort;
import org.researchedc.dao.managestudy.ListEventsForSubjectFilter;
import org.researchedc.dao.managestudy.ListEventsForSubjectSort;
import org.researchedc.dao.managestudy.ListDiscNotesForCRFFilter;
import org.researchedc.dao.managestudy.ListDiscNotesForCRFSort;
import org.researchedc.dao.StudySubjectSDVFilter;
import org.researchedc.dao.StudySubjectSDVSort;

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
    StudySubjectBean findByLabelAndStudy(String label, StudyBean study);
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
    ArrayList getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId);
    Integer getCountWithFilter(ListDiscNotesSubjectFilter filter, StudyBean study);
    Integer getCountWithFilter(ListDiscNotesForCRFFilter filter, StudyBean study);
    ArrayList getWithFilterAndSort(StudyBean study, ListDiscNotesForCRFFilter filter, ListDiscNotesForCRFSort sort, int rowStart, int rowLength);
    ArrayList getWithFilterAndSort(StudyBean study, ListDiscNotesSubjectFilter filter, ListDiscNotesSubjectSort sort, int rowStart, int rowLength);
    Integer getCountWithFilter(ListEventsForSubjectFilter filter, StudyBean study);
    ArrayList getWithFilterAndSort(StudyBean study, ListEventsForSubjectFilter filter, ListEventsForSubjectSort sort, int rowStart, int rowLength);
    Integer getCountWithFilter(FindSubjectsFilter filter, StudyBean study);
    ArrayList getWithFilterAndSort(StudyBean study, FindSubjectsFilter filter, FindSubjectsSort sort, int rowStart, int rowEnd);
    Integer getCountWithFilter(StudyAuditLogFilter filter, StudyBean study);
    ArrayList getWithFilterAndSort(StudyBean study, StudyAuditLogFilter filter, StudyAuditLogSort sort, int rowStart, int rowEnd);
    Integer getTotalEventCrfCountForCrfMigration(CRFVersionBean sourceCrfVersionBean, CRFVersionBean targetCrfVersionBean, ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist);
    Integer getTotalCountStudySubjectForCrfMigration(CRFVersionBean sourceCrfVersionBean, CRFVersionBean targetCrfVersionBean, ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist);

    default StudySubject findByOcOID(String OCOID) { throw new UnsupportedOperationException(); }
    default StudySubject findById(Integer id) { throw new UnsupportedOperationException(); }
    Integer getCountofStudySubjectsAtStudyOrSite(StudyBean currentStudy);
    Integer getCountofStudySubjectsAtStudy(StudyBean currentStudy);
    Integer getCountofStudySubjects(StudyBean currentStudy);
    Integer getCountofStudySubjectsBasedOnStatus(StudyBean currentStudy, Status status);
    ArrayList findAllByStudySDV(int studyId, int parentStudyId, StudySubjectSDVFilter filter, StudySubjectSDVSort sort, int rowStart, int rowEnd);
    int countAllByStudySDV(int studyId, int parentStudyId, StudySubjectSDVFilter filter);
}

package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public interface EventDefinitionCRFDao {
    EntityBean findByPK(int ID);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    Object getEntityFromHashMap(HashMap hm);
    Collection findAllByDefinition(int definitionId);
    Collection findAllByDefinition(StudyBean study, int definitionId);
    Collection findAllByCRF(int crfId);
    ArrayList findByDefaultVersion(int versionId);
    ArrayList findAllByCrfDefinitionInSiteOnly(int definitionId, int crfId);
    Collection findAllActiveParentsByEventDefinitionId(int definitionId);
    Collection findAllActiveNonHiddenByEventDefinitionIdAndStudy(int definitionId, StudyBean study);
    ArrayList findAllByEventDefinitionId(int eventDefinitionId);
    ArrayList findAllByEventDefinitionIdAndOrdinal(int eventDefinitionId, int ordinal);
    ArrayList findAllActiveByEventDefinitionId(int eventDefinitionId);
    Collection findAllActiveByEventDefinitionId(StudyBean study, int eventDefinitionId);
    Collection findAllByEventDefinitionId(StudyBean study, int eventDefinitionId);
    boolean isRequiredInDefinition(int crfVersionId, StudyEventBean studyEvent);
    EventDefinitionCRFBean findByStudyEventIdAndCRFVersionId(StudyBean study, int studyEventId, int crfVersionId);
    EventDefinitionCRFBean findByStudyEventDefinitionIdAndCRFId(int studyEventDefinitionId, int crfId);
    EventDefinitionCRFBean findByStudyEventDefinitionIdAndCRFIdAndStudyId(int studyEventDefinitionId, int crfId, int studyId);
    EventDefinitionCRFBean findByStudyEventDefinitionIdAndCRFId(StudyBean study, int studyEventDefinitionId, int crfId);
    EventDefinitionCRFBean findForStudyByStudyEventDefinitionIdAndCRFId(int studyEventDefinitionId, int crfId);
    EventDefinitionCRFBean findForStudyByStudyEventIdAndCRFVersionId(int studyEventId, int crfVersionId);
    ArrayList findAllDefIdandStudyId(Integer studyEventDefnId, Integer studyId);
    Set<String> findHiddenCrfIdsBySite(StudyBean study);
    Set<String> findHiddenCrfNamesBySite(StudyBean study);
    Map<Integer, SortedSet<EventDefinitionCRFBean>> buildEventDefinitionCRFListByStudyEventDefinition(Integer studySubjectId, Integer siteId, Integer parentStudyId);
    Map<Integer, SortedSet<EventDefinitionCRFBean>> buildEventDefinitionCRFListByStudyEventDefinitionForStudy(Integer studySubjectId);
    Collection findAllParentsByDefinition(int definitionId);
    Collection findAllByDefinitionAndSiteIdAndParentStudyId(int definitionId, int siteId, int parentStudyId);
    ArrayList<EventDefinitionCRFBean> findAllActiveSitesAndStudiesPerParentStudy(int parentStudyId);
    ArrayList<EventDefinitionCRFBean> findAllSubmissionUriAndStudyId(String submissionUri, int studyId);
    List findAllCrfMigrationDoesNotPerform(CRFVersionBean sourceCrfVersionBean, CRFVersionBean targetCrfVersionBean, ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist);

}

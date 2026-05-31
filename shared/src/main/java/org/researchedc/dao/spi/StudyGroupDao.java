package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.bean.managestudy.StudySubjectBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface StudyGroupDao {
    EntityBean findByPK(int id);
    EntityBean findByStudyId(int studyId);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    ArrayList findAllByStudy(StudyBean study);
    ArrayList findAllByGroupClass(StudyGroupClassBean group);
    ArrayList getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId);
    StudyGroupBean findByNameAndGroupClassID(String name, int studyGroupClassId);
    StudyGroupBean findSubjectStudyGroup(int subjectId, String groupClassName);
    HashMap findByStudySubject(StudySubjectBean studySubject);
    HashMap findSubjectGroupMaps(int studyId);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Object getEntityFromHashMap(HashMap hm);
}

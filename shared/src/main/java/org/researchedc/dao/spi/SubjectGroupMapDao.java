package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.submit.SubjectGroupMapBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface SubjectGroupMapDao {
    EntityBean findByPK(int ID);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    Collection findAllByStudySubject(int studySubjectId);
    SubjectGroupMapBean findAllByStudySubjectAndStudyGroupClass(int studySubjectId, int studyGroupClassId);
    ArrayList findAllByStudyGroupId(int studyGroupId);
    ArrayList findAllByStudyGroupClassId(int studyGroupClassId);
    ArrayList findAllByStudyGroupClassAndGroup(int studyGroupClassId, int studyGroupId);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    void deleteTestGroupMap(int id);
    Object getEntityFromHashMap(HashMap hm);
}

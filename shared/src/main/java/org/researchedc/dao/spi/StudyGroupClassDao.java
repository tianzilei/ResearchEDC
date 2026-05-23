package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface StudyGroupClassDao {
    EntityBean findByPK(int id);
    EntityBean findByStudyId(int studyId);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    ArrayList findAllByStudy(StudyBean study);
    ArrayList findAllActiveByStudy(StudyBean study);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    int getCurrentPK();
    Object getEntityFromHashMap(HashMap hm);
}

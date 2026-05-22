package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.service.StudyParameter;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.domain.datamap.StudyParameterValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface IStudyParameterValueDAO {
    EntityBean findByPK(int ID);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    StudyParameterValueBean findByHandleAndStudy(int studyId, String handle);
    StudyParameter findParameterByHandle(String handle);
    boolean setParameterValue(int studyId, String parameterHandle, String value);
    ArrayList findAllParameters();
    ArrayList findAllParameterValuesByStudy(StudyBean study);
    ArrayList findParamConfigByStudy(StudyBean study);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    default StudyParameterValue findByStudyIdParameter(int studyId, String parameter) { throw new UnsupportedOperationException(); }
    Object getEntityFromHashMap(HashMap hm);
}

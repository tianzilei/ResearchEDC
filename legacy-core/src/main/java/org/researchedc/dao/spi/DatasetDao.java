package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.bean.extract.ExtractBean;
import org.researchedc.bean.managestudy.StudyBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface DatasetDao {
    EntityBean findByPK(int ID);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    EntityBean findByNameAndStudy(String name, StudyBean study);
    ArrayList findAllByStudyId(int studyId);
    ArrayList findAllByStudyIdAdmin(int studyId);
    DatasetBean initialDatasetData(int datasetId);
    Collection findAllOrderByStudyIdAndName();
    Collection findTopFive(StudyBean currentStudy);
    Collection findByOwnerId(int ownerId, int studyId);
    EntityBean updateAll(EntityBean eb);
    EntityBean updateGroupMap(DatasetBean db);
    ExtractBean getDatasetData(ExtractBean eb, int currentstudyid, int parentstudyid);
    String parseSQLDataset(String sql, boolean issed, boolean hasfilterzero);
    Object getEntityFromHashMap(HashMap hm);
}

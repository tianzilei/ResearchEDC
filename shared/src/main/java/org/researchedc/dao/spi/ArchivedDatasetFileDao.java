package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;

public interface ArchivedDatasetFileDao {
    EntityBean findByPK(int ID);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    ArrayList findByDatasetId(int did);
    ArrayList findByDatasetIdByDate(int did);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    void deleteArchiveDataset(org.researchedc.bean.extract.ArchivedDatasetFileBean adfBean);
    Object getEntityFromHashMap(HashMap hm);
}

package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface FilterDao {
    EntityBean findByPK(int ID);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAllAdmin();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    String genSQLStatement(String oldSQLStatement, String connector, ArrayList filterObjs);
    ArrayList genExplanation(ArrayList oldExplanation, String connector, ArrayList filterObjs);
    Object getEntityFromHashMap(HashMap hm);
}

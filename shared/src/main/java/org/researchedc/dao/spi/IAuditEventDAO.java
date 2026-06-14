package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;

import java.util.ArrayList;
import java.util.HashMap;

public interface IAuditEventDAO {
    EntityBean findByPK(int id);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    ArrayList findAllByUserId(int userId);
    Object getEntityFromHashMap(HashMap hm);
}

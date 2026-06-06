package org.researchedc.dao.spi;

import org.researchedc.domain.rule.action.PropertyBean;

import java.util.ArrayList;

public interface RuleActionPropertyDomainDao {

    ArrayList<PropertyBean> findByOid(String itemOid, String groupOid);

    ArrayList<PropertyBean> findByOid(String oid);
}

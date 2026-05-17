package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.bean.oid.ItemGroupOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.hibernate.query.Query;

public class ItemGroupDao extends AbstractDomainDao<ItemGroup> {

    @Override
    Class<ItemGroup> domainClass() {
        return ItemGroup.class;
    }

    public ItemGroup findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("OCOID", OCOID);
        return (ItemGroup) q.uniqueResult();
    }

    public ItemGroup findByNameCrfId(String groupName, CrfBean crf) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.name = :groupName and do.crf = :crf";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("groupName", groupName);
        q.setParameter("crf", crf);
        return (ItemGroup) q.uniqueResult();
    }

    public static final String findAllByCrfVersionIdQuery = "select distinct ig.* from item_group ig, item_group_metadata igm"
            + " where igm.crf_version_id = :crfversionid and ig.item_group_id = igm.item_group_id";

    @SuppressWarnings("unchecked")
    public ArrayList<ItemGroup> findByCrfVersionId(Integer crfVersionId) {
        Query q = getCurrentSession().createNativeQuery(findAllByCrfVersionIdQuery, ItemGroup.class);
        q.setParameter("crfversionid", crfVersionId.intValue());
        return (ArrayList<ItemGroup>) q.list();
    }

    public String getValidOid(ItemGroup itemGroup, String crfName, String itemGroupLabel, ArrayList<String> oidList) {
    OidGenerator oidGenerator = new ItemGroupOidGenerator();
        String oid = getOid(itemGroup, crfName, itemGroupLabel);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(ItemGroup itemGroup, String crfName, String itemGroupLabel) {
        OidGenerator oidGenerator = new ItemGroupOidGenerator();
        String oid;
        try {
            oid = itemGroup.getOcOid() != null ? itemGroup.getOcOid() : oidGenerator.generateOid(crfName, itemGroupLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }
}

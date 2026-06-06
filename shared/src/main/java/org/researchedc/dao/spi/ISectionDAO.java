package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.domain.datamap.Section;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.SectionBean;

public interface ISectionDAO {

    public void setTypesExpected();

    public EntityBean update(EntityBean eb);

    public EntityBean create(EntityBean eb);

    public Object getEntityFromHashMap(HashMap hm);

    public Collection findAll();

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public Collection findByVersionId(int ID);

    public EntityBean findByPK(int ID);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType);

    public ArrayList findAllByCRFVersionId(int crfVersionId);

    public HashMap getNumItemsBySectionId();

    public HashMap getNumItemsBySection(SectionBean sb);

    public HashMap getNumItemsPlusRepeatBySectionId(EventCRFBean ecb);

    public HashMap getNumItemsCompletedBySectionId(EventCRFBean ecb);

    public HashMap getNumItemsCompletedBySection(EventCRFBean ecb);

    public HashMap getNumItemsPendingBySectionId(EventCRFBean ecb);

    public HashMap getNumItemsPendingBySection(EventCRFBean ecb,SectionBean sb);

    public HashMap getNumItemsBlankBySectionId(EventCRFBean ecb);

    public HashMap getNumItemsBlankBySection(EventCRFBean ecb,SectionBean sb);

    public SectionBean findNext(EventCRFBean ecb, SectionBean current);

    public SectionBean findPrevious(EventCRFBean ecb, SectionBean current);

    public void deleteTestSection(String label);

    public boolean hasSCDItem(Integer sectionId);

    public int countSCDItemBySectionId(Integer sectionId);

    public boolean containNormalItem(Integer crfVersionId, Integer sectionId);

    public HashMap getSectionIdForTabId(int crfVersionId, int tabId);

    default Section saveOrUpdate(Section entity) { throw new UnsupportedOperationException(); }
    default Section findByCrfVersionOrdinal(int crfVersionId, int ordinal) { throw new UnsupportedOperationException(); }

}

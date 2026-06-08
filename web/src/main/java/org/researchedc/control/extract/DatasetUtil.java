package org.researchedc.control.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IItemDAO;

@Deprecated
public final class DatasetUtil {

    public static final String EVENTS_FOR_CREATE_DATASET = "eventsForCreateDataset";

    private DatasetUtil() {
    }
    public static ArrayList<String> allSedItemIdsInStudy(
            HashMap events, ICrfDAO crfdao, IItemDAO idao) {
        ArrayList<String> sedItemIds = new ArrayList<>();
        Iterator it = events.keySet().iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) it.next();
            ArrayList crfs = (ArrayList) crfdao.findAllActiveByDefinition(sed);
            for (int i = 0; i < crfs.size(); i++) {
                CRFBean crf = (CRFBean) crfs.get(i);
                ArrayList<ItemBean> items = idao.findAllActiveByCRF(crf);
                for (ItemBean item : items) {
                    sedItemIds.add(sed.getId() + "-" + item.getId());
                }
            }
        }
        return sedItemIds;
    }
}

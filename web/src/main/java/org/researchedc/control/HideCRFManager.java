package org.researchedc.control;

import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.DisplayStudyEventBean;
import org.researchedc.bean.submit.DisplayEventCRFBean;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: Feb 8, 2009
 * Time: 3:13:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class HideCRFManager {

    private HideCRFManager() {
    }

    public void optionallyCheckHideCRFProp(
      DisplayStudyEventBean displayStudyEventBean) {

        EventDefinitionCRFBean tempEventCRFBean = new EventDefinitionCRFBean();

            for(DisplayEventCRFBean deCRFBean : displayStudyEventBean.getAllEventCRFs()){
                 tempEventCRFBean = deCRFBean.getEventDefinitionCRF();
                if(tempEventCRFBean.isHideCrf()) {
                    tempEventCRFBean.setHidden(true);
                }
            }

             for(DisplayEventCRFBean deCRFBean : displayStudyEventBean.getDisplayEventCRFs()){
                 tempEventCRFBean = deCRFBean.getEventDefinitionCRF();
                if(tempEventCRFBean.isHideCrf()) {
                    tempEventCRFBean.setHidden(true);
                }
            }


    }
}

package org.researchedc.service.crfdata;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.DisplayEventDefinitionCRFBean;
import org.researchedc.bean.managestudy.DisplayStudyEventBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.DisplayEventCRFBean;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.sql.DataSource;

/**
 * This is a convenience service class to help implement the requirement that a
 * user logged in at the site level cannot view/use CRFs that are marked as
 * "hidden." User: bruceperry Date: Feb 9, 2009
 */
public class HideCRFManager {
    private DataSource eventDefinitionCrfDataSource;
    private EventDefinitionCRFDAO eventDefinitionCrfDao;
    private Function<DataSource, EventDefinitionCRFDAO> eventDefinitionCrfDaoFactory = EventDefinitionCRFDAO::new;

    public static org.researchedc.service.crfdata.HideCRFManager createHideCRFManager() {
        return new org.researchedc.service.crfdata.HideCRFManager();
    }

    private HideCRFManager() {
    }

    public void removeHiddenEventCRF(DisplayStudyEventBean displayStudyEventBean) {

        List<DisplayEventCRFBean> eventCRFBeans = displayStudyEventBean.getAllEventCRFs();

        displayStudyEventBean.setAllEventCRFs(suppressHiddenEventCRFBeans(eventCRFBeans));

        eventCRFBeans = displayStudyEventBean.getDisplayEventCRFs();

        displayStudyEventBean.setDisplayEventCRFs(suppressHiddenEventCRFBeans(eventCRFBeans));

        List<DisplayEventDefinitionCRFBean> displayEventDefinitionCRFBeans = displayStudyEventBean.getUncompletedCRFs();

        ArrayList<DisplayEventDefinitionCRFBean> newDisplayEventDefinitionCRFBeans = new ArrayList<DisplayEventDefinitionCRFBean>();

        for (DisplayEventDefinitionCRFBean displayEventDefinitionCRFBean : displayEventDefinitionCRFBeans) {

            if (!displayEventDefinitionCRFBean.getEdc().isHideCrf()) {
                newDisplayEventDefinitionCRFBeans.add(displayEventDefinitionCRFBean);
            }

        }
        displayStudyEventBean.setUncompletedCRFs(newDisplayEventDefinitionCRFBeans);

    }

    private ArrayList<DisplayEventCRFBean> suppressHiddenEventCRFBeans(List<DisplayEventCRFBean> eventCRFBeans) {

        // Must make this an ArrayList because of the
        // displayStudyEventBean.setAllEventCRFs()
        // method definition
        ArrayList<DisplayEventCRFBean> newEventCRFBeans = new ArrayList<DisplayEventCRFBean>();

        EventDefinitionCRFBean definitionCRFBean = new EventDefinitionCRFBean();

        for (DisplayEventCRFBean deCRFBean : eventCRFBeans) {

            definitionCRFBean = deCRFBean.getEventDefinitionCRF();
            if (!definitionCRFBean.isHideCrf()) {
                newEventCRFBeans.add(deCRFBean);
            }
        }
        return newEventCRFBeans;
    }

    public ArrayList<DisplayEventCRFBean> removeHiddenEventCRFBeans(List<DisplayEventCRFBean> displayEventCRFBeans) {

        ArrayList<DisplayEventCRFBean> newDisplayEventCRFBeans = new ArrayList<DisplayEventCRFBean>();

        for (DisplayEventCRFBean displayEventCRFBean : displayEventCRFBeans) {
            if (!displayEventCRFBean.getEventDefinitionCRF().isHideCrf()) {
                newDisplayEventCRFBeans.add(displayEventCRFBean);
            }
        }

        return newDisplayEventCRFBeans;

    }

    public ArrayList<DisplayEventDefinitionCRFBean> removeHiddenEventDefinitionCRFBeans(List<DisplayEventDefinitionCRFBean> displayEventDefinitionCRFBeans) {

        ArrayList<DisplayEventDefinitionCRFBean> newDisplayEventDefinitionCRFBeans = new ArrayList<DisplayEventDefinitionCRFBean>();

        EventDefinitionCRFBean eventDefinitionCRFBean = new EventDefinitionCRFBean();

        for (DisplayEventDefinitionCRFBean displayEventDefinitionCRFBean : displayEventDefinitionCRFBeans) {

            eventDefinitionCRFBean = displayEventDefinitionCRFBean.getEdc();
            if (!eventDefinitionCRFBean.isHideCrf()) {
                newDisplayEventDefinitionCRFBeans.add(displayEventDefinitionCRFBean);
            }
        }
        return newDisplayEventDefinitionCRFBeans;
    }

    public void optionallyCheckHideCRFProperty(DisplayStudyEventBean displayStudyEventBean) {

        EventDefinitionCRFBean tempEventCRFBean = new EventDefinitionCRFBean();

        for (DisplayEventCRFBean deCRFBean : displayStudyEventBean.getAllEventCRFs()) {
            tempEventCRFBean = deCRFBean.getEventDefinitionCRF();
            if (tempEventCRFBean.isHideCrf()) {
                tempEventCRFBean.setHidden(true);
            }
        }

        for (DisplayEventCRFBean deCRFBean : displayStudyEventBean.getDisplayEventCRFs()) {
            tempEventCRFBean = deCRFBean.getEventDefinitionCRF();
            if (tempEventCRFBean.isHideCrf()) {
                tempEventCRFBean.setHidden(true);
            }
        }

    }

    public boolean studyEventHasAHideCRFProperty(DisplayStudyEventBean displayStudyEventBean) {

        EventDefinitionCRFBean tempEventCRFBean = new EventDefinitionCRFBean();
        boolean hasAHideCRFProperty = false;

        for (DisplayEventCRFBean deCRFBean : displayStudyEventBean.getAllEventCRFs()) {
            tempEventCRFBean = deCRFBean.getEventDefinitionCRF();
            if (tempEventCRFBean.isHideCrf()) {
                hasAHideCRFProperty = true;
            }
        }

        for (DisplayEventCRFBean deCRFBean : displayStudyEventBean.getDisplayEventCRFs()) {
            tempEventCRFBean = deCRFBean.getEventDefinitionCRF();
            if (tempEventCRFBean.isHideCrf()) {
                hasAHideCRFProperty = true;
            }
        }
        return hasAHideCRFProperty;
    }

    public void hideSpecifiedEventCRFDefBeans(List<EventDefinitionCRFBean> eventDefinitionCRFBeans) {

        for (EventDefinitionCRFBean eventDefinitionCRFBean : eventDefinitionCRFBeans) {
            eventDefinitionCRFBean.setHidden(true);
        }
    }

    /**
     * Given a StudyEventDefinitionBean and a list of CRFBeans as parameters,
     * this method returns of new list of CRFBeans containing only unhidden CRFs
     * (their isHideCRF property is false).
     * 
     * @param studyEventBean
     *            The StudyEventDefinitionBean
     * @param crfBeans
     *            An ArrayList of CRFBeans
     * @param dataSource
     *            A javax.sql.DataSource for the use of the
     *            EventDefinitionCRFDAO.
     * @return An ArrayList of CRFBeans containing only "unhidden" CRFs.
     */
    public ArrayList<CRFBean> removeHiddenCRFBeans(StudyBean study, StudyEventDefinitionBean studyEventBean, ArrayList<CRFBean> crfBeans, DataSource dataSource) {

        ArrayList<CRFBean> newBeans = new ArrayList<CRFBean>();
        if (crfBeans == null || crfBeans.isEmpty()) {
            return newBeans;
        }
        EventDefinitionCRFBean tempBean = new EventDefinitionCRFBean();
        for (CRFBean crfBean : crfBeans) {
            tempBean = getEventDefinitionCrfDao(dataSource).findByStudyEventDefinitionIdAndCRFId(study, studyEventBean.getId(), crfBean.getId());
            if (tempBean != null && !tempBean.isHideCrf()) {
                newBeans.add(crfBean);
            }
        }

        return newBeans;
    }

    private EventDefinitionCRFDAO getEventDefinitionCrfDao(DataSource dataSource) {
        if (eventDefinitionCrfDataSource != dataSource) {
            eventDefinitionCrfDataSource = dataSource;
            eventDefinitionCrfDao = null;
        }
        if (eventDefinitionCrfDao == null) {
            eventDefinitionCrfDao = eventDefinitionCrfDaoFactory.apply(dataSource);
        }
        return eventDefinitionCrfDao;
    }
}

package org.researchedc.view.display;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.submit.DisplaySectionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.view.form.FormBeanUtil;
import org.researchedc.view.form.ViewPersistanceHandler;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class handles the responsibility for generating a List of
 * DisplaySectionBeans for a form, such as for a CRF that will be printed. The
 * class is used by PrintCRFServlet and PrintDataEntryServlet.
 */
public class DisplaySectionBeanHandler {

    @Autowired
    protected EventDefinitionCRFDao eventDefinitionCrfDao;

    @Autowired
    protected EventCRFDao eventCrfDao;

    @Autowired
    protected IStudyEventDAO studyEventDao;

    @Autowired
    protected IStudyDAO studyDao;

    @Autowired
    protected SectionDAO sectionDao;
    private boolean hasStoredData = false;
    private int crfVersionId;
    private int eventCRFId;
    private List<DisplaySectionBean> displaySectionBeans;
    private ServletContext context;
    private DataSource dataSource;

    public DisplaySectionBeanHandler(boolean dataEntry) {
        this.hasStoredData = dataEntry;
    }

    public DisplaySectionBeanHandler(boolean dataEntry, DataSource dataSource, ServletContext context) {
        this(dataEntry);
        if (dataSource != null) {
            this.setDataSource(dataSource);
        }
        if (context != null) {
            this.context = context;
        }
    }

    public int getCrfVersionId() {
        return crfVersionId;
    }

    public void setCrfVersionId(int crfVersionId) {
        this.crfVersionId = crfVersionId;
    }

    public int getEventCRFId() {
        return eventCRFId;
    }

    public void setEventCRFId(int eventCRFId) {
        this.eventCRFId = eventCRFId;
    }

    /**
     * This method creates a List of DisplaySectionBeans, returning them in the
     * order that the sections appear in a CRF. This List is "lazily"
     * initialized the first time it is requested.
     *
     * @return A List of DisplaySectionBeans.
     * @see org.researchedc.control.managestudy.PrintCRFServlet
     * @see org.researchedc.control.managestudy.PrintDataEntryServlet
     */
    public List<DisplaySectionBean> getDisplaySectionBeans() {
        FormBeanUtil formBeanUtil;
        ViewPersistanceHandler persistanceHandler;
        ArrayList<SectionBean> allCrfSections;
        // DAO classes for getting item definitions
        SectionDAO sectionDao;
        ICrfVersionDAO crfVersionDao;

        if (displaySectionBeans == null) {
            displaySectionBeans = new ArrayList<DisplaySectionBean>();
            formBeanUtil = new FormBeanUtil();
            if (hasStoredData)
                persistanceHandler = new ViewPersistanceHandler();

            // We need a CRF version id to populate the form display
            if (this.crfVersionId == 0) {
                return displaySectionBeans;
            }

            sectionDao = this.sectionDao;
            allCrfSections = (ArrayList) sectionDao.findByVersionId(this.crfVersionId);

            // for the purposes of null values, try to obtain a valid
            // eventCrfDefinition id
            EventDefinitionCRFBean eventDefBean = null;
            EventCRFBean eventCRFBean = new EventCRFBean();
            if (eventCRFId > 0) {
                EventCRFDao ecdao = this.eventCrfDao;
                eventCRFBean = (EventCRFBean) ecdao.findByPK(eventCRFId);
                IStudyEventDAO sedao = this.studyEventDao;
                StudyEventBean studyEvent = (StudyEventBean) sedao.findByPK(eventCRFBean.getStudyEventId());

                EventDefinitionCRFDao eventDefinitionCRFDAO = this.eventDefinitionCrfDao;
                IStudyDAO sdao = this.studyDao;
                StudyBean study = sdao.findByStudySubjectId(eventCRFBean.getStudySubjectId());
                eventDefBean = eventDefinitionCRFDAO.findByStudyEventIdAndCRFVersionId(study, studyEvent.getId(), this.crfVersionId);
            }
            eventDefBean = eventDefBean == null ? new EventDefinitionCRFBean() : eventDefBean;
            // Create an array or List of DisplaySectionBeans representing each
            // section
            // for printing
            DisplaySectionBean displaySectionBean;
            for (SectionBean sectionBean : allCrfSections) {
                displaySectionBean = formBeanUtil.createDisplaySectionBWithFormGroupsForPrint(sectionBean.getId(), 
                        this.crfVersionId, dataSource, eventDefBean.getId(), eventCRFBean, context);
                displaySectionBeans.add(displaySectionBean);
            }
        }
        return displaySectionBeans;
    }

    public void setDisplaySectionBeans(List<DisplaySectionBean> displaySectionBeans) {
        this.displaySectionBeans = displaySectionBeans;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}

package org.researchedc.control.submit;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.DataEntryStage;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.DisplayTableOfContentsBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IItemGroupDAO;
import org.researchedc.dao.spi.ISectionDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.service.crfdata.DynamicsMetadataService;
import org.researchedc.view.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;

public final class TableOfContentsHelper {

    public static final String BEAN_DISPLAY = "toc";
    public static final String INPUT_ACTION = "action";
    public static final String INPUT_ID = "ecid";
    public static final String INPUT_EVENT_CRF_BEAN = "eventCRF";
    public static final String INPUT_EVENT_DEFINITION_CRF_ID = "eventDefinitionCRFId";
    public static final String INPUT_CRF_VERSION_ID = "crfVersionId";
    public static final String INPUT_STUDY_EVENT_ID = "studyEventId";
    public static final String INPUT_SUBJECT_ID = "subjectId";
    public static final String INPUT_EVENT_CRF_ID = "eventCRFId";
    public static final String INPUT_INTERVIEWER = "interviewer";
    public static final String INPUT_INTERVIEW_DATE = "interviewDate";
    public static final String ACTION_START_INITIAL_DATA_ENTRY = "ide_s";
    public static final String ACTION_CONTINUE_INITIAL_DATA_ENTRY = "ide_c";
    public static final String ACTION_START_DOUBLE_DATA_ENTRY = "dde_s";
    public static final String ACTION_CONTINUE_DOUBLE_DATA_ENTRY = "dde_c";
    public static final String ACTION_ADMINISTRATIVE_EDITING = "ae";
    public static final String INPUT_EVENT_ID = "eventId";
    public static final String BEAN_STUDY_EVENT = "studyEvent";
    public static final String BEAN_STUDY_SUBJECT = "studySubject";
    public static final String BEAN_UNCOMPLETED_EVENTDEFINITIONCRFS = "uncompletedEventDefinitionCRFs";
    public static final String BEAN_DISPLAY_EVENT_CRFS = "displayEventCRFs";

    private TableOfContentsHelper() {
    }

    public static int getIntById(HashMap h, Integer key) {
        Integer value = (Integer) h.get(key);
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    public static String getLink(EventCRFBean ecb) {
        String answer = Page.TABLE_OF_CONTENTS_SERVLET.getFileName();
        answer += "?action=" + getActionForStage(ecb.getStage());
        answer += "&" + INPUT_ID + "=" + ecb.getId();
        return answer;
    }

    public static String getActionForStage(DataEntryStage stage) {
        if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY)) {
            return ACTION_CONTINUE_INITIAL_DATA_ENTRY;
        } else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)) {
            return ACTION_START_DOUBLE_DATA_ENTRY;
        } else if (stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
            return ACTION_CONTINUE_DOUBLE_DATA_ENTRY;
        } else if (stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)) {
            return ACTION_ADMINISTRATIVE_EDITING;
        }
        return "";
    }

    public static ArrayList getSections(EventCRFBean ecb, DataSource ds, ISectionDAO sdao, IItemGroupDAO igdao) {
        HashMap numItemsBySectionId = sdao.getNumItemsBySectionId();
        HashMap numItemsPlusRepeatBySectionId = sdao.getNumItemsPlusRepeatBySectionId(ecb);
        HashMap numItemsCompletedBySectionId = sdao.getNumItemsCompletedBySectionId(ecb);
        HashMap numItemsPendingBySectionId = sdao.getNumItemsPendingBySectionId(ecb);

        ArrayList sections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());

        for (int i = 0; i < sections.size(); i++) {
            SectionBean sb = (SectionBean) sections.get(i);
            int sectionId = sb.getId();
            Integer key = Integer.valueOf(sectionId);
            int numItems = getIntById(numItemsBySectionId, key);
            List<ItemGroupBean> itemGroups = igdao.findLegitGroupBySectionId(sectionId);
            if (!itemGroups.isEmpty()) {
                int numItemsPlusRepeat = getIntById(numItemsPlusRepeatBySectionId, key);
                if (numItemsPlusRepeat > numItems) {
                    sb.setNumItems(numItemsPlusRepeat);
                } else {
                    sb.setNumItems(numItems);
                }
            } else {
                sb.setNumItems(numItems);
            }
            int numItemsCompleted = getIntById(numItemsCompletedBySectionId, key);
            sb.setNumItemsCompleted(numItemsCompleted);
            sb.setNumItemsNeedingValidation(getIntById(numItemsPendingBySectionId, key));
            sections.set(i, sb);
        }
        return sections;
    }

    public static ArrayList getSections(int crfVersionId, ISectionDAO sdao) {
        HashMap numItemsBySectionId = sdao.getNumItemsBySectionId();
        ArrayList sections = sdao.findAllByCRFVersionId(crfVersionId);
        for (int i = 0; i < sections.size(); i++) {
            SectionBean sb = (SectionBean) sections.get(i);
            int sectionId = sb.getId();
            Integer key = Integer.valueOf(sectionId);
            sb.setNumItems(getIntById(numItemsBySectionId, key));
            sections.set(i, sb);
        }
        return sections;
    }

    public static DisplayTableOfContentsBean getDisplayBean(EventCRFBean ecb, DataSource ds, StudyBean currentStudy, IStudySubjectDAO ssdao,
            IStudyEventDAO sedao, ISectionDAO sdao, IItemGroupDAO igdao, IStudyEventDefinitionDAO seddao, ICrfVersionDAO cvdao, ICrfDAO cdao, IStudyDAO studyDao,
            EventDefinitionCRFDao edcdao) {
        DisplayTableOfContentsBean answer = new DisplayTableOfContentsBean();
        answer.setEventCRF(ecb);

        StudySubjectBean ssb = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());
        answer.setStudySubject(ssb);

        StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
        answer.setStudyEvent(seb);

        ArrayList sections = getSections(ecb, ds, sdao, igdao);
        answer.setSections(sections);

        StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(seb.getStudyEventDefinitionId());
        answer.setStudyEventDefinition(sedb);

        CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
        answer.setCrfVersion(cvb);

        CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());
        answer.setCrf(cb);

        StudyBean studyForStudySubject = studyDao.findByStudySubjectId(ssb.getId());
        org.researchedc.bean.managestudy.EventDefinitionCRFBean edcb = edcdao.findByStudyEventDefinitionIdAndCRFId(studyForStudySubject, sedb.getId(), cb.getId());
        answer.setEventDefinitionCRF(edcb);

        answer.setAction(getActionForStage(ecb.getStage()));
        return answer;
    }

    public static DisplayTableOfContentsBean getDisplayBean(int crfVersionId, ISectionDAO sectionDao, ICrfVersionDAO crfVersionDao, ICrfDAO crfDao) {
        DisplayTableOfContentsBean answer = new DisplayTableOfContentsBean();
        ArrayList sections = getSections(crfVersionId, sectionDao);
        answer.setSections(sections);

        CRFVersionBean cvb = (CRFVersionBean) crfVersionDao.findByPK(crfVersionId);
        answer.setCrfVersion(cvb);

        CRFBean cb = (CRFBean) crfDao.findByPK(cvb.getCrfId());
        answer.setCrf(cb);

        answer.setEventCRF(new EventCRFBean());
        answer.setStudyEventDefinition(new StudyEventDefinitionBean());
        return answer;
    }

    public static DisplayTableOfContentsBean getDisplayBeanWithShownSections(DataSource ds, DisplayTableOfContentsBean displayTableOfContentsBean,
            DynamicsMetadataService dynamicsMetadataService, ISectionDAO sectionDAO, IItemGroupDAO itemGroupDAO) {
        if (displayTableOfContentsBean == null) {
            return displayTableOfContentsBean;
        }
        EventCRFBean ecb = displayTableOfContentsBean.getEventCRF();
        ArrayList<SectionBean> sectionBeans = getSections(ecb, ds, sectionDAO, itemGroupDAO);
        ArrayList<SectionBean> showSections = new ArrayList<>();
        if (sectionBeans != null && sectionBeans.size() > 0) {
            for (SectionBean s : sectionBeans) {
                if (sectionDAO.containNormalItem(s.getCRFVersionId(), s.getId())) {
                    showSections.add(s);
                } else {
                    if (dynamicsMetadataService.hasShowingDynGroupInSection(s.getId(), s.getCRFVersionId(), ecb.getId())) {
                        showSections.add(s);
                    } else {
                        if (dynamicsMetadataService.hasShowingDynItemInSection(s.getId(), s.getCRFVersionId(), ecb.getId())) {
                            showSections.add(s);
                        }
                    }
                }
            }
            displayTableOfContentsBean.setSections(showSections);
        }
        return displayTableOfContentsBean;
    }

    public static LinkedList<Integer> sectionIdsInToc(DisplayTableOfContentsBean toc) {
        LinkedList<Integer> ids = new LinkedList<>();
        if (toc != null) {
            ArrayList<SectionBean> sectionBeans = toc.getSections();
            if (sectionBeans != null && sectionBeans.size() > 0) {
                for (int i = 0; i < sectionBeans.size(); ++i) {
                    SectionBean s = sectionBeans.get(i);
                    ids.add(s.getId());
                }
            }
        }
        return ids;
    }

    public static int sectionIndexInToc(SectionBean sb, DisplayTableOfContentsBean toc, LinkedList<Integer> sectionIdsInToc) {
        ArrayList<SectionBean> sectionBeans = new ArrayList<>();
        int index = -1;
        if (toc != null) {
            sectionBeans = toc.getSections();
        }
        if (sectionBeans != null && sectionBeans.size() > 0) {
            for (int i = 0; i < sectionIdsInToc.size(); ++i) {
                if (sb.getId() == sectionIdsInToc.get(i)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
}

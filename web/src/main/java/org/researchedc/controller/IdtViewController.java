package org.researchedc.controller;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.UserType;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.login.UserDTO;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.dao.hibernate.AuthoritiesDao;
import org.researchedc.dao.hibernate.EventCrfFlagDao;
import org.researchedc.dao.hibernate.EventCrfFlagWorkflowDao;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.hibernate.IdtViewDao;
import org.researchedc.dao.hibernate.ItemDataFlagDao;
import org.researchedc.dao.hibernate.ItemDataFlagWorkflowDao;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.domain.datamap.EventCrfFlag;
import org.researchedc.domain.datamap.EventCrfFlagWorkflow;
import org.researchedc.domain.datamap.EventDefinitionCrfTag;
import org.researchedc.domain.datamap.EventDefinitionCrfItemTag;
import org.researchedc.domain.datamap.IdtView;
import org.researchedc.domain.datamap.ItemDataFlag;
import org.researchedc.domain.datamap.ItemDataFlagWorkflow;
import org.researchedc.domain.user.AuthoritiesBean;
import org.researchedc.domain.user.UserAccount;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.fop.area.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.print.Pageable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

@Controller
@RequestMapping(value = "auth/api/itemdata")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class IdtViewController {

    @Autowired
    protected IStudyDAO studyDao;
    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    IdtViewDao idtViewDao;

    @Autowired
    ItemDataFlagDao itemDataFlagDao;

    @Autowired
    EventCrfFlagWorkflowDao eventCrfFlagWorkflowDao;

    @Autowired
    ItemDataFlagWorkflowDao itemDataFlagWorkflowDao;

    @Autowired
    EventCrfFlagDao eventCrfFlagDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    IStudyDAO sdao;

    @RequestMapping(value = "/sdv/{filternumber}/{studyoid}/paginated", params = { "page", "per_page" }, method = RequestMethod.GET)
    public ResponseEntity<List<IdtView>> getPaginatedIdtViewData(@PathVariable("filternumber") String filterNumber, @PathVariable("studyoid") String studyOid,
            @RequestParam("page") int page, @RequestParam("per_page") int per_page) throws Exception {
        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        List<IdtView> idtDTO = null;
        if (page == 0)
            page = 1;
        if (per_page == 0)
            per_page = 30; // default to 30 records / page

        System.out.println("I'm in getPaginatedIdtViewData");

        StudyBean parentStudy = getParentStudy(studyOid);
        Integer pStudyId = parentStudy.getId();
        Integer studyId = getStudy(studyOid).getId();

        ArrayList<String> studySubjects = new ArrayList<>();
        // studySubjects.add("Sub B 101");
        // studySubjects.add("FIEL01");
        // studySubjects.add("104Waltham");
        // studySubjects.add("SS_SUBB101");

        ArrayList<String> studyEventDefinitions = new ArrayList<>();
        // studyEventDefinitions.add("SE_FOLLOWUPVISIT");

        ArrayList<String> crfs = new ArrayList<>();
        // crfs.add("Groups_Adverse_Events");

        int tagId = 1;
        int filter = Integer.valueOf(filterNumber);

        if (studyId == pStudyId) {
            // parent Study
            if (filter == 1)
                idtDTO = getIdtViewDao().findFilter1(studyId, pStudyId, per_page, page, studySubjects, studyEventDefinitions, crfs, tagId, "OR");

        } else {
            // Site
            if (filter == 1)
                idtDTO = getIdtViewDao().findFilter1(studyId, pStudyId, per_page, page, studySubjects, studyEventDefinitions, crfs, tagId, "AND");
        }
        return new ResponseEntity<List<IdtView>>(idtDTO, HttpStatus.OK);

    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity postSDVedItemDataAndEventCrfWorkflow(@RequestBody ArrayList<HashMap<String, String>> maps) throws Exception {
        int tagId = 1;
        UserAccount userAccount = null;
        HashSet<String> listOfEventCrfPaths = new HashSet();
        for (HashMap map : maps) {
            String eventCrfPath = map.get("ssOid") + "." + map.get("sedOid") + "." + map.get("eventOrdinal") + "." + map.get("crfOid");

            String itemDataPath = eventCrfPath + "." + map.get("groupOid") + "." + map.get("groupOrdinal") + "." + map.get("itemOid");
            String workflowStatus = (String) map.get("itemDataWorkflowStatus");
            listOfEventCrfPaths.add(eventCrfPath);
            saveOrUpdateItemDataFlag(tagId, itemDataPath, workflowStatus);
        }

        for (String eventCrfPath : listOfEventCrfPaths) {
            saveOrUpdateEventCrfFlag(tagId, eventCrfPath, userAccount);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void saveOrUpdateItemDataFlag(int tagId, String itemDataPath, String workflowStatus) {
        ItemDataFlag itemDataFlag = null;
        ItemDataFlagWorkflow itemDataFlagWorkflow = null;
        itemDataFlag = getItemDataFlagDao().findByItemDataPath(tagId, itemDataPath);

        if (itemDataFlag != null) {
            itemDataFlagWorkflow = getItemDataFlagWorkflowDao().findById(itemDataFlag.getItemDataFlagWorkflow().getId());
            itemDataFlagWorkflow.setDateUpdated(new Date());
            itemDataFlagWorkflow.setWorkflowStatus(workflowStatus);
            // itemDataFlagWorkflow.setUpdateId(updateId);

            getItemDataFlagWorkflowDao().saveOrUpdate(itemDataFlagWorkflow);

            itemDataFlag.setDateUpdated(new Date());
            // itemDataFlag.setUpdateId(updateId);; // add user account
            getItemDataFlagDao().saveOrUpdate(itemDataFlag);

        } else {
            itemDataFlagWorkflow = new ItemDataFlagWorkflow();
            itemDataFlagWorkflow.setDateCreated(new Date());
            // itemDataFlagWorkflow.setUserAccount(userAccount);
            itemDataFlagWorkflow.setWorkflowId("abcd");
            itemDataFlagWorkflow.setWorkflowStatus(workflowStatus);
            ItemDataFlagWorkflow idfw = getItemDataFlagWorkflowDao().saveOrUpdate(itemDataFlagWorkflow);

            itemDataFlag = new ItemDataFlag();
            itemDataFlag.setPath(itemDataPath);
            itemDataFlag.setDateCreated(new Date());
            itemDataFlag.setUserAccount(null); // add user account
            itemDataFlag.setTagId(tagId);
            itemDataFlag.setItemDataFlagWorkflow(idfw);
            getItemDataFlagDao().saveOrUpdate(itemDataFlag);

        }

    }

    public void saveOrUpdateEventCrfFlag(int tagId, String eventCrfPath, UserAccount ua) {
        EventCrfFlag eventCrfFlag = null;
        EventCrfFlagWorkflow eventCrfFlagWorkflow = null;
        String workflowStatus = "";
        ArrayList<ItemDataFlag> itemDataFlags = (ArrayList<ItemDataFlag>) getItemDataFlagDao().findAllByEventCrfPath(tagId, eventCrfPath);
        eventCrfFlag = getEventCrfFlagDao().findByEventCrfPath(tagId, eventCrfPath);

        if (itemDataFlags.size() != 0) {

            if (eventCrfFlag != null) {
                eventCrfFlagWorkflow = getEventCrfFlagWorkflowDao().findById(eventCrfFlag.getEventCrfFlagWorkflow().getId());
                eventCrfFlagWorkflow.setDateUpdated(new Date());

                // itemDataFlagWorkflow.setUpdateId(updateId);

                getEventCrfFlagWorkflowDao().saveOrUpdate(eventCrfFlagWorkflow);

                // eventCrfFlag.setUpdateId(ua.getUpdateId());
                eventCrfFlag.setDateUpdated(new Date());
                getEventCrfFlagDao().saveOrUpdate(eventCrfFlag);

            } else {

                eventCrfFlagWorkflow = new EventCrfFlagWorkflow();
                eventCrfFlagWorkflow.setDateCreated(new Date());
                // eventCrfFlagWorkflow.setUserAccount(userAccount);
                eventCrfFlagWorkflow.setWorkflowId("abcd");
                EventCrfFlagWorkflow ecfw = getEventCrfFlagWorkflowDao().saveOrUpdate(eventCrfFlagWorkflow);

                EventCrfFlag eventCrfFg = new EventCrfFlag();
                eventCrfFg.setPath(eventCrfPath);
                eventCrfFg.setTagId(1);
                eventCrfFg.setDateCreated(new Date());
                eventCrfFg.setUserAccount(ua);
                eventCrfFg.setEventCrfFlagWorkflow(ecfw);

                getEventCrfFlagDao().saveOrUpdate(eventCrfFg);
            }

        }

    }

    private StudyBean getStudy(String oid) {
        sdao = this.studyDao;
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private StudyBean getStudy(Integer id) {
        sdao = this.studyDao;
        StudyBean studyBean = (StudyBean) sdao.findByPK(id);
        return studyBean;
    }

    private StudyBean getParentStudy(Integer studyId) {
        StudyBean study = getStudy(studyId);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    public IdtViewDao getIdtViewDao() {
        return idtViewDao;
    }

    public ItemDataFlagDao getItemDataFlagDao() {
        return itemDataFlagDao;
    }

    public EventCrfFlagDao getEventCrfFlagDao() {
        return eventCrfFlagDao;
    }

    public EventCrfFlagWorkflowDao getEventCrfFlagWorkflowDao() {
        return eventCrfFlagWorkflowDao;
    }

    public ItemDataFlagWorkflowDao getItemDataFlagWorkflowDao() {
        return itemDataFlagWorkflowDao;
    }

}

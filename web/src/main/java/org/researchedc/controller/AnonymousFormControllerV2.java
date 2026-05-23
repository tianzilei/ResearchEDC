package org.researchedc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import jakarta.servlet.ServletContext;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.service.managestudy.EventDefinitionCrfTagService;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.researchedc.web.pform.PFormCache;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

@Controller
@RequestMapping(value = "/api/v2/anonymousform")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class AnonymousFormControllerV2 {

    @Autowired
    protected ICrfDAO crfDao;

    @Autowired
    protected StudyParameterValueDAO studyParameterValueDao;

    @Autowired
    protected EventDefinitionCRFDao eventDefinitionCrfDao;

    @Autowired
    protected IStudyEventDefinitionDAO studyEventDefinitionDao;

    @Autowired
    protected IStudyDAO studyDao;

    @Autowired
    protected CRFVersionDAO crfVersionDao;

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    IUserAccountDAO udao;
    IStudyDAO sdao;


    /**
     * @api {post} /pages/api/v2/anonymousform/form Retrieve anonymous form URL
     * @apiName getEnketoForm
     * @apiPermission Module participate - enabled
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid
     * @apiParam {String} submissionUri Submission Url
     * @apiGroup Form
     * @apiDescription Retrieve anonymous form url.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "submissionUri": "abcde"
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "url": "http://localhost:8006/::YYYi?iframe=true&ecid=abb764d026830e98b895ece6d9dcaf3c5e817983cc00a4ebfaabcb6c3700b4d5",
     *                    "offline": "false"
     *                    }
     */

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public ResponseEntity<AnonymousUrlResponse> getEnketoForm(@RequestBody HashMap<String, String> map) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        EventDefinitionCrfTagService tagService = (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");
        String formUrl = null;
        String studyOid = map.get("studyOid");

        if (!mayProceed(studyOid))
            return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        String submissionUri = map.get("submissionUri");
        if (submissionUri != "" && submissionUri != null) {


            StudyBean study = getStudy(studyOid);

            EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
            ArrayList<EventDefinitionCRFBean> edcBeans = edcdao.findAllSubmissionUriAndStudyId(submissionUri, study.getId());
            if (edcBeans.size() != 0) {
                EventDefinitionCRFBean edcBean = edcBeans.get(0);
                ICrfDAO crfdao = this.crfDao;
                CRFVersionDAO cvdao = this.crfVersionDao;
                IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;

                CRFVersionBean crfVersionBean = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
                CRFBean crf = (CRFBean) crfdao.findByPK(crfVersionBean.getCrfId());
                StudyBean sBean = (StudyBean) sdao.findByPK(edcBean.getStudyId());
                StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao.findByPK(edcBean.getStudyEventDefinitionId());

                String tagPath = sedBean.getOid() + "." + crf.getOid();

                boolean isOffline = tagService.getEventDefnCrfOfflineStatus(2,tagPath,true);
                String offline = null;
                if (isOffline) offline = "true";
                else offline = "false";

                formUrl = createAnonymousEnketoUrl(sBean.getOid(), crfVersionBean, edcBean, isOffline);
                AnonymousUrlResponse anonResponse = new AnonymousUrlResponse(formUrl, offline, crf.getName(), crfVersionBean.getDescription());

                return new ResponseEntity<AnonymousUrlResponse>(anonResponse, org.springframework.http.HttpStatus.OK);
            } else {
                return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
            }
        } else {
            return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
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

    private StudyBean getStudy(String oid) {
        sdao = this.studyDao;
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private String createAnonymousEnketoUrl(String studyOID, CRFVersionBean crfVersion, EventDefinitionCRFBean edcBean, boolean isOffline) throws Exception {
        StudyBean parentStudyBean = getParentStudy(studyOID);
        PFormCache cache = PFormCache.getInstance(context);
        String enketoURL = cache.getPFormURL(parentStudyBean.getOid(), crfVersion.getOid(), isOffline);
        String contextHash = cache.putAnonymousFormContext(studyOID, crfVersion.getOid(),edcBean.getStudyEventDefinitionId());
        String url = null;
        if (isOffline) url = enketoURL.split("#",2)[0] + "?" + FORM_CONTEXT + "=" + contextHash + "#" + enketoURL.split("#",2)[1];
        else url = enketoURL + "?" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
        return url;

    }
    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean siteStudy = getStudy(studyOid);
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = this.studyParameterValueDao;
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOid()).toString(); // ACTIVE , PENDING , INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
        System.out.println("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "   siteStatus: " + siteStatus);
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus  + "   siteStatus: " + siteStatus);
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }

        return accessPermission;
    }

    private class AnonymousUrlResponse {
        private String url = null;
        private String offline = null;
        private String name = null;
        private String description = null;

        public AnonymousUrlResponse(String url, String offline, String name, String description) {
            this.url = url;
            this.offline = offline;
            this.name = name;
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getOffline() {
            return offline;
        }

        public void setOffline(String offline) {
            this.offline = offline;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }


    }

}

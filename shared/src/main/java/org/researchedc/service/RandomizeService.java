package org.researchedc.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.dao.hibernate.DynamicsItemFormMetadataDao;
import org.researchedc.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.IItemGroupDAO;
import org.researchedc.dao.spi.ISectionDAO;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.action.StratificationFactorBean;
import org.researchedc.service.pmanage.RandomizationRegistrar;
import org.researchedc.service.pmanage.SeRandomizationDTO;
import org.researchedc.service.rule.expression.ExpressionService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.security.oauth2.common.json.JSONException;
//import org.springframework.security.oauth2.common.json.JSONObject;
import org.json.JSONObject;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("randomizeService")
public class RandomizeService extends RandomizationRegistrar {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String ESCAPED_SEPERATOR = "\\.";
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    DataSource ds;
    private EventCRFDao eventCRFDAO;
    private IItemDataDAO itemDataDAO;
    private IItemDAO itemDAO;
    private IItemGroupDAO itemGroupDAO;
    private ISectionDAO sectionDAO;
    // private CRFVersionDAO crfVersionDAO;
    private IItemFormMetadataDAO itemFormMetadataDAO;
    private IStudyEventDAO studyEventDAO;
    private ExpressionService expressionService;
    private IStudySubjectDAO studySubjectDAO;
    private ISubjectDAO subjectDAO;
    private StudyGroupClassDao studyGroupClassDAO;
    private StudyGroupDao studyGroupDAO;
    private IUserAccountDAO userAccountDAO;
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    public static final int RANDOMIZATION_READ_TIMEOUT = 10000;
    IStudyDAO sdao=null;


    public RandomizeService(DataSource ds) {
        this.ds = ds;
        this.expressionService = new ExpressionService(ds);
    }

    @Autowired
    public RandomizeService(DataSource ds, ExpressionService expressionService, IStudySubjectDAO studySubjectDAO, IStudyDAO studyDAO,
            IUserAccountDAO userAccountDAO, ISubjectDAO subjectDAO, StudyGroupClassDao studyGroupClassDAO, StudyGroupDao studyGroupDAO,
            IItemDataDAO itemDataDAO) {
        this.ds = ds;
        this.expressionService = expressionService;
        this.studySubjectDAO = studySubjectDAO;
        this.sdao = studyDAO;
        this.userAccountDAO = userAccountDAO;
        this.subjectDAO = subjectDAO;
        this.studyGroupClassDAO = studyGroupClassDAO;
        this.studyGroupDAO = studyGroupDAO;
        this.itemDataDAO = itemDataDAO;
    }

    // Rest Call to OCUI to get Randomization

    public String getRandomizationCode(EventCRFBean eventCrfBean, List<StratificationFactorBean> stratificationFactorBeans, RuleSetBean ruleSet) {
        IStudySubjectDAO ssdao = getStudySubjectDAO();
        StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        String identifier = ssBean.getOid(); // study subject oid
        IStudyDAO sdao = getStudyDAO();
        StudyBean sBean = (StudyBean) sdao.findByPK(ssBean.getStudyId());
        String siteIdentifier = sBean.getOid(); // site or study oid
        String name = sBean.getName(); // site or study name
        IUserAccountDAO udao = getUserAccountDAO();
        int userId = 0;
        if (eventCrfBean.getUpdaterId() == 0) {
            userId = eventCrfBean.getOwnerId();
        } else {
            userId = eventCrfBean.getUpdaterId();
        }
        UserAccountBean uBean = (UserAccountBean) udao.findByPK(userId);
        String user = uBean.getName();

        // sBean should be parent study
        // put randomization object in cache
       
        StudyBean study = getParentStudy(sBean.getOid());        
        SeRandomizationDTO randomization = null;

        try {
            randomization = getCachedRandomizationDTOObject(study.getOid(), false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String randomiseUrl = randomization.getUrl();
        String username = randomization.getUsername();
        String password = randomization.getPassword();
        String timezone = "America/New_York";

            // String randomiseUrl = "https://evaluation.sealedenvelope.com/redpill/seti2";
            // String username = "oc";
            // String password = "secret";

            HttpHeaders headers = createHeaders(username, password);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // retrieve json object if Randomization exist ,otherwise return a null object
            JSONObject jsonRandObject = retrieveARandomisation(randomiseUrl, ssBean, headers);
            if (jsonRandObject != null) {
                return (String) jsonRandObject.get("code");
            } else {
                // if Site identifier exists ,then update otherwise create new Site identifier
                addOrUpdateASite(randomiseUrl, sBean, headers, timezone);

                // send for Randomization
                JSONObject jsonRandomisedObject = randomiseSubject(randomiseUrl, ssBean, sBean, headers, user, stratificationFactorBeans, eventCrfBean, ruleSet);
                if (jsonRandomisedObject != null)
                    return (String) jsonRandomisedObject.get("code");
                else
                    return "";
            }
          }




    private String getExpressionValue(String expr, EventCRFBean eventCrfBean, RuleSetBean ruleSet) {
        String expression = getExpressionService().constructFullExpressionIfPartialProvided(expr, ruleSet.getTarget().getValue());
        ItemDataBean itemData = null;
        if (expression != null && !expression.isEmpty()) {
            ItemBean itemBean = getExpressionService().getItemBeanFromExpression(expression);
            String itemGroupBOrdinal = getExpressionService().getGroupOrdninalCurated(expression);
            itemData = getItemDataDAO().findByItemIdAndEventCRFIdAndOrdinal(itemBean.getId(), eventCrfBean.getId(),
                    itemGroupBOrdinal == "" ? 1 : Integer.valueOf(itemGroupBOrdinal));
        }
        return itemData.getValue();
    }

    private String getStudySubjectAttrValue(String expr, EventCRFBean eventCrfBean, RuleSetBean ruleSet) {
        String value = "";
        IStudySubjectDAO ssdao = getStudySubjectDAO();
        ISubjectDAO subdao = getSubjectDAO();
        StudyGroupClassDao sgcdao = getStudyGroupClassDAO();
        StudyGroupDao sgdao = getStudyGroupDAO();
        StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        SubjectBean subjectBean = (SubjectBean) subdao.findByPK(ssBean.getSubjectId());

        String prefix = "STUDYGROUPCLASSLIST";
        String param = expr.split("\\.", -1)[1].trim();

        if (param.equalsIgnoreCase("BIRTHDATE")) {
            value = subjectBean.getDateOfBirth().toString();
        } else if (param.equalsIgnoreCase("SEX")) {
            if (String.valueOf(subjectBean.getGender()).equals("m"))
                value = "Male";
            else
                value = "Female";

            // value =String.valueOf(ssBean.getGender());

        } else if (param.startsWith(prefix)) {
            String gcName = param.substring(21, param.indexOf("\"]"));

            StudyGroupBean sgBean = sgdao.findSubjectStudyGroup(ssBean.getId(), gcName);
            if (sgBean != null)
                value = sgBean.getName();
        }
        return value;
    }

    private JSONObject retrieveARandomisation(String randomiseUrl, StudySubjectBean studySubject, HttpHeaders headers) {
        // method : GET
        randomiseUrl = randomiseUrl + "/api/randomisation?identifier=" + studySubject.getOid(); // concatenate
                                                                                                // Study_Siubject_oid
        RestTemplate rest = new RestTemplate(requestFactory);
        ResponseEntity<String> response = null;
        String body = null;
        JSONObject jsonObject = null;
        HttpEntity<String> request = new HttpEntity<String>(headers);

        try {
            response = rest.exchange(randomiseUrl, HttpMethod.GET, request, String.class);
            body = response.getBody();
            jsonObject = new JSONObject(body);
            // if (!jsonObject.get("error").equals("0"))
            // jsonObject= null;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return jsonObject;

    }

    private void addOrUpdateASite(String randomiseUrl, StudyBean studyBean, HttpHeaders headers, String timezone) {
        // mehtod : POST
        randomiseUrl = randomiseUrl + "/api/sites";
        RestTemplate rest = new RestTemplate(requestFactory);
        ResponseEntity<String> response = null;
        MultiValueMap<String, String> siteMap = new LinkedMultiValueMap<String, String>();
        siteMap.add("siteIdentifier", studyBean.getOid());
        siteMap.add("name", studyBean.getName());
        siteMap.add("timezone", timezone);
        HttpEntity<MultiValueMap<String, String>> siteRequest = new HttpEntity<MultiValueMap<String, String>>(siteMap, headers);

        try {
            response = rest.exchange(randomiseUrl, HttpMethod.POST, siteRequest, String.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private JSONObject randomiseSubject(String randomiseUrl, StudySubjectBean studySubject, StudyBean studyBean, HttpHeaders headers, String user,
            List<StratificationFactorBean> stratificationFactorBeans, EventCRFBean eventCrfBean, RuleSetBean ruleSet) {
        // method : POST
        int i = 1;
        String exp = "";

        randomiseUrl = randomiseUrl + "/api/randomise";
        RestTemplate rest = new RestTemplate(requestFactory);
        ResponseEntity<String> response = null;
        MultiValueMap<String, String> subjectMap = new LinkedMultiValueMap<String, String>();
        subjectMap.add("identifier", String.valueOf(studySubject.getOid()));
        subjectMap.add("siteIdentifier", studyBean.getOid());
        subjectMap.add("user", user);
        for (StratificationFactorBean stratificationFactorBean : stratificationFactorBeans) {
            exp = stratificationFactorBean.getStratificationFactor().getValue();
            if (exp.startsWith("SS.")) {
                subjectMap.add("question" + i, getStudySubjectAttrValue(exp, eventCrfBean, ruleSet));

            } else {

                String output = getExpressionValue(exp, eventCrfBean, ruleSet);
                subjectMap.add("question" + i, output);
            }
            i++;
        }

        String body = null;
        JSONObject jsonObject = null;
        HttpEntity<MultiValueMap<String, String>> subjectRequest = new HttpEntity<MultiValueMap<String, String>>(subjectMap, headers);

        try {
            response = rest.exchange(randomiseUrl, HttpMethod.POST, subjectRequest, String.class);
            body = response.getBody();
            jsonObject = new JSONObject(body);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage());
            System.out.println(ExceptionUtils.getStackTrace(e));
            logger.error(ExceptionUtils.getStackTrace(e));
        }

        return jsonObject;
    }

    HttpHeaders createHeaders(final String username, final String password) {
        return new HttpHeaders() {
            {
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
                String authHeader = "Basic " + new String(encodedAuth);
                set("Authorization", authHeader);
            }
        };
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public IItemDataDAO getItemDataDAO() {
        return itemDataDAO;
    }

    public void setItemDataDAO(IItemDataDAO itemDataDAO) {
        this.itemDataDAO = itemDataDAO;
    }
    private StudyBean getStudy(String oid) {
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
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

    public IStudySubjectDAO getStudySubjectDAO() {
        return studySubjectDAO;
    }

    public IStudyDAO getStudyDAO() {
        return sdao;
    }

    public IUserAccountDAO getUserAccountDAO() {
        return userAccountDAO;
    }

    public ISubjectDAO getSubjectDAO() {
        return subjectDAO;
    }

    public StudyGroupClassDao getStudyGroupClassDAO() {
        return studyGroupClassDAO;
    }

    public StudyGroupDao getStudyGroupDAO() {
        return studyGroupDAO;
    }

}

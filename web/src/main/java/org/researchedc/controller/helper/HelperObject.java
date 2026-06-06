package org.researchedc.controller.helper;

import java.util.List;
import java.util.ResourceBundle;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.core.OpenClinicaMailSender;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IStudySubjectDAO;

public class HelperObject {

    List<EventCRFBean> eventCrfListToMigrate;
    CRFVersionBean sourceCrfVersionBean;
    CRFVersionBean targetCrfVersionBean;
    ReportLog reportLog;
    StudyBean stBean;
    CRFBean cBean;
    HttpServletRequest request;
    DataSource dataSource;
    UserAccountBean userAccountBean;
    ResourceBundle resterms;
    String urlBase;
    OpenClinicaMailSender openClinicaMailSender;
    EventCRFDao eventCrfDao;
    IStudyEventDAO studyEventDao;
    IStudySubjectDAO studySubjectDao;
    ICrfVersionDAO crfVersionDao;
    SessionFactory sessionFactory;
    Session session;

    public HelperObject() {
        // TODO Auto-generated constructor stub
    }

    public CRFVersionBean getSourceCrfVersionBean() {
        return sourceCrfVersionBean;
    }

    public void setSourceCrfVersionBean(CRFVersionBean sourceCrfVersionBean) {
        this.sourceCrfVersionBean = sourceCrfVersionBean;
    }

    public CRFVersionBean getTargetCrfVersionBean() {
        return targetCrfVersionBean;
    }

    public void setTargetCrfVersionBean(CRFVersionBean targetCrfVersionBean) {
        this.targetCrfVersionBean = targetCrfVersionBean;
    }

    public ReportLog getReportLog() {
        return reportLog;
    }

    public void setReportLog(ReportLog reportLog) {
        this.reportLog = reportLog;
    }

    public StudyBean getStBean() {
        return stBean;
    }

    public void setStBean(StudyBean stBean) {
        this.stBean = stBean;
    }

    public CRFBean getcBean() {
        return cBean;
    }

    public void setcBean(CRFBean cBean) {
        this.cBean = cBean;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserAccountBean getUserAccountBean() {
        return userAccountBean;
    }

    public void setUserAccountBean(UserAccountBean userAccountBean) {
        this.userAccountBean = userAccountBean;
    }

    public ResourceBundle getResterms() {
        return resterms;
    }

    public void setResterms(ResourceBundle resterms) {
        this.resterms = resterms;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    public OpenClinicaMailSender getOpenClinicaMailSender() {
        return openClinicaMailSender;
    }

    public void setOpenClinicaMailSender(OpenClinicaMailSender openClinicaMailSender) {
        this.openClinicaMailSender = openClinicaMailSender;
    }

    public List<EventCRFBean> getEventCrfListToMigrate() {
        return eventCrfListToMigrate;
    }

    public void setEventCrfListToMigrate(List<EventCRFBean> eventCrfListToMigrate) {
        this.eventCrfListToMigrate = eventCrfListToMigrate;
    }

    public EventCRFDao getEventCrfDao() {
        return eventCrfDao;
    }

    public void setEventCrfDao(EventCRFDao eventCrfDao) {
        this.eventCrfDao = eventCrfDao;
    }

    public IStudyEventDAO getStudyEventDao() {
        return studyEventDao;
    }

    public void setStudyEventDao(IStudyEventDAO studyEventDao) {
        this.studyEventDao = studyEventDao;
    }

    public IStudySubjectDAO getStudySubjectDao() {
        return studySubjectDao;
    }

    public void setStudySubjectDao(IStudySubjectDAO studySubjectDao) {
        this.studySubjectDao = studySubjectDao;
    }

    public ICrfVersionDAO getCrfVersionDao() {
        return crfVersionDao;
    }

    public void setCrfVersionDao(ICrfVersionDAO crfVersionDao) {
        this.crfVersionDao = crfVersionDao;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }


}

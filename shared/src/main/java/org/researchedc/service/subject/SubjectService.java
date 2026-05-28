package org.researchedc.service.subject;

import org.researchedc.bean.core.Status;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.managestudy.SubjectTransferBean;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.core.SessionManager;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

@Service("legacySubjectService")
public class SubjectService implements SubjectServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    ISubjectDAO subjectDao;
    IStudyParameterValueDAO studyParameterValueDAO;
    IStudySubjectDAO studySubjectDao;
    IUserAccountDAO userAccountDao;
    IStudyDAO studyDao;
    DataSource dataSource;

    public SubjectService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SubjectService(SessionManager sessionManager) {
        this.dataSource = sessionManager.getDataSource();
    }

    @Autowired
    public SubjectService(
            DataSource dataSource,
            ISubjectDAO subjectDao,
            IStudyParameterValueDAO studyParameterValueDAO,
            IStudyDAO studyDao,
            IStudySubjectDAO studySubjectDao,
            IUserAccountDAO userAccountDao) {
        this.dataSource = dataSource;
        this.subjectDao = subjectDao;
        this.studyParameterValueDAO = studyParameterValueDAO;
        this.studyDao = studyDao;
        this.studySubjectDao = studySubjectDao;
        this.userAccountDao = userAccountDao;
    }

    public List<StudySubjectBean> getStudySubject(StudyBean study) {
        return getStudySubjectDao().findAllByStudy(study);

    }

    /*
     * (non-Javadoc)
     * @see org.researchedc.service.subject.SubjectServiceInterface#createSubject(org.researchedc.bean.submit.SubjectBean,
     * org.researchedc.bean.managestudy.StudyBean)
     */
    public String createSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        if (subjectBean.getUniqueIdentifier() != null && subjectBean.getUniqueIdentifier().trim().length()> 0 && 
        		getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier()).getId() != 0) {
        	//we need to keep the label to transfer it to the StudySubjectBean later
        	String label = subjectBean.getLabel();
        	subjectBean = getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier());
        	subjectBean.setLabel(label);
        } else {
            subjectBean.setStatus(Status.AVAILABLE);
            subjectBean = getSubjectDao().create(subjectBean);
        }
        
        StudySubjectBean studySubject = createStudySubject(subjectBean, studyBean, enrollmentDate, secondaryId);
        getStudySubjectDao().createWithoutGroup(studySubject);
        return studySubject.getLabel();
    }

    private StudySubjectBean createStudySubject(SubjectBean subject, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setSecondaryLabel(secondaryId);
        studySubject.setOwner(subject.getOwner());
        studySubject.setEnrollmentDate(enrollmentDate);
        studySubject.setSubjectId(subject.getId());
        studySubject.setStudyId(studyBean.getId());
        studySubject.setStatus(Status.AVAILABLE);
        
        int handleStudyId = studyBean.getParentStudyId() > 0 ? studyBean.getParentStudyId() : studyBean.getId();
        StudyParameterValueBean subjectIdGenerationParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
        String idSetting = subjectIdGenerationParameter.getValue();
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
        	// Warning: Here we have a race condition. 
        	// At least, a uniqueness constraint should be set on the database! Better provide an atomic method which stores a new label in the database and returns it.  
            int nextLabel = getStudySubjectDao().findTheGreatestLabel() + 1;
            studySubject.setLabel(Integer.toString(nextLabel));
        } else {
        	studySubject.setLabel(subject.getLabel());
        	subject.setLabel(null);
        }
        
        return studySubject;

    }

    public void validateSubjectTransfer(SubjectTransferBean subjectTransferBean) {
        // TODO: Validate here
    }

    /**
     * Getting the first user account from the database. This would be replaced by an authenticated user who is doing the SOAP requests .
     * 
     * @return UserAccountBean
     */
    private UserAccountBean getUserAccount() {

        UserAccountBean user = new UserAccountBean();
        user.setId(1);
        return user;
    }

    /**
     * @return the subjectDao
     */
    public ISubjectDAO getSubjectDao() {
        return subjectDao;
    }
    
    public IStudyParameterValueDAO getStudyParameterValueDAO() {
        return studyParameterValueDAO;
    }

    /**
     * @return the subjectDao
     */
    public IStudyDAO getStudyDao() {
        return studyDao;
    }

    /**
     * @return the subjectDao
     */
    public IStudySubjectDAO getStudySubjectDao() {
        return studySubjectDao;
    }

    /**
     * @return the UserAccountDao
     */
    public IUserAccountDAO getUserAccountDao() {
        return userAccountDao;
    }

    /**
     * @return the datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param datasource
     *            the datasource to set
     */
    public void setDatasource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}

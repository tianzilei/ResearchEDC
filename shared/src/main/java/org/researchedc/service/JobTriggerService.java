package org.researchedc.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.sql.DataSource;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.hibernate.RuleSetDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.expression.ExpressionBean;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.patterns.ocobserver.StudyEventChangeDetails;
import org.researchedc.service.rule.RuleSetService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobTriggerService {
    @Autowired
    RuleSetDao ruleSetDao;
    @Autowired
    DataSource ds;
    @Autowired
    RuleSetService ruleSetService;
    @Autowired
    IUserAccountDAO userAccountDao;
    @Autowired
    IStudyDAO studyDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final SimpleDateFormat currentDateFormat = new SimpleDateFormat("HH:mm:ss");

    // @Scheduled(cron = "0 0/2 * * * ?") // trigger every 2 minutes
    // @Scheduled(cron = "0 0/1 * * * ?") // trigger every minute
    @Scheduled(cron = "0 0 0/1 * * ?") // trigger every hour
    public void hourlyJobTrigger() throws NumberFormatException, ParseException {
        try {
            logger.info("Beginning scheduled rule run.  The time is now " + currentDateFormat.format(new Date()));
            triggerJob();
            logger.info("Completed scheduled rule run.");
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    public void triggerJob(){
        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        ArrayList<RuleSetBean> ruleSets = ruleSetDao.findAllRunOnSchedules(true);
        for (RuleSetBean ruleSet : ruleSets) {
            if (ruleSet.getStatus().AVAILABLE != null && ruleSet.isRunSchedule()) {
                if(ruleSet.getItemId()!=null){ 
                     // item Specific Rule
                    ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<>();
                    StudyBean currentStudy = (StudyBean) getStudyDao().findByPK(ruleSet.getStudyId());
                    ResourceBundleProvider.updateLocale(Locale.getDefault());
                    UserAccountBean ub = (UserAccountBean) getUserAccountDao().findByPK(1);
                    ruleSetBeans.add(ruleSet);
                    ruleSetService.runRulesInBulk(ruleSetBeans, false, currentStudy, ub, true);
                }else{
                    // Event Specific Rule        
                    StudyEventChangeDetails studyEventChangeDetails = new StudyEventChangeDetails(true, true);
                    ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<>();
                    ExpressionBean eBean = new ExpressionBean();
                    eBean.setValue(ruleSet.getTarget().getValue()+".A.B");
                    
                    ruleSet.setTarget(eBean);
                    ruleSetBeans.add(ruleSet);
        
                    ruleSetService.runRulesInBeanProperty(ruleSetBeans ,1, studyEventChangeDetails);

                }
            }
        }
    }
    public IUserAccountDAO getUserAccountDao() {
        return userAccountDao;
    }

    public IStudyDAO getStudyDao() {
        return studyDao;
    }

}

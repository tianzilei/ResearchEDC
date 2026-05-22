package org.researchedc.web.job;

import org.researchedc.bean.admin.TriggerBean;
import org.researchedc.bean.core.DataEntryStage;
import org.researchedc.bean.core.DiscrepancyNoteType;
import org.researchedc.bean.core.ResolutionStatus;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.rule.XmlSchemaValidationHelper;
import org.researchedc.bean.submit.DisplayItemBean;
import org.researchedc.bean.submit.DisplayItemBeanWrapper;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.bean.submit.crfdata.SubjectDataBean;
import org.researchedc.bean.submit.crfdata.SummaryStatsBean;
import org.researchedc.core.OpenClinicaMailSender;
import org.researchedc.dao.admin.AuditEventDAO;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.exception.OpenClinicaException;
import org.researchedc.exception.OpenClinicaSystemException;
import org.researchedc.i18n.util.ResourceBundleProvider;

import org.researchedc.web.crfdata.ImportCRFDataService;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.sql.DataSource;

/**
 * Import Spring Job, a job running asynchronously on the Tomcat server using Spring and Quartz.
 * 
 * @author thickerson, 04/2009
 * 
 */
public class ImportSpringJob extends QuartzJobBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    public static final String DIR_PATH = "scheduled_data_import";

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    
    }
}

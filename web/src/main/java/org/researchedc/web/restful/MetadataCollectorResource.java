package org.researchedc.web.restful;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.sql.DataSource;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.bean.extract.odm.ClinicalDataReportBean;
import org.researchedc.bean.extract.odm.FullReportBean;
import org.researchedc.bean.extract.odm.MetaDataReportBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.odmbeans.ODMBean;
import org.researchedc.bean.odmbeans.OdmClinicalDataBean;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.spi.IRuleSetDAO;
import org.researchedc.dao.spi.IRuleSetRuleDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.service.StudyConfigService;
import org.researchedc.domain.datamap.Study;
import org.researchedc.logic.odmExport.AdminDataCollector;
import org.researchedc.logic.odmExport.MetaDataCollector;
import org.researchedc.dao.spi.IRuleDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * R
 * @author jnyayapathi
 *
 */
public class MetadataCollectorResource {

    @Autowired
    protected IStudyParameterValueDAO studyParameterValueDao;
    @Autowired
    protected StudyConfigService studyConfigService;

    private static final int INDENT_LEVEL = 2;
	private DataSource dataSource;

	private IStudyDAO studyDao;

private IRuleSetRuleDAO ruleSetRuleDao;

private CoreResources coreResources;
//Testing purposes TODO:remove me
private IStudyDAO studyDaoHib;

	public IStudyDAO getStudyDaoHib() {
	return studyDaoHib;
}



public void setStudyDaoHib(IStudyDAO studyDaoHib) {
	this.studyDaoHib = studyDaoHib;
}



	public CoreResources getCoreResources() {
	return coreResources;
}



public void setCoreResources(CoreResources coreResources) {
	this.coreResources = coreResources;
}



	public IRuleSetRuleDAO getRuleSetRuleDao() {
	return ruleSetRuleDao;
}



public void setRuleSetRuleDao(IRuleSetRuleDAO ruleSetRuleDao) {
	this.ruleSetRuleDao = ruleSetRuleDao;
}



	public IStudyDAO getStudyDao() {
		return this.studyDao;
	}



	public void setStudyDao(IStudyDAO studyDao) {
		this.studyDao = studyDao;
	}



	public DataSource getDataSource() {
		return dataSource;
	}



	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}



	public MetadataCollectorResource(){

	}



	public String collectODMMetadata(String studyOID){

		StudyBean studyBean = getStudyDao().findByOid(studyOID);

	    MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean,getRuleSetRuleDao());
        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
        MetaDataCollector.setTextLength(200);

        ODMBean odmb = mdc.getODMBean();
        odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<String>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
        odmb.setXmlnsList(xmlnsList);
        odmb.setODMVersion("oc1.3");
        mdc.setODMBean(odmb);
     adc.setOdmbean(odmb);
        mdc.collectFileData();
   adc.collectFileData();

        FullReportBean report = new FullReportBean();
        report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        report.setCoreResources(getCoreResources());
        report.setOdmBean(mdc.getODMBean());
        report.setODMVersion("oc1.3");
        report.createStudyMetaOdmXml(Boolean.FALSE);

        return report.getXmlOutput().toString().trim();

	}


	public String collectODMMetadataJson(String studyOID){
		net.sf.json.xml.XMLSerializer xmlserializer = new XMLSerializer();
		JSON json = xmlserializer.read(collectODMMetadata(studyOID));
		return json.toString(INDENT_LEVEL);

	}



	public JSON collectODMMetadataJson(String studyOID,String formVersionOID){
		net.sf.json.xml.XMLSerializer xmlserializer = new XMLSerializer();
		JSON json = xmlserializer.read(collectODMMetadataForForm(studyOID,formVersionOID));
		return json;
	}

	public String collectODMMetadataJsonString(String studyOID,String formVersionOID){
		net.sf.json.xml.XMLSerializer xmlserializer = new XMLSerializer();
		JSON json = xmlserializer.read(collectODMMetadataForForm(studyOID,formVersionOID));
		return json.toString(INDENT_LEVEL);
	}

	public String collectODMMetadataForForm(String studyOID,String formVersionOID) {
		StudyBean studyBean = getStudyDao().findByOid(studyOID);
	if(studyBean!=null)
		studyBean  = populateStudyBean(studyBean);
	    MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean,getRuleSetRuleDao());
        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
        MetaDataCollector.setTextLength(200);

        ODMBean odmb = mdc.getODMBean();
        odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<String>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
        odmb.setXmlnsList(xmlnsList);
        odmb.setODMVersion("oc1.3");
        mdc.setODMBean(odmb);
        adc.setOdmbean(odmb);
        if(studyBean==null)
        mdc.collectFileData(formVersionOID);
        else
	mdc.collectFileData();
        adc.collectFileData();

        FullReportBean report = new FullReportBean();
        report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        report.setCoreResources(getCoreResources());
        report.setOdmBean(mdc.getODMBean());
        report.setODMVersion("oc1.3");
        report.createStudyMetaOdmXml(Boolean.FALSE);


		return report.getXmlOutput().toString().trim();
	}


	public FullReportBean collectODMMetadataForClinicalData(String studyOID,String formVersionOID, LinkedHashMap<String,OdmClinicalDataBean> clinicalDataMap)
	{
		StudyBean studyBean = getStudyDao().findByOid(studyOID);
		if(studyBean!=null)
			studyBean  = populateStudyBean(studyBean);
		    MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean,getRuleSetRuleDao());
	        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
	        MetaDataCollector.setTextLength(200);

	        ODMBean odmb = mdc.getODMBean();
	        odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
	        ArrayList<String> xmlnsList = new ArrayList<String>();
	        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
	        //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
	        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
	        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
	        odmb.setXmlnsList(xmlnsList);
	        odmb.setODMVersion("oc1.3");
	        mdc.setODMBean(odmb);
	        adc.setOdmbean(odmb);
	        if(studyBean==null)
	        mdc.collectFileData(formVersionOID);
	        else
	        	mdc.collectFileData();
	        adc.collectFileData();


	        FullReportBean report = new FullReportBean();
	        report.setAdminDataMap(adc.getOdmAdminDataMap());
	        report.setOdmStudyMap(mdc.getOdmStudyMap());
	        report.setCoreResources(getCoreResources());
	        report.setOdmBean(mdc.getODMBean());
	        //report.setClinicalData(odmClinicalDataBean);

			report.setClinicalDataMap(clinicalDataMap);
	        report.setODMVersion("oc1.3");


	        return report;
	}
	private StudyBean populateStudyBean(StudyBean studyBean) {
		 IStudyParameterValueDAO spvdao = this.studyParameterValueDao;
		  @SuppressWarnings("rawtypes")
		ArrayList studyParameters = spvdao.findParamConfigByStudy(studyBean);

		  studyBean.setStudyParameters(studyParameters);
          if (studyBean.getParentStudyId() <= 0) {// top study
              studyBean = studyConfigService.setParametersForStudy(studyBean);

          } else {
              // YW <<
              studyBean.setParentStudyName(((StudyBean) getStudyDao().findByPK(studyBean.getParentStudyId())).getName());
              // YW >>
              studyBean = studyConfigService.setParametersForSite(studyBean);
          }

	return studyBean;
	}


}

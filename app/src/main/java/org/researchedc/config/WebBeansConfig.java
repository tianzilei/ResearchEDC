package org.researchedc.config;

import javax.sql.DataSource;

import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.extract.ArchivedDatasetFileDAO;
import org.researchedc.dao.extract.DatasetDAO;
import org.researchedc.dao.hibernate.RuleSetRuleDao;
import org.researchedc.dao.hibernate.RuleDao;
import org.researchedc.dao.hibernate.RuleSetDao;
import org.researchedc.dao.spi.DatasetDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.service.crfdata.InstantOnChangeService;
import org.researchedc.service.extract.GenerateExtractFileService;
import org.researchedc.service.extract.OdmFileCreation;
import org.researchedc.service.rule.RulesPostImportContainerService;
import org.researchedc.validator.rule.action.EventActionValidator;
import org.researchedc.validator.rule.action.InsertActionValidator;
import org.researchedc.validator.rule.action.RandomizeActionValidator;
import org.researchedc.web.table.sdv.SDVUtil;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Replaces applicationContext-web-beans.xml.
 *
 * <p>Migrates the legacy XML bean definitions to Java Config. Currently contains
 * the {@code sdvUtil} bean which requires {@code dataSource} injection.</p>
 */
@Configuration
public class WebBeansConfig {

    /**
     * Creates the {@link SDVUtil} bean used by SDV-related servlets and controllers.
     * Replaces the XML-defined {@code sdvUtil} bean with its {@code dataSource} property.
     */
    @Bean("sdvUtil")
    public SDVUtil sdvUtil(DataSource dataSource) {
        SDVUtil sdvUtil = new SDVUtil();
        sdvUtil.setDataSource(dataSource);
        return sdvUtil;
    }

    @Bean("instantOnChangeService")
    public InstantOnChangeService instantOnChangeService(DataSource dataSource) {
        return new InstantOnChangeService(dataSource, new ItemFormMetadataDAO(dataSource));
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DatasetDAO extractDatasetDao(DataSource dataSource) {
        return new DatasetDAO(dataSource);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ItemFormMetadataDAO extractItemFormMetadataDao(DataSource dataSource) {
        return new ItemFormMetadataDAO(dataSource);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ArchivedDatasetFileDAO extractArchivedDatasetFileDao(DataSource dataSource) {
        return new ArchivedDatasetFileDAO(dataSource);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public OdmFileCreation odmFileCreation(DataSource dataSource, CoreResources coreResources,
            RuleSetRuleDao ruleSetRuleDao, DatasetDao extractDatasetDao,
            ArchivedDatasetFileDAO extractArchivedDatasetFileDao) {
        return new OdmFileCreation(dataSource, coreResources, ruleSetRuleDao,
                extractDatasetDao, extractArchivedDatasetFileDao);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GenerateExtractFileService generateExtractFileService(DataSource dataSource,
            CoreResources coreResources, RuleSetRuleDao ruleSetRuleDao, DatasetDao extractDatasetDao,
            ItemFormMetadataDAO extractItemFormMetadataDao,
            ArchivedDatasetFileDAO extractArchivedDatasetFileDao, OdmFileCreation odmFileCreation) {
        return new GenerateExtractFileService(dataSource, coreResources, ruleSetRuleDao,
                extractDatasetDao, extractItemFormMetadataDao, extractArchivedDatasetFileDao,
                odmFileCreation);
    }

    @Bean("rulesPostImportContainerService")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RulesPostImportContainerService rulesPostImportContainerService(DataSource dataSource,
            RuleDao ruleDao, RuleSetDao ruleSetDao, IStudyDAO studyDao) {
        RulesPostImportContainerService service = new RulesPostImportContainerService(dataSource);
        service.setRuleDao(ruleDao);
        service.setRuleSetDao(ruleSetDao);
        service.setInsertActionValidator(new InsertActionValidator(dataSource));
        service.setEventActionValidator(new EventActionValidator(dataSource, studyDao));
        service.setRandomizeActionValidator(new RandomizeActionValidator(dataSource));
        return service;
    }
}

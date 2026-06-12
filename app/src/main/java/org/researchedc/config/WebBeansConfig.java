package org.researchedc.config;

import javax.sql.DataSource;

import org.researchedc.dao.LegacyDaoFactory;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.spi.IRuleSetRuleDAO;
import org.researchedc.dao.spi.RuleDomainDao;
import org.researchedc.dao.spi.RuleSetDomainDao;
import org.researchedc.dao.spi.ArchivedDatasetFileDao;
import org.researchedc.dao.spi.DatasetDao;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.service.crfdata.InstantOnChangeService;
import org.researchedc.service.extract.GenerateExtractFileService;
import org.researchedc.service.extract.OdmFileCreation;
import org.researchedc.service.rule.RulesPostImportContainerService;
import org.researchedc.validator.rule.action.EventActionValidator;
import org.researchedc.validator.rule.action.InsertActionValidator;
import org.researchedc.validator.rule.action.RandomizeActionValidator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Legacy web beans migrated from XML to Java Config.
 */
@Configuration
public class WebBeansConfig {

    @Bean("instantOnChangeService")
    public InstantOnChangeService instantOnChangeService(DataSource dataSource,
            IItemFormMetadataDAO itemFormMetadataDao) {
        return new InstantOnChangeService(dataSource, itemFormMetadataDao);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DatasetDao extractDatasetDao(DataSource dataSource) {
        return LegacyDaoFactory.datasetDao(dataSource);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IItemFormMetadataDAO extractItemFormMetadataDao(
            IItemFormMetadataDAO itemFormMetadataDao) {
        return itemFormMetadataDao;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ArchivedDatasetFileDao extractArchivedDatasetFileDao(DataSource dataSource) {
        return LegacyDaoFactory.archivedDatasetFileDao(dataSource);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public OdmFileCreation odmFileCreation(DataSource dataSource, CoreResources coreResources,
            IRuleSetRuleDAO ruleSetRuleDao, DatasetDao extractDatasetDao,
            ArchivedDatasetFileDao extractArchivedDatasetFileDao) {
        return new OdmFileCreation(dataSource, coreResources, ruleSetRuleDao,
                extractDatasetDao, extractArchivedDatasetFileDao);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GenerateExtractFileService generateExtractFileService(DataSource dataSource,
            CoreResources coreResources, IRuleSetRuleDAO ruleSetRuleDao, DatasetDao extractDatasetDao,
            IItemFormMetadataDAO extractItemFormMetadataDao,
            ArchivedDatasetFileDao extractArchivedDatasetFileDao, OdmFileCreation odmFileCreation) {
        return new GenerateExtractFileService(dataSource, coreResources, ruleSetRuleDao,
                extractDatasetDao, extractItemFormMetadataDao, extractArchivedDatasetFileDao,
                odmFileCreation);
    }

    @Bean("rulesPostImportContainerService")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RulesPostImportContainerService rulesPostImportContainerService(DataSource dataSource,
            RuleDomainDao ruleDao, RuleSetDomainDao ruleSetDao, IStudyDAO studyDao) {
        RulesPostImportContainerService service = new RulesPostImportContainerService(dataSource);
        service.setRuleDao(ruleDao);
        service.setRuleSetDao(ruleSetDao);
        service.setInsertActionValidator(new InsertActionValidator(dataSource));
        service.setEventActionValidator(new EventActionValidator(dataSource, studyDao));
        service.setRandomizeActionValidator(new RandomizeActionValidator(dataSource));
        return service;
    }
}

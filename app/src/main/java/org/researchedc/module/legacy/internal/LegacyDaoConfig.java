package org.researchedc.module.legacy.internal;

import javax.sql.DataSource;
import org.researchedc.dao.extract.DatasetDAO;
import org.researchedc.dao.extract.FilterDAO;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;
import org.researchedc.dao.managestudy.StudyGroupClassDAO;
import org.researchedc.dao.managestudy.StudyGroupDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares legacy-core DAOs as Spring beans so they can be injected into
 * legacy-gateway controllers instead of being instantiated with {@code new}.
 *
 * <p>This is a transitional configuration. As each DAO is replaced by a
 * proper Spring Data JPA repository (see Sequence 2 migration), the
 * corresponding bean declaration here can be removed.</p>
 */
@Configuration
public class LegacyDaoConfig {

    @Bean
    public DiscrepancyNoteDAO discrepancyNoteDao(DataSource dataSource) {
        return new DiscrepancyNoteDAO(dataSource);
    }

    @Bean
    public DatasetDAO datasetDao(DataSource dataSource) {
        return new DatasetDAO(dataSource);
    }

    @Bean
    public StudyGroupClassDAO studyGroupClassDao(DataSource dataSource) {
        return new StudyGroupClassDAO(dataSource);
    }

    @Bean
    public StudyGroupDAO studyGroupDao(DataSource dataSource) {
        return new StudyGroupDAO(dataSource);
    }

    @Bean
    public FilterDAO filterDao(DataSource dataSource) {
        return new FilterDAO(dataSource);
    }
}

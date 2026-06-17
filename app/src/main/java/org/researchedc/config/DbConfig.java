package org.researchedc.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.researchedc.core.ExtendedBasicDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class DbConfig {

    @Bean
    public DataSource dataSource() {
        String dbHost = System.getenv().getOrDefault("RESEARCHEDC_DB_HOST", "localhost");
        String dbPort = System.getenv().getOrDefault("RESEARCHEDC_DB_PORT", "5432");
        String dbName = System.getenv().getOrDefault("RESEARCHEDC_DB_NAME", "researchedc");
        String dbUser = System.getenv().getOrDefault("RESEARCHEDC_DB_USER", "researchedc");
        String dbPass = System.getenv().getOrDefault("RESEARCHEDC_DB_PASS", "researchedc");
        ExtendedBasicDataSource ds = new ExtendedBasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName);
        ds.setUsername(dbUser);
        ds.setPassword(dbPass);

        // Connection pool tuned for single-site deployment (≤20 users, ≤10 concurrent).
        // Per optimization plan: small pool avoids context-switch overhead.
        ds.setMaxActive(12);             // maximumPoolSize equivalent
        ds.setMaxIdle(4);                // minimumIdle equivalent
        ds.setMinIdle(2);
        ds.setMaxWait(5000);             // fail fast if DB is unavailable
        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(300);
        ds.setLogAbandoned(true);
        ds.setTestWhileIdle(true);
        ds.setTestOnReturn(true);
        ds.setTimeBetweenEvictionRunsMillis(300000);
        ds.setMinEvictableIdleTimeMillis(600000);
        ds.setBigStringTryClob("true");
        return ds;
    }

    @Bean
    @ConditionalOnProperty(name = "researchedc.liquibase.enabled", havingValue = "true", matchIfMissing = false)
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase lb = new SpringLiquibase();
        lb.setDataSource(dataSource);
        lb.setChangeLog("classpath:migration/master.xml");
        lb.setDropFirst(true);
        return lb;
    }

    @Bean(initMethod = "init")
    public QueryStore queryStore(DataSource dataSource) {
        QueryStore qs = new QueryStore();
        qs.setDataSource(dataSource);
        return qs;
    }
}

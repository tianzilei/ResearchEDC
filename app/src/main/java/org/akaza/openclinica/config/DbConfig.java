package org.akaza.openclinica.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.akaza.openclinica.core.ExtendedBasicDataSource;
import org.akaza.openclinica.dao.QueryStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class DbConfig {

    @Bean
    public DataSource dataSource() {
        ExtendedBasicDataSource ds = new ExtendedBasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://localhost:5432/openclinica");
        ds.setUsername("clinica");
        ds.setPassword("clinica");
        ds.setMaxActive(50);
        ds.setMaxIdle(2);
        ds.setMaxWait(180000);
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
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase lb = new SpringLiquibase();
        lb.setDataSource(dataSource);
        lb.setChangeLog("classpath:migration/master.xml");
        return lb;
    }

    @Bean(initMethod = "init")
    public QueryStore queryStore(DataSource dataSource) {
        QueryStore qs = new QueryStore();
        qs.setDataSource(dataSource);
        return qs;
    }
}

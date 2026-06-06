package org.researchedc.dao.spi;

import org.researchedc.domain.technicaladmin.ConfigurationBean;

import java.util.ArrayList;

public interface ConfigurationDao {

    ArrayList<ConfigurationBean> findAll();

    ConfigurationBean findByKey(String key);

    ConfigurationBean saveOrUpdate(ConfigurationBean entity);
}

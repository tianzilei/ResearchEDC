package org.researchedc.ws.internal.adapter;

import org.researchedc.dao.hibernate.ConfigurationDao;
import org.researchedc.domain.technicaladmin.ConfigurationBean;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigurationAdapter {

    private final ConfigurationDao delegate;

    public ConfigurationAdapter(ConfigurationDao delegate) {
        this.delegate = delegate;
    }

    public ConfigurationBean findByKey(String key) {
        return delegate.findByKey(key);
    }

    public ConfigurationDao getDelegate() {
        return delegate;
    }
}

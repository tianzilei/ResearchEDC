package org.researchedc.module.dataimport.internal.adapter;

import java.util.Locale;

import javax.sql.DataSource;

import org.researchedc.web.crfdata.ImportCRFDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImportCrfDataAdapter {

    private static final Logger log = LoggerFactory.getLogger(ImportCrfDataAdapter.class);

    private final DataSource dataSource;

    public ImportCrfDataAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ImportCRFDataService createService(Locale locale) {
        log.debug("Creating legacy ImportCRFDataService adapter for locale: {}", locale);
        return new ImportCRFDataService(dataSource, locale);
    }
}

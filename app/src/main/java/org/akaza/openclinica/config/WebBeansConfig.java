package org.akaza.openclinica.config;

import javax.sql.DataSource;

import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}

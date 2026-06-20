package org.researchedc.config;

import org.apache.commons.dbcp.BasicDataSource;

final class ExtendedBasicDataSource extends BasicDataSource {

    void setBigStringTryClob(String value) {
        addConnectionProperty("SetBigStringTryClob", value);
    }
}

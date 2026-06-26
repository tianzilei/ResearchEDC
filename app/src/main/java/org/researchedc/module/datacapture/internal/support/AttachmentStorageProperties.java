package org.researchedc.module.datacapture.internal.support;

import java.io.File;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AttachmentStorageProperties {

    private final Properties dataInfo;

    public AttachmentStorageProperties(@Qualifier("dataInfoProperties") Properties dataInfo) {
        this.dataInfo = dataInfo;
    }

    public String attachedFileRootPath() {
        String configuredPath = trimmedProperty("attached_file_location");
        if (!configuredPath.isEmpty()) {
            return configuredPath;
        }
        return trimmedProperty("filePath") + "attached_files" + File.separator;
    }

    private String trimmedProperty(String key) {
        String value = dataInfo.getProperty(key);
        return value == null ? "" : value.trim();
    }
}

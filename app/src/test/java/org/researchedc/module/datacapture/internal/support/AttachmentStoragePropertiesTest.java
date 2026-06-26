package org.researchedc.module.datacapture.internal.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class AttachmentStoragePropertiesTest {

    @Test
    void attachedFileRootPath_prefersConfiguredAttachmentLocation() {
        Properties dataInfo = new Properties();
        dataInfo.setProperty("attached_file_location", " /data/attachments/ ");
        dataInfo.setProperty("filePath", "/data/root/");

        AttachmentStorageProperties properties = new AttachmentStorageProperties(dataInfo);

        assertEquals("/data/attachments/", properties.attachedFileRootPath());
    }

    @Test
    void attachedFileRootPath_fallsBackToLegacyFilePathConvention() {
        Properties dataInfo = new Properties();
        dataInfo.setProperty("filePath", "/data/root/");

        AttachmentStorageProperties properties = new AttachmentStorageProperties(dataInfo);

        assertEquals("/data/root/attached_files" + File.separator, properties.attachedFileRootPath());
    }
}

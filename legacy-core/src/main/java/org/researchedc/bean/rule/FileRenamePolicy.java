package org.researchedc.bean.rule;

import java.io.File;
import java.io.InputStream;

public interface FileRenamePolicy {

    public File rename(File f, InputStream content);

}

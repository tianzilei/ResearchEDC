/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2007 Akaza Research
 */

package org.researchedc.bean.core;

import org.researchedc.dao.core.CoreResources;

import java.io.File;

public class Utils {

    public static String getAttachedFileRootPath() {
        String rootPath = CoreResources.getField("attached_file_location");
        if (rootPath == null || rootPath.length() <= 0) {
            rootPath = CoreResources.getField("filePath") + "attached_files" + File.separator;
        }
        return rootPath;
    }
}

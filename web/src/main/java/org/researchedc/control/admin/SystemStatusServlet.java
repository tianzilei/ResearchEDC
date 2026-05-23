/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.researchedc.dao.hibernate.DatabaseChangeLogDao;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.web.util.HtmlUtils;

import java.io.PrintWriter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

// allows both deletion and restoration of a study user role

public class SystemStatusServlet extends SecureController {

    private static final long serialVersionUID = 1722670001851393612L;
    private Locale locale;
    private DatabaseChangeLogDao databaseChangeLogDao;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        return;
    }

    @Override
    protected void processRequest() throws Exception {

        Long databaseChangelLogCount = getDatabaseChangeLogDao().count();
        String applicationStatus = "OK";
        if (session.getAttribute("ome")!=null) {
            applicationStatus = "OutOfMemory.";
        }
//        request.setAttribute("databaseChangeLogCount", String.valueOf(databaseChangelLogCount));
//        request.setAttribute("applicationStatus", applicationStatus);
//        forwardPage(Page.SYSTEM_STATUS);

        PrintWriter out = response.getWriter();
        out.println(applicationStatus);
        out.println(HtmlUtils.htmlEscape(String.valueOf(databaseChangelLogCount)));
    }

    public DatabaseChangeLogDao getDatabaseChangeLogDao() {
        databaseChangeLogDao =
            this.databaseChangeLogDao != null ? databaseChangeLogDao : (DatabaseChangeLogDao) SpringServletAccess.getApplicationContext(context).getBean(
                    "databaseChangeLogDao");
        return databaseChangeLogDao;
    }

    public void setDatabaseChangeLogDao(DatabaseChangeLogDao databaseChangeLogDao) {
        this.databaseChangeLogDao = databaseChangeLogDao;
    }
}

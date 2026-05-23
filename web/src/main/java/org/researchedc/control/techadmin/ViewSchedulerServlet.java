/*
 * Created on Sep 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.researchedc.control.techadmin;

import org.researchedc.control.core.SecureController;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.impl.SimpleLog;
//
// import org.quartz.*;
// import org.quartz.impl.jdbcjobstore.*;

/**
 * @author thickerson
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ViewSchedulerServlet extends SecureController {

    Locale locale;

    // < ResourceBundleresexception;

    @Override
    protected void processRequest() throws Exception {
        // Log psqlLogger = new SimpleLog("QuartzJobLog");
        // //ServletContext ctx = request.getSession().getServletContext();
        // PostgreSQLDelegate delegate = new PostgreSQLDelegate(psqlLogger,
        // "qrtz_",
        // "OpenClinica Jobs");
        // CronTrigger trigger =
        // (CronTrigger)delegate.selectTrigger(sm.getConnection(),
        // "Refresh Data View",
        // "OpenClinica Jobs");
        //
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);

        if (!ub.isTechAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, "You may not perform technical administrative functions", "1");
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

}

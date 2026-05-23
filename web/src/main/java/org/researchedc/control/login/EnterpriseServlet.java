/*
 * Created on Sep 28, 2005
 *
 *
 */
package org.researchedc.control.login;

import org.researchedc.control.core.SecureController;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author thickerson
 *
 *
 */
public class EnterpriseServlet extends SecureController {

    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        setToPanel("", "");
        forwardPage(Page.ENTERPRISE);
    }

}

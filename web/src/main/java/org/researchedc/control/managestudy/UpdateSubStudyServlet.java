package org.researchedc.control.managestudy;

import org.researchedc.control.core.SecureController;
import org.researchedc.web.InsufficientPermissionException;

public class UpdateSubStudyServlet extends SecureController {

    public static final String INPUT_START_DATE = "startDate";
    public static final String INPUT_VER_DATE = "protocolDateVerification";
    public static final String INPUT_END_DATE = "endDate";

    @Override
    protected void processRequest() throws Exception {
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
    }
}

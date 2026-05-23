package org.researchedc.service;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.exception.OpenClinicaSystemException;

import java.util.Date;
import java.util.HashMap;

public interface EventServiceInterface {

    public HashMap<String, String> scheduleEvent(UserAccountBean user, Date startDateTime, Date endDateTime, String location, String studyUniqueId,
            String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException;

}
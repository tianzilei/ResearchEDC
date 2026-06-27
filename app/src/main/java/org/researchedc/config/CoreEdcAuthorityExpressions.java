package org.researchedc.config;

public final class CoreEdcAuthorityExpressions {

    public static final String ADMINISTER_STUDIES =
            "hasAnyRole('SYSADMIN','TECHADMIN','ADMIN','BUSINESS_ADMINISTRATOR',"
                    + "'STUDY_DIRECTOR','STUDYDIRECTOR','DIRECTOR','COORDINATOR',"
                    + "'PRINCIPALINVESTIGATOR')";

    public static final String READ_EDC_DATA =
            "hasAnyRole('SYSADMIN','TECHADMIN','ADMIN','BUSINESS_ADMINISTRATOR',"
                    + "'STUDY_DIRECTOR','STUDYDIRECTOR','DIRECTOR','COORDINATOR','INVESTIGATOR',"
                    + "'RA','DATA_ENTRY','DATAENTRY','DATA SPECIALIST','DATAMANAGER',"
                    + "'DATA_MANAGER','PRINCIPALINVESTIGATOR','MONITOR')";

    public static final String WRITE_EDC_DATA =
            "hasAnyRole('SYSADMIN','TECHADMIN','ADMIN','BUSINESS_ADMINISTRATOR',"
                    + "'STUDY_DIRECTOR','STUDYDIRECTOR','DIRECTOR','COORDINATOR','INVESTIGATOR',"
                    + "'RA','DATA_ENTRY','DATAENTRY','DATA SPECIALIST','DATAMANAGER',"
                    + "'DATA_MANAGER','PRINCIPALINVESTIGATOR')";

    public static final String IMPORT_DATA =
            "hasAnyRole('SYSADMIN','TECHADMIN','ADMIN','BUSINESS_ADMINISTRATOR',"
                    + "'STUDY_DIRECTOR','STUDYDIRECTOR','DIRECTOR','COORDINATOR','DATAMANAGER',"
                    + "'DATA_MANAGER','DATA SPECIALIST')";

    public static final String EXPORT_DATA =
            "hasAnyRole('SYSADMIN','TECHADMIN','ADMIN','BUSINESS_ADMINISTRATOR',"
                    + "'STUDY_DIRECTOR','STUDYDIRECTOR','DIRECTOR','COORDINATOR','DATAMANAGER',"
                    + "'DATA_MANAGER','DATA SPECIALIST')";

    private CoreEdcAuthorityExpressions() {
    }
}

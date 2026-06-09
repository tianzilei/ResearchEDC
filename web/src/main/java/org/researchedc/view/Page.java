/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.view;

/**
 *
 *
 *         Provides a type-safe enumeration for JSP Page,converted from original static class.
 *         @author jnyayapathi
 */
public enum Page {






    /**
     * Page for logging in
     */
      	LOGIN("/WEB-INF/jsp/login/login.jsp", "OpenClinica Login"),
    	LOGIN_USER_ACCOUNT_DELETED("/WEB-INF/jsp/login/login.jsp" + "?action=userAccountDeleted",
                "Unsuccessful Login Due to Account Deletion"),
    /**
     * Page to show the main menu of openclinica
     */
                MENU("/WEB-INF/jsp/menu.jsp", "Welcome to OpenClinica"),
                MENU_SERVLET("/MainMenu", "Welcome to OpenClinica Main Servlet"),

    /**
     * Page for creating a user account.
     */
    CREATE_ACCOUNT("/WEB-INF/jsp/admin/createuseraccount.jsp", "Create an account"),

    /**
     * Page for editing a user account, and confirmation page.
     */
    EDIT_ACCOUNT("/WEB-INF/jsp/admin/edituseraccount.jsp", "Edit an account"),
     EDIT_ACCOUNT_CONFIRM ("/WEB-INF/jsp/admin/edituseraccountconfirm.jsp", "Edit an account"),

    /**
     * Page for viewing all user accounts (for admin)
     */
    LIST_USER_ACCOUNTS ("/WEB-INF/jsp/admin/listuseraccounts.jsp", "List user accounts"),
    LIST_USER_ACCOUNTS_SERVLET ("/ListUserAccounts", "List user accounts"),

    /**
     * Page for viewing a single user account (for admin)
     */
     VIEW_USER_ACCOUNT("/WEB-INF/jsp/admin/viewuseraccount.jsp", "View user account"),
 VIEW_USER_ACCOUNT_SERVLET ("/ViewUserAccount", "View user account servlet"),

    CONFIGURATION ("/WEB-INF/jsp/admin/configuration.jsp", "Configuration"),
   CONFIGURATION_PASSWORD_REQUIREMENTS("/WEB-INF/jsp/admin/configurationPasswordRequirements.jsp", "Configuration"),


    /**
     * Page for update a study.
     */
    UPDATE_STUDY1("/WEB-INF/jsp/managestudy/updateStudy1.jsp", "Update a Study first section"),

    LIST_STUDY_SUBJECTS ("/WEB-INF/jsp/managestudy/findSubjects.jsp", "List Study Subjects"),
    LIST_STUDY_SUBJECTS_SERVLET("/ListStudySubjects", "List Study Subjects"),

     UPDATE_STUDY_SERVLET_NEW ("/UpdateStudyNew", "Update a Study"),
    UPDATE_STUDY_NEW ("/WEB-INF/jsp/managestudy/updateStudyNew.jsp", "Update a Study"),
    UPDATE_SUB_STUDY("/WEB-INF/jsp/managestudy/updateSubStudy.jsp", "Update a sub Study"),

    VIEW_STUDY("/WEB-INF/jsp/admin/viewStudy.jsp", "View study"),

    /**
     * Page for editing a study user role.
     */
    EDIT_STUDY_USER_ROLE ("/WEB-INF/jsp/admin/editstudyuserrole.jsp", "Edit Study User Role"),

    /**
     * Page for view all users of a study and its sites.
     */
    STUDY_USER_LIST ("/WEB-INF/jsp/managestudy/studyUserList.jsp", "View Study Users"),
    /**
     * Page for view all studies.
     */
    STUDY_LIST ("/WEB-INF/jsp/managestudy/studyList.jsp", "View All Studies"),

    /**
     * Page for view all studies.
     */
    STUDY_LIST_SERVLET ("/ListStudy", "View All Studies"),

    REMOVE_STUDY ("/WEB-INF/jsp/admin/removeStudy.jsp", "Remove a Study"),
    RESTORE_STUDY ("/WEB-INF/jsp/admin/restoreStudy.jsp", "Restore a Study"),

    /**
     * Page for view all sites.
     */
     SITE_LIST ("/WEB-INF/jsp/managestudy/siteList.jsp", "View All Sites"),
     SITE_LIST_SERVLET ("/ListSite", "View All Sites Servlet"),

    VIEW_SITE_SERVLET ("/ViewSite", "View a sub Study"),


    /**
     * Page for updating a study event definition.
     */
   UPDATE_EVENT_DEFINITION1 ("/WEB-INF/jsp/managestudy/updateEventDefinition1.jsp", "Update Event Definition"),
    UPDATE_EVENT_DEFINITION2 ("/WEB-INF/jsp/managestudy/updateEventDefinition2.jsp", "Update Event Definition"),

    /**
     * Page for viewing definition
     */
    VIEW_EVENT_DEFINITION ("/WEB-INF/jsp/managestudy/viewEventDefinition.jsp", "View Event Definition"),
    VIEW_EVENT_DEFINITION_READONLY ("/WEB-INF/jsp/managestudy/viewEventDefinitionReadOnly.jsp", "View Event Definition"),
    VIEW_EVENT_DEFINITION_NOSIDEBAR ("/WEB-INF/jsp/managestudy/viewEventDefinitionNoSidebar.jsp", "View Event Definition"),
    VIEW_EVENT_DEFINITION_SERVLET ("/ViewEventDefinition", "View Event Definition Servlet"),

    /**
     * Page for listing seds
     */
    STUDY_EVENT_DEFINITION_LIST ("/WEB-INF/jsp/managestudy/studyEventDefinitionList.jsp", "List all CRFs"),

    /**
     * Page for view all seds.
     */
     LIST_DEFINITION_SERVLET ("/ListEventDefinition", "View All Definitions"),

    /**
     * Page for listing crfs.
     */
    CRF_LIST ("/WEB-INF/jsp/admin/listCRF.jsp", "List all CRFs"),
    CRF_LIST_SERVLET ("/ListCRF", "List all CRFs servlet"),

    /**
     * Page for creating crf.
     */
    VIEW_CRF ("/WEB-INF/jsp/admin/viewCRF.jsp", "View a CRF"),

    /**
     * Page for updating crf.
     */
    UPDATE_CRF ("/WEB-INF/jsp/admin/updateCRF.jsp", "Update a CRF"),

    CREATE_CRF_VERSION ("/WEB-INF/jsp/admin/createCRFVersion.jsp", "Create a new CRF Version"),
    UPLOAD_CRF_VERSION ("/WEB-INF/jsp/admin/uploadCRFVersionFile.jsp", "Upload a new CRF Version"),

    REMOVE_CRF("/WEB-INF/jsp/admin/removeCRF.jsp", "Remove a CRF"),
    RESTORE_CRF ("/WEB-INF/jsp/admin/restoreCRF.jsp", "Restore a CRF"),

    REMOVE_CRF_VERSION ("/WEB-INF/jsp/admin/removeCRFVersion.jsp", "Remove CRF Version"),
    RESTORE_CRF_VERSION ("/WEB-INF/jsp/admin/restoreCRFVersion.jsp", "Restore CRF Version"),

    CREATE_XFORM_CRF_VERSION_SERVLET ("/WEB-INF/jsp/admin/createXformCRFVersion.jsp", "Create a new Xform CRF Version"),

    /**
     * Page for creating crf data imports
     */
    IMPORT_CRF_DATA ("/WEB-INF/jsp/submit/import.jsp", "Import CRF Data"),

    /**
     * Page for confirming crf version.
     */
    CREATE_CRF_VERSION_CONFIRM ("/WEB-INF/jsp/admin/createCRFVersionConfirm.jsp", "Create a new CRF Version Confirm"),
    CREATE_CRF_VERSION_CONFIRMSQL ("/WEB-INF/jsp/admin/createCRFVersionConfirmSQL.jsp",
            "Create a new CRF Version Confirm SQL"),
    CREATE_CRF_VERSION_DONE ("/WEB-INF/jsp/admin/createCRFVersionDone.jsp", "Create a new CRF Version Done"),
    REMOVE_CRF_VERSION_CONFIRM ("/WEB-INF/jsp/admin/removeCRFVersionConfirm.jsp", "Remove CRF Version Confirm"),
    CREATE_CRF_VERSION_NODELETE("/WEB-INF/jsp/admin/createCRFVersionNoDelete.jsp", "Create a new CRF cannot delete version"),
    CREATE_CRF_VERSION_ERROR ("/WEB-INF/jsp/admin/createCRFVersionError.jsp", "Create a new CRF error"),
    REMOVE_CRF_VERSION_DEF ("/WEB-INF/jsp/admin/removeCRFVersionDef.jsp", "Remove CRF Version From Definition"),

    /**
     * Page for extract datasets main, tbh
     */
    EXTRACT_DATASETS_MAIN ("/WEB-INF/jsp/extract/extractDatasetsMain.jsp", "Extract Datasets Main Page"),

    /**
     * Page for view all datasets, tbh
     */
    VIEW_DATASETS ("/WEB-INF/jsp/extract/viewDatasets.jsp", "View Datasets"),
    VIEW_DATASET_DETAILS ("/WEB-INF/jsp/extract/viewDatasetDetails.jsp", "View Dataset Details"),

    EXPORT_DATASETS ("/WEB-INF/jsp/extract/exportDatasets.jsp", "Export Dataset"),
    GENERATE_DATASET ("/WEB-INF/jsp/extract/generatedDataset.jsp", "Generate Dataset"),
    GENERATE_DATASET_HTML ("/WEB-INF/jsp/extract/generatedDatasetHtml.jsp", "Generate Dataset"),
    GENERATE_EXCEL_DATASET ("/WEB-INF/jsp/extract/generatedExcelDataset.jsp", "Generate Excel Dataset"),

    CREATE_DATASET_1 ("/WEB-INF/jsp/extract/createDatasetBegin.jsp", "Create Dataset Begin"),
    CREATE_DATASET_2 ("/WEB-INF/jsp/extract/createDatasetStep2.jsp", "Create Dataset Step Two"),
    CREATE_DATASET_3 ("/WEB-INF/jsp/extract/createDatasetStep3.jsp", "Create Dataset Step Three"),
    CREATE_DATASET_4 ("/WEB-INF/jsp/extract/createDatasetStep4.jsp", "Create Dataset Step Four"),
    CONFIRM_DATASET ("/WEB-INF/jsp/extract/createDatasetConfirmMetadata.jsp", "Create Dataset Step Four"),

    CREATE_DATASET_EVENT_ATTR ("/WEB-INF/jsp/extract/selectEventAttribute.jsp", "Create Dataset and select event Attribute"),
    CREATE_DATASET_SUB_ATTR ("/WEB-INF/jsp/extract/selectSubAttribute.jsp", "Create Dataset and select subject Attribute"),
    CREATE_DATASET_GROUP_ATTR ("/WEB-INF/jsp/extract/selectGroupAttribute.jsp", "Create Dataset and select group Attribute"),
    CREATE_DATASET_CRF_ATTR ("/WEB-INF/jsp/extract/selectCRFAttributes.jsp", "Create Dataset and select CRF Attribute"),
    // CREATE_DATASET_DISC_ATTR = new
    // Page("/WEB-INF/jsp/extract/selectDiscrepancyAttributes.jsp","Create
    // Dataset and select discrepancy Attribute"),
    CREATE_DATASET_APPLY_FILTER ("/WEB-INF/jsp/extract/createDatasetApplyFilter.jsp", "Create Dataset Apply Filter"),

    CREATE_DATASET_VIEW_SELECTED ("/WEB-INF/jsp/extract/viewSelected.jsp", "View Selected Items"),
    CREATE_DATASET_VIEW_SELECTED_HTML ("/WEB-INF/jsp/extract/viewSelectedHtml.jsp", "View Selected Items in a static way"),
    ITEM_DETAIL ("/WEB-INF/jsp/extract/itemDetail.jsp", "Remove Dataset"),
    /**
     * Pages for create and show all filters, tbh
     *
     */
    APPLY_FILTER ("/WEB-INF/jsp/extract/applyFilter.jsp", "Apply Filter"),
    CREATE_FILTER_SCREEN_1 ("/WEB-INF/jsp/extract/createFilterScreen1.jsp", "Create Filter Screen One"),
    CREATE_FILTER_SCREEN_2 ("/WEB-INF/jsp/extract/createFilterScreen2.jsp", "Create Filter Screen Two"),
    CREATE_FILTER_SCREEN_3 ("/WEB-INF/jsp/extract/createFilterScreen3.jsp", "Create Filter Screen Three"),
    CREATE_FILTER_SCREEN_3_1 ("/WEB-INF/jsp/extract/createFilterScreen3_1.jsp", "Create Filter Screen Three Point One"),
    CREATE_FILTER_SCREEN_3_2 ("/WEB-INF/jsp/extract/createFilterScreen3_2.jsp", "Create Filter Screen Three Point Two"),
    CREATE_FILTER_SCREEN_4 ("/WEB-INF/jsp/extract/createFilterScreen4.jsp", "Create Filter Screen Four"),
    CREATE_FILTER_SCREEN_5 ("/WEB-INF/jsp/extract/createFilterScreen5.jsp", "Create Filter Screen Five"),
    VIEW_FILTER_DETAILS ("/WEB-INF/jsp/extract/viewFilterDetails.jsp", "View Filter Details"),
    EDIT_FILTER ("/WEB-INF/jsp/extract/editFilter.jsp", "Edit Filter"),
    EDIT_DATASET ("/WEB-INF/jsp/extract/editDataset.jsp", "Edit Dataset"),
    /**
     * Page to show errors
     */
    ERROR ("/WEB-INF/jsp/error.jsp", "Error Page of OpenClinica"),



    ADMIN_SYSTEM ("/WEB-INF/jsp/" + "admin/index.jsp", "Administer System Menu"),
    MANAGE_STUDY ("/WEB-INF/jsp/" + "managestudy/index.jsp", "Manage Study Menu"),
    MANAGE_STUDY_BODY ("/WEB-INF/jsp/" + "managestudy/managestudy_body.jsp", "Manage Study Menu"),

    CREATE_JOB_EXPORT ("/WEB-INF/jsp/" + "admin/createExportJob.jsp", "Create Export Job"),
    UPDATE_JOB_EXPORT ("/WEB-INF/jsp/" + "admin/updateExportJob.jsp", "Update Export Job"),
    CREATE_JOB_IMPORT ("/WEB-INF/jsp/" + "admin/createImportJob.jsp", "Create Import Job"),
    UPDATE_JOB_IMPORT ("/WEB-INF/jsp/" + "admin/updateImportJob.jsp", "Update Import Job"),
    VIEW_IMPORT_JOB ("/WEB-INF/jsp/" + "admin/viewImportJobs.jsp", "View Import Jobs"),
    VIEW_IMPORT_JOB_SERVLET ("/ViewImportJob", "View Import Jobs"),
    TECH_ADMIN_SYSTEM ("/WEB-INF/jsp/" + "techadmin/index.jsp", "Technical Administrator Menu"),
    ADMIN_SYSTEM_SERVLET ("/AdminSystem", "Administer System Servlet"),
    MANAGE_STUDY_SERVLET ("/ManageStudy", "Manage Study Servlet"),

    CREATE_NEW_STUDY_EVENT ("/WEB-INF/jsp/" + "submit/createNewStudyEvent.jsp", "Create a New Study Event"),
    CREATE_NEW_STUDY_EVENT_SERVLET ("/CreateNewStudyEvent", "Create a New Study Event"),

    INSTRUCTIONS_ENROLL_SUBJECT ("/WEB-INF/jsp/" + "submit/instructionsEnrollSubject.jsp", "Enroll New Subject - Instructions"),
    ADD_NEW_SUBJECT ("/WEB-INF/jsp/" + "submit/addNewSubject.jsp", "Enroll New Subject"),
    ADD_EXISTING_SUBJECT ("/WEB-INF/jsp/" + "submit/addExistingSubject.jsp", "Enroll An Existing Subject"),

    ENTER_DATA_FOR_STUDY_EVENT ("/WEB-INF/jsp/" + "submit/enterDataForStudyEvent.jsp", "Enter Data for a Study Event"),
    ENTER_DATA_FOR_STUDY_EVENT_SERVLET ("/EnterDataForStudyEvent", "Enter Data for a Study Event"),

    TABLE_OF_CONTENTS ("/WEB-INF/jsp/" + "submit/tableOfContents.jsp", "Event CRF Data Submission"),
    TABLE_OF_CONTENTS_SERVLET ("/TableOfContents", "Event CRF Data Submission"),
    INTERVIEWER ("/WEB-INF/jsp/" + "submit/interviewer.jsp", "Event CRF Interview Info Submission"),
    INTERVIEWER_ENTIRE_PAGE ("/WEB-INF/jsp/" + "submit/interviewerEntirePage.jsp", "Event CRF Interview Info Submission"),

    INITIAL_DATA_ENTRY ("/WEB-INF/jsp/" + "submit/initialDataEntry.jsp", "Initial Data Entry"),
    INITIAL_DATA_ENTRY_SERVLET ("/InitialDataEntry", "Initial Data Entry"),

    DOUBLE_DATA_ENTRY ("/WEB-INF/jsp/" + "submit/doubleDataEntry.jsp", "Double Data Entry"),
    DOUBLE_DATA_ENTRY_SERVLET ("/DoubleDataEntry", "Double Data Entry"),

    ADMIN_EDIT ("/WEB-INF/jsp/" + "submit/administrativeEditing.jsp", "Administrative Editing"),
    ADMIN_EDIT_SERVLET ("/AdministrativeEditing", "Administrative Editing Servlet"),

    LIST_USER_IN_STUDY_SERVLET ("/ListStudyUser", "list users in a study"),

    VIEW_SUBJECT ("/WEB-INF/jsp/" + "admin/viewSubject.jsp", "View Subject"),

    // TODO do we need both versions here??? tbh
    LIST_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/listStudySubject.jsp", "list subjects in a study"),
    LIST_STUDY_SUBJECT_SERVLET ("/ListStudySubject", "list subjects in a study"),
    VIEW_STUDY_SUBJECT_SERVLET ("/ViewStudySubject", "View Subject in a study Servlet"),

    UPDATE_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/updateStudySubject.jsp", "update Subject in a study"),
    UPDATE_STUDY_SUBJECT_SERVLET ("/UpdateStudySubject", "update Subject in a study"),
    UPDATE_STUDY_SUBJECT_CONFIRM ("/WEB-INF/jsp/" + "managestudy/updateStudySubjectConfirm.jsp", "update Subject in a study Confirm"),

    REMOVE_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/removeStudySubject.jsp", "Remove Subject from a study"),
    RESTORE_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/restoreStudySubject.jsp", "Restore Subject to a study"),

    REMOVE_STUDY_EVENT ("/WEB-INF/jsp/" + "managestudy/removeStudyEvent.jsp", "Remove Event from a study"),
    RESTORE_STUDY_EVENT ("/WEB-INF/jsp/" + "managestudy/restoreStudyEvent.jsp", "Restore Event to a study"),
    DELETE_STUDY_EVENT ("/WEB-INF/jsp/" + "managestudy/deleteStudyEvent.jsp", "Delete Event from a study"),

    REMOVE_EVENT_CRF ("/WEB-INF/jsp/" + "managestudy/removeEventCRF.jsp", "Remove CRF from event"),
    RESTORE_EVENT_CRF ("/WEB-INF/jsp/" + "managestudy/restoreEventCRF.jsp", "Restore CRF to event"),
    DELETE_EVENT_CRF ("/WEB-INF/jsp/" + "admin/deleteEventCRF.jsp", "Delete CRF from event"),

    UPDATE_SUBJECT ("/WEB-INF/jsp/" + "admin/updateSubject.jsp", "update a subject"),
    UPDATE_SUBJECT_SERVLET ("/UpdateSubject", "update a subject"),
    UPDATE_SUBJECT_CONFIRM ("/WEB-INF/jsp/" + "admin/updateSubjectConfirm.jsp", "confirm update a subject"),
    REASSIGN_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/reassignStudySubject.jsp", "reassign a subject"),
    REASSIGN_STUDY_SUBJECT_CONFIRM ("/WEB-INF/jsp/" + "managestudy/reassignStudySubjectConfirm.jsp", "confirm reassign a subject"),

    REMOVE_SUBJECT ("/WEB-INF/jsp/" + "admin/removeSubject.jsp", "remove a subject"),
    RESTORE_SUBJECT ("/WEB-INF/jsp/" + "admin/restoreSubject.jsp", "restore a subject"),

    SET_USER_ROLE ("/WEB-INF/jsp/" + "admin/setUserRole.jsp", "set a study user role for a user"),
    /**
     * Page for listing subjects.
     */
    SUBJECT_LIST ("/WEB-INF/jsp/admin/listSubject.jsp", "List all Subjects"),

    VIEW_SECTION_DATA_ENTRY ("/WEB-INF/jsp/managestudy/viewSectionDataEntry.jsp", "View Section Data Entry"),
    VIEW_SECTION_DATA_ENTRY_PRINT ("/WEB-INF/jsp/managestudy/viewSectionDataEntryHtml.jsp", "View Section Data Entry Html"),

    VIEW_SECTION_DATA_ENTRY_SERVLET ("/ViewSectionDataEntry", "View Section Data Entry Servlet"),
    EXPORT_DATA_CUSTOM ("", "Dataset Export"),
    UPDATE_STUDY_EVENT ("/WEB-INF/jsp/managestudy/updateStudyEvent.jsp", "Upate Study Event"),
    UPDATE_STUDY_EVENT_SERVLET ("/UpdateStudyEvent", "Upate Study Event"),
    UPDATE_STUDY_EVENT_SIGNED ("/WEB-INF/jsp/managestudy/updateStudyEventSigned.jsp", "Upate Study Event"),
    VIEW_STUDY_EVENTS ("/WEB-INF/jsp/managestudy/viewStudyEvents.jsp", "View Study Events"),

    VIEW_STUDY_EVENTS_PRINT ("/WEB-INF/jsp/managestudy/viewStudyEventsPrint.jsp", "View Study Events"),

    DELETE_CRF_VERSION ("/WEB-INF/jsp/admin/deleteCRFVersion.jsp", "delete CRF Version"),

    ADD_DISCREPANCY_NOTE ("/WEB-INF/jsp/submit/addDiscrepancyNote.jsp", "Add Discrepancy Note"),
    ADD_DISCREPANCY_NOTE_SERVLET ("/CreateDiscrepancyNote", "Add Discrepancy Note"),
    ADD_DISCREPANCY_NOTE_DONE ("/WEB-INF/jsp/submit/addDiscrepancyNoteDone.jsp", "Add Discrepancy Note Done"),
    ADD_DISCREPANCY_NOTE_SAVE_DONE ("/WEB-INF/jsp/submit/addDiscrepancyNoteSaveDone.jsp", "Add Discrepancy Note Save Done"),

    VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET ("/ViewNotes", "View Discrepancy Notes in Study"),

    LIST_EVENTS_FOR_SUBJECT ("/WEB-INF/jsp/submit/listEventsForSubject.jsp", "List Events For Subject"),
    INITIAL_DATA_ENTRY_NW ("/WEB-INF/jsp/submit/initialDataEntryNw.jsp", "Data Entry"),
    CHOOSE_DOWNLOAD_FORMAT ("/WEB-INF/jsp/submit/chooseDownloadFormat.jsp", "Choose download format"),

    FILE_UPLOAD ("/WEB-INF/jsp/submit/uploadFile.jsp", "Form For File Uploading"),
    //UPLOAD_FILE_SERVLET ("/UploadFile", "Upload File"),
    DOWNLOAD_ATTACHED_FILE ("/WEB-INF/jsp/submit/downloadAttachedFile.jsp", "Download Attached File"),


    // MANAGE_STUDY_MODULE = new
    // Page("pages/studymodule", "Manage study");


     MANAGE_STUDY_MODULE ( "/pages/studymodule",null),
     VIEW_SECTION_DATA_ENTRY_SERVLET_REST_URL ("/ViewSectionDataEntryRESTUrlServlet", "View Section Data Entry Servlet for REST Url call");





  //  private final static String path = "/WEB-INF/jsp/";
  //  public final static String servletPath = "/OpenClinica";

	private String fileName;
	private String title;

	/**
     * Constructs the JSP Page instance
     *
     * @param fileName The filename of the JSP page
     * @param title The title of the JSP page
     */
    private Page(String fileName, String title) {
        this.fileName = fileName;
        this.title = title;
    }

    /**
     * Gets the title attribute of the Page object.
     *
     * @return The title value
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the fileName attribute of the Page object.
     *
     * @return The fileName value
     */
    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String newFileName) {
        this.fileName = newFileName;
    }



  /*  public static  Page setNewPage(String fileName, String title) {

    	for (Page p : Page.values())
    	 {
    		if( p.fileName == fileName &&	 p.title == title)
    			return p;
    	 }
    	return null;
    }*/

}

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
     * Page to show the main menu of openclinica
     */
                MENU("/WEB-INF/jsp/menu.jsp", "Welcome to OpenClinica"),
                MENU_SERVLET("/MainMenu", "Welcome to OpenClinica Main Servlet"),

    /**
     * Page for creating a user account.
     */
    // CREATE_ACCOUNT removed — JSP deleted in phase-1-login-profile

    /**
     * Page for editing a user account, and confirmation page.
     */
    // EDIT_ACCOUNT removed — JSP deleted in phase-1-login-profile
    // EDIT_ACCOUNT_CONFIRM removed — JSP deleted in phase-1-login-profile

    /**
     * Page for viewing all user accounts (for admin)
     */
    // LIST_USER_ACCOUNTS removed — JSP deleted in phase-1-login-profile
    // LIST_USER_ACCOUNTS_SERVLET removed — JSP deleted in phase-1-login-profile

    /**
     * Page for viewing a single user account (for admin)
     */
     // VIEW_USER_ACCOUNT removed — JSP deleted in phase-1-login-profile
 // VIEW_USER_ACCOUNT_SERVLET removed — JSP deleted in phase-1-login-profile

// CONFIGURATION_PASSWORD_REQUIREMENTS removed — JSP deleted in phase-1-login-profile


    /**
     * Page for update a study.
     */
    // UPDATE_STUDY1 removed — phase-1-run-29 (InitUpdateStudyServlet + updateStudy1.jsp deleted, 0 consumers)

    // phase-1-run-80: LIST_STUDY_SUBJECTS fixed — findSubjects.jsp deleted (run-79), changed to /ListStudySubjects (redirects to SPA /app/subjects via WebMvcConfig)
    LIST_STUDY_SUBJECTS ("/ListStudySubjects", "List Study Subjects"),
    LIST_STUDY_SUBJECTS_SERVLET("/ListStudySubjects", "List Study Subjects"),
    // UPDATE_STUDY_NEW removed — phase-1-run-54: servlet deleted (dead code, JSP gone, 0 callers)
    // UPDATE_SUB_STUDY removed — servlet deleted in phase-1-group-e

    // EDIT_STUDY_USER_ROLE removed — JSP deleted in Phase 1

    /**
     * Page for view all users of a study and its sites.
     */
    // STUDY_USER_LIST removed — JSP deleted in Phase 1, studyUserList.jsp no longer exists

    /**
     * Page for view all studies.
     */
    STUDY_LIST_SERVLET ("/ListStudy", "View All Studies"),

    /**
     * Page for view all sites.
     */
     SITE_LIST_SERVLET ("/ListSite", "View All Sites Servlet"),

    VIEW_SITE_SERVLET ("/ViewSite", "View a sub Study"),


    /**
     * Page for updating a study event definition.
     */
   // UPDATE_EVENT_DEFINITION1 removed — phase-1-run-29 (InitUpdateEventDefinitionServlet + updateEventDefinition1.jsp deleted, 0 consumers)
    // UPDATE_EVENT_DEFINITION2 removed — JSP deleted in Phase 1, updateEventDefinition2.jsp no longer exists


    // VIEW_EVENT_DEFINITION_READONLY, VIEW_EVENT_DEFINITION_NOSIDEBAR, VIEW_EVENT_DEFINITION_SERVLET removed — servlets deleted in Phase 1 Groups F+G

    /**
     * Page for listing seds
     */
    // STUDY_EVENT_DEFINITION_LIST removed — JSP deleted in Phase 1, studyEventDefinitionList.jsp no longer exists

    /**
     * Page for view all seds.
     */
     LIST_DEFINITION_SERVLET ("/ListEventDefinition", "View All Definitions"),

    /**
     * Page for listing crfs.
     */
    CRF_LIST_SERVLET ("/ListCRF", "List all CRFs servlet"),

    /**
     * Page for creating crf.
     */
    // VIEW_CRF removed — JSP deleted in Phase 1

    /**
     * Page for updating crf.
     */
    // UPDATE_CRF removed — JSP deleted in Phase 1

    // CREATE_CRF_VERSION, UPLOAD_CRF_VERSION, REMOVE_CRF, RESTORE_CRF,
    // REMOVE_CRF_VERSION, RESTORE_CRF_VERSION removed — JSPs deleted in Phase 1

    // CREATE_XFORM_CRF_VERSION_SERVLET removed — phase-1-group-b

    // IMPORT_CRF_DATA removed — import.jsp + ImportCRFDataServlet deleted in run-91

    /**
     * Page for confirming crf version.
     */
    // CREATE_CRF_VERSION_CONFIRM, CREATE_CRF_VERSION_CONFIRMSQL, CREATE_CRF_VERSION_DONE,
    // REMOVE_CRF_VERSION_CONFIRM, CREATE_CRF_VERSION_NODELETE, CREATE_CRF_VERSION_ERROR,
    // REMOVE_CRF_VERSION_DEF removed — JSPs deleted in Phase 1

    // EXTRACT_DATASETS_MAIN removed — JSP deleted in Phase 1

    /**
     * Page for view all datasets, tbh
     */
    // VIEW_DATASETS removed — JSP deleted in Phase 1
    // VIEW_DATASET_DETAILS removed — JSP deleted in Phase 1
    // EXPORT_DATASETS, GENERATE_DATASET_HTML removed — extract servlets deleted in Phase 1
    // CREATE_DATASET_1-4, CONFIRM_DATASET, CREATE_DATASET_EVENT_ATTR, CREATE_DATASET_SUB_ATTR,
    // CREATE_DATASET_GROUP_ATTR, CREATE_DATASET_CRF_ATTR, CREATE_DATASET_APPLY_FILTER,
    // CREATE_DATASET_VIEW_SELECTED, CREATE_DATASET_VIEW_SELECTED_HTML, ITEM_DETAIL,
    // APPLY_FILTER, CREATE_FILTER_SCREEN_1-5, CREATE_FILTER_SCREEN_3_1, CREATE_FILTER_SCREEN_3_2,
    // VIEW_FILTER_DETAILS, EDIT_FILTER, EDIT_DATASET removed — extract servlets deleted in Phase 1

    /**
     * Page to show errors
     */
    ERROR ("/WEB-INF/jsp/error.jsp", "Error Page of OpenClinica"),



    ADMIN_SYSTEM ("/WEB-INF/jsp/" + "admin/index.jsp", "Administer System Menu"),
    // MANAGE_STUDY removed — phase-1-run-75 (ManageStudyServlet + JSP deleted)
    // MANAGE_STUDY_BODY removed — phase-1-run-75

    // CREATE_JOB_EXPORT removed — JSP deleted in Phase 1
    // UPDATE_JOB_EXPORT removed — JSP deleted in Phase 1
    // CREATE_JOB_IMPORT removed — JSP deleted in Phase 1
    // UPDATE_JOB_IMPORT removed — JSP deleted in Phase 1
    // VIEW_IMPORT_JOB removed — JSP deleted in Phase 1
    // VIEW_IMPORT_JOB_SERVLET removed — JSP deleted in Phase 1
    
    MANAGE_STUDY_SERVLET ("/ManageStudy", "Manage Study Servlet"),

    // CREATE_NEW_STUDY_EVENT removed — phase-1-run-48 (CreateNewStudyEventServlet + JSP deleted, SPA SubjectDetail now handles)

    // INSTRUCTIONS_ENROLL_SUBJECT removed — phase-1-run-76 (JSP deleted, 0 forwardPage callers, SPA handles enrollment)
    // ADD_NEW_SUBJECT removed — phase-1-run-76 (JSP deleted, 0 forwardPage callers, SPA handles enrollment)

    // ENTER_DATA_FOR_STUDY_EVENT removed — servlet + JSP deleted, not in web.xml
    // ENTER_DATA_FOR_STUDY_EVENT_SERVLET removed — servlet + JSP deleted, not in web.xml

    // TABLE_OF_CONTENTS removed — servlet + JSP deleted, not in web.xml
    TABLE_OF_CONTENTS_SERVLET ("/TableOfContents", "Event CRF Data Submission"), // retained for TableOfContentsHelper
    INTERVIEWER ("/WEB-INF/jsp/" + "submit/interviewer.jsp", "Event CRF Interview Info Submission"),

    INITIAL_DATA_ENTRY ("/WEB-INF/jsp/" + "submit/initialDataEntry.jsp", "Initial Data Entry"),
    INITIAL_DATA_ENTRY_SERVLET ("/InitialDataEntry", "Initial Data Entry"),

    DOUBLE_DATA_ENTRY ("/WEB-INF/jsp/" + "submit/doubleDataEntry.jsp", "Double Data Entry"),
    DOUBLE_DATA_ENTRY_SERVLET ("/DoubleDataEntry", "Double Data Entry"),

    ADMIN_EDIT ("/WEB-INF/jsp/" + "submit/administrativeEditing.jsp", "Administrative Editing"),
    ADMIN_EDIT_SERVLET ("/AdministrativeEditing", "Administrative Editing Servlet"),

    LIST_USER_IN_STUDY_SERVLET ("/ListStudyUser", "list users in a study"),

    // VIEW_SUBJECT removed — phase-1-group-c

    // TODO do we need both versions here??? tbh
    // LIST_STUDY_SUBJECT removed — phase-1-run-55: servlet deleted (orphaned), JSP deleted below
    VIEW_STUDY_SUBJECT_SERVLET ("/ViewStudySubject", "View Subject in a study Servlet"),

    UPDATE_STUDY_SUBJECT_SERVLET ("/UpdateStudySubject", "update Subject in a study"),

    // REMOVE_STUDY_EVENT removed — phase-1-run-35 (RemoveStudyEventServlet + JSP deleted, SPA EventList now handles)
    // RESTORE_STUDY_EVENT removed — phase-1-run-27 (RestoreStudyEventServlet + JSP deleted, 0 consumers)
    // DELETE_STUDY_EVENT removed — phase-1-run-29 (DeleteStudyEventServlet + deleteStudyEvent.jsp deleted, 0 consumers)

    // REMOVE_EVENT_CRF removed — phase-1-run-37 (RemoveEventCRFServlet + JSP deleted, SPA EntityAction now handles)
    // RESTORE_EVENT_CRF removed — phase-1-run-31 (RestoreEventCRFServlet deleted in Run #27; JSP orphaned and deleted)
    // DELETE_EVENT_CRF removed — phase-1-group-b

    // UPDATE_SUBJECT removed — phase-1-group-c
    // UPDATE_SUBJECT_SERVLET removed — phase-1-group-c
    // SET_USER_ROLE removed — JSP deleted in phase-1-login-profile
    /**
     * Page for listing subjects.
     */
    // SUBJECT_LIST removed — JSP deleted in Phase 1

    VIEW_SECTION_DATA_ENTRY ("/WEB-INF/jsp/managestudy/viewSectionDataEntry.jsp", "View Section Data Entry"),
    VIEW_SECTION_DATA_ENTRY_PRINT ("/WEB-INF/jsp/managestudy/viewSectionDataEntryHtml.jsp", "View Section Data Entry Html"),

    VIEW_SECTION_DATA_ENTRY_SERVLET ("/ViewSectionDataEntry", "View Section Data Entry Servlet"),
    EXPORT_DATA_CUSTOM ("", "Dataset Export"),
    // UPDATE_STUDY_EVENT, UPDATE_STUDY_EVENT_SERVLET, UPDATE_STUDY_EVENT_SIGNED removed — UpdateStudyEventServlet + JSPs deleted in Phase 1
    // VIEW_STUDY_EVENTS, VIEW_STUDY_EVENTS_PRINT removed — ViewStudyEventsServlet + JSPs deleted in Phase 1

    // DELETE_CRF_VERSION removed — phase-1-group-b

    ADD_DISCREPANCY_NOTE ("/WEB-INF/jsp/submit/addDiscrepancyNote.jsp", "Add Discrepancy Note"),
    ADD_DISCREPANCY_NOTE_DONE ("/WEB-INF/jsp/submit/addDiscrepancyNoteDone.jsp", "Add Discrepancy Note Done"),
    ADD_DISCREPANCY_NOTE_SAVE_DONE ("/WEB-INF/jsp/submit/addDiscrepancyNoteSaveDone.jsp", "Add Discrepancy Note Save Done"),

    VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET ("/ViewNotes", "View Discrepancy Notes in Study"),

    // LIST_EVENTS_FOR_SUBJECT removed — phase-1-run-55: servlet + JSP deleted (orphaned, 0 active callers)
    INITIAL_DATA_ENTRY_NW ("/WEB-INF/jsp/submit/initialDataEntryNw.jsp", "Data Entry"),
    // CHOOSE_DOWNLOAD_FORMAT removed — phase-1-run-61: JSP deleted (orphaned, 0 active references)

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

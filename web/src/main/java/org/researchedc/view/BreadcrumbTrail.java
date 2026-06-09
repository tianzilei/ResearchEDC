/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.view;

import org.researchedc.bean.core.Status;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.i18n.util.ResourceBundleProvider;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Maintain the breadcrumbs on the page, remain seamless, for example, it gets
 * set when the Page gets set in the forwardPage() method in the
 * SecureController servlet, Keep track of metadata being sent in the request,
 * so that users can go back down the bread crumb trail.
 *
 * @author thickerson
 *
 */
public class BreadcrumbTrail {
    private ArrayList trail = new ArrayList();

    public BreadcrumbTrail() {

    }

    public BreadcrumbTrail(ArrayList trail) {

        this.trail = trail;

    }

    /**
     * @return Returns the trail.
     */
    public ArrayList getTrail() {
        return trail;
    }

    /**
     * @param trail
     *            The trail to set.
     */
    public void setTrail(ArrayList trail) {
        this.trail = trail;
    }

    /**
     * method to be called right before forwardPage() in the SecureController.
     * Generates an arraylist of breadcrumb beans, which is then set to the
     * request/session. Has the possibility of getting quite long, since we will
     * be setting up all breadcrumb bean configurations here based on the Page
     * submitted to us.
     *
     * @param jspPage
     *            the page which is the new target.
     * @param request
     *            the HTTP request which we will construct the URL with.
     * @return ArrayList of breadcrumb
     */
    public ArrayList generateTrail(Page jspPage, HttpServletRequest request) {

        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundle resworkflow = ResourceBundleProvider.getWorkflowBundle(locale);

        try {
            // ArrayList newTrail = new ArrayList();

            // VIEW_STUDY_SUBJECT branch removed — JSP deleted in Phase 1 slice
            // UPDATE_STUDY_EVENT branch removed — UpdateStudyEventServlet + JSP deleted in Phase 1

            if (jspPage.equals(Page.INSTRUCTIONS_ENROLL_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("submit_data"), "ListStudySubjectsSubmit", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("enroll_subject_instructions"), "AddNewSubject?instr=1", Status.PENDING));
                trail.add(new BreadcrumbBean(resworkflow.getString("enroll_subject"), "AddNewSubject", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("add_new_study_event"), "CreateNewStudyEvent", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("study_event_overview"), "EnterDataForStudyEvent", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("event_CRF_data_submission"), "TableOfContents", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("data_entry"), "InitialDataEntry", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("mark_event_CRF_complete"), "MarkEventCRFComplete", Status.UNAVAILABLE));
            } else if (jspPage.equals(Page.ADD_NEW_SUBJECT)) {
                trail = advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("enroll_subject"), "AddNewSubject", Status.PENDING), 2);
                closeRestOfTrail(2);
            } else if (jspPage.equals(Page.CREATE_NEW_STUDY_EVENT)) {
                if (!containsServlet("AddNewSubject")) {
                    trail = new ArrayList();
                    trail.add(new BreadcrumbBean(resworkflow.getString("submit_data"), "ListStudySubjectsSubmit", Status.AVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("add_new_study_event"), "CreateNewStudyEvent", Status.PENDING));
                    trail.add(new BreadcrumbBean(resworkflow.getString("study_event_overview"), "EnterDataForStudyEvent", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("event_CRF_data_submission"), "TableOfContents", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("data_entry"), "InitialDataEntry", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("mark_event_CRF_complete"), "MarkEventCRFComplete", Status.UNAVAILABLE));
                } else {
                    trail = advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("add_new_study_event"), "CreateNewStudyEvent", Status.PENDING), 3);
                    closeRestOfTrail(3);
                }
            // ENTER_DATA_FOR_STUDY_EVENT branch removed — servlet + JSP deleted, not in web.xml
            // TABLE_OF_CONTENTS branch removed — servlet + JSP deleted, not in web.xml
            } else if (jspPage.equals(Page.INITIAL_DATA_ENTRY)) {
                int ordinal = trail.size() - 2;
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("data_entry"), "InitialDataEntry" + this.generateURLString(request),
                            Status.PENDING), ordinal);
                closeRestOfTrail(ordinal);
            }

            // CREATE_STUDY1-8 + STUDY_CREATE_CONFIRM branches removed — JSPs deleted in Phase 1 slice
            // UPDATE_STUDY1-8 + STUDY_UPDATE_CONFIRM branches removed — JSPs deleted in Phase 1 slice

            else if (jspPage.equals(Page.ADMIN_SYSTEM)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.MANAGE_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.MANAGE_STUDY_BODY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "#", Status.PENDING));
            }

            // LIST_USER_IN_STUDY branch removed — JSP deleted in Phase 1 slice

            else if (jspPage.equals(Page.LIST_STUDY_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_subjects"), "ListStudySubject", Status.PENDING));
            }

            // SITE_LIST, STUDY_EVENT_DEFINITION_LIST branches removed — JSPs deleted in Phase 1 slice
            // SUBJECT_GROUP_CLASS_LIST, CREATE_SUBJECT_GROUP_CLASS branches removed — JSPs deleted in Phase 1 slice

            // CRF_LIST branch removed — JSP deleted in Phase 1

            // LIST_USER_ACCOUNTS branch removed — JSPs deleted in phase-1-login-profile

            // CRF_VERSION, VIEW_CRF, REMOVE_CRF, RESTORE_CRF, REMOVE_CRF_VERSION,
            // RESTORE_CRF_VERSION, UPDATE_CRF, VIEW_SUBJECT, UPDATE_SUBJECT branches removed — JSPs deleted in Phase 1

            // VIEW_USER_ACCOUNT branch removed — JSPs deleted in phase-1-login-profile
            // EDIT_ACCOUNT branch removed — JSPs deleted in phase-1-login-profile
            // EDIT_ACCOUNT_CONFIRM branch removed — JSPs deleted in phase-1-login-profile
            // CREATE_ACCOUNT branch removed — JSPs deleted in phase-1-login-profile

            // REASSIGN_STUDY_SUBJECT, DEFINE_STUDY_EVENT1, UPDATE_EVENT_DEFINITION1,
            // VIEW_EVENT_DEFINITION, CREATE_SUB_STUDY, VIEW_SITE branches removed — JSPs deleted in Phase 1
            // SET_USER_ROLE_IN_STUDY, STUDY_USER_LIST branches removed — JSPs deleted
            // LOCK_DEFINITION, UNLOCK_DEFINITION branches removed — JSPs deleted
            // VIEW_USER_IN_STUDY, REMOVE_USER_ROLE_IN_STUDY branches removed — JSPs deleted
            // REMOVE_DEFINITION, RESTORE_DEFINITION branches removed — JSPs deleted
            // REMOVE_SITE, RESTORE_SITE branches removed — JSPs deleted

            // VIEW_STUDY, REMOVE_STUDY, RESTORE_STUDY, UPDATE_SUBJECT, REMOVE_SUBJECT, RESTORE_SUBJECT branches removed — JSPs deleted in Phase 1

            // SET_USER_ROLE branch removed — JSPs deleted in phase-1-login-profile

            // REMOVE_SITE, RESTORE_SITE branches removed — JSPs deleted in Phase 1

            if (jspPage.equals(Page.MENU)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("home"), "MainMenu", Status.PENDING));

            }

            // VIEW_TABLE_OF_CONTENT branch removed — JSP deleted in Phase 1

            else if (jspPage.equals(Page.VIEW_SECTION_DATA_ENTRY) || jspPage.equals(Page.VIEW_SECTION_DATA_ENTRY_SERVLET)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_CRF_version_section_data"), "#", Status.PENDING));

            }

            // VIEW_EVENT_CRF_CONTENT, VIEW_STUDY_EVENTS, DELETE_CRF_VERSION branches removed — JSPs deleted in Phase 1

            // All extract/dataset/filter breadcrumb branches removed — JSPs deleted in Phase 1

            // else {
            // trail = new ArrayList();
            // }
        } catch (IndexOutOfBoundsException ioobe) {
            // TODO Auto-generated catch block, created to disallow errors
            ioobe.printStackTrace();

            trail = new ArrayList();
        }

        return trail;
    }

    public String generateURLString(HttpServletRequest request) {
        String newURL = "?";
        FormProcessor fp = new FormProcessor(request);
        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String title = (String) en.nextElement();
            String value = fp.getString(title);
            newURL += title + "=" + value + "&";
        }
        return newURL;
    }

    public ArrayList advanceTrail(ArrayList trail, BreadcrumbBean newBean, int ordinal) {

        int previous = ordinal - 1;

        BreadcrumbBean bcb;

        if (previous >= 0 && previous < trail.size()) {
            bcb = (BreadcrumbBean) trail.remove(previous);
            bcb.setStatus(Status.AVAILABLE);
            trail.add(previous, bcb);
        }

        if (ordinal >= 0 && ordinal < trail.size()) {
            bcb = (BreadcrumbBean) trail.remove(ordinal);
            trail.add(ordinal, newBean);
        }

        return trail;
    }

    /**
     * Determines if the trail contains a particular servlet.
     *
     * @param servlet
     *            The name of the servlet.
     * @return <code>true</code> if one of the elements refers to the
     *         specified servlet, <code>false</code> otherwise.
     */
    public boolean containsServlet(String servlet) {
        servlet = servlet.toLowerCase();
        for (int i = 0; i < trail.size(); i++) {
            BreadcrumbBean b = (BreadcrumbBean) trail.get(i);
            if (b.getUrl().toLowerCase().indexOf(servlet) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make everything in the trail after the specified ordinal unavailable.
     *
     * It is recommended that this method be called after advanceTrail. Using
     * this method ensures that if the user got to the current page by "going
     * back" through the trail, all the "future" pages will be marked
     * unavailable.
     *
     * @param ordinal
     *            The index after which everything will be unavailable.
     */
    private void closeRestOfTrail(int ordinal) {
        if (ordinal < 0) {
            return;
        }

        for (int i = ordinal + 1; i < trail.size(); i++) {
            BreadcrumbBean b = (BreadcrumbBean) trail.get(i);
            b.setStatus(Status.UNAVAILABLE);
            trail.set(i, b);
        }

        return;
    }

    /**
     * Make the breadcrumb at position ordinal unavailable.
     *
     * @param ordinal
     *            The index of the breadcrumb.
     */
    private void closeBreadcrumb(int ordinal) {
        if (ordinal < 0 || ordinal >= trail.size()) {
            return;
        }

        BreadcrumbBean b = (BreadcrumbBean) trail.get(ordinal);
        b.setStatus(Status.UNAVAILABLE);
        trail.set(ordinal, b);

        return;
    }

    /**
     * Makes all breadcrumbs previous to this one open. Good for when you have
     * to skip a few steps ahead.
     *
     * @author thickerson
     * @param ordinal
     *            the index of the current breadcrumb.
     *
     */
    private void openBreadcrumbs(int ordinal) {
        if (ordinal < 0 || ordinal > trail.size()) {
            return;
        }

        for (int i = 0; i < ordinal; i++) {
            BreadcrumbBean b = (BreadcrumbBean) trail.get(i);
            b.setStatus(Status.AVAILABLE);
            trail.set(i, b);
        }
        return;
    }
}
/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.extract;

import org.researchedc.dao.extract.FilterDAO;
import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.extract.FilterBean;
import org.researchedc.bean.extract.FilterObjectBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.web.bean.EntityBeanTable;
import org.researchedc.web.bean.FilterRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * <P>
 * Meant to serve as the specify parameters and specify criteria for the create
 * filter process (Screens 3 and 4).
 *
 * @author thickerson
 *
 */
public class CreateFiltersTwoServlet extends SecureController {

    @Autowired
    protected SectionDAO sectionDao;

    @Autowired
    protected FilterDAO filterDao;

    @Autowired
    protected CRFVersionDAO crfVersionDao;

    @Autowired
    protected ItemFormMetadataDAO itemFormMetadataDao;

    Locale locale;

    // < ResourceBundle restext,resword,respage,resexception;

    @Override
    public void processRequest() throws Exception {
        // we can't get to here without an action:
        // begin--takes us to specify parameters,
        // where the user will select CRF, then section,
        // then parameters of the section: a little tricky
        // to do entirely with javascript, but can be done.
        // possible to set up the CRF-section relationship,
        // and then have a screen 3.5 with the parameters?
        // criteria--takes us to specify criteria, that is,
        // to specify and, or, not and, etc. here we'll take the
        // list we just generated online and generate a chain
        // of filterobjectbeans, which will in turn,
        // generate the SQL add on for the dataset.
        String action = request.getParameter("action");

        StudyBean studyWithEventDefs = currentStudy;
        if (currentStudy.getParentStudyId() > 0) {
            studyWithEventDefs = new StudyBean();
            studyWithEventDefs.setId(currentStudy.getParentStudyId());
        }

        if (StringUtil.isBlank(action)) {
            // throw an error

        } else if ("begin".equalsIgnoreCase(action)) {
            IStudyEventDAO sedao = this.studyEventDao;
            HashMap events = sedao.findCRFsByStudy(studyWithEventDefs);
            // if events are empty -- resend to first filter page with message
            if (events.isEmpty()) {
                addPageMessage(respage.getString("no_CRF_assigned_pick_another"));
                FormProcessor fp = new FormProcessor(request);
                FilterDAO fdao = this.filterDao;
                EntityBeanTable table = fp.getEntityBeanTable();

                ArrayList filters = (ArrayList) fdao.findAll();
                ArrayList filterRows = FilterRow.generateRowsFromBeans(filters);

                String[] columns =
                    { resword.getString("filter_name"), resword.getString("description"), resword.getString("created_by"), resword.getString("created_date"),
                        resword.getString("status"), resword.getString("actions") };

                table.setColumns(new ArrayList(Arrays.asList(columns)));
                table.hideColumnLink(5);
                table.addLink(resword.getString("create_new_filter"), "CreateFiltersOne?action=begin");
                table.setQuery("CreateFiltersOne", new HashMap());
                table.setRows(filterRows);
                table.computeDisplay();

                request.setAttribute("table", table);

                forwardPage(Page.CREATE_FILTER_SCREEN_1);
            } else {
                // else, send to the following page:
                request.setAttribute("events", events);
                forwardPage(Page.CREATE_FILTER_SCREEN_3);
            }
        } else if ("crfselected".equalsIgnoreCase(action)) {
            // get the crf id, return to a new page with sections
            // and parameters attached, tbh
            FormProcessor fp = new FormProcessor(request);
            HashMap errors = new HashMap();
            int crfId = fp.getInt("crfId");
            if (crfId > 0) {
                CRFVersionDAO cvDAO = this.crfVersionDao;
                ICrfDAO cDAO = this.crfDao;
                SectionDAO secDAO = this.sectionDao;
                Collection sections = secDAO.findByVersionId(crfId);
                CRFVersionBean cvBean = (CRFVersionBean) cvDAO.findByPK(crfId);
                CRFBean cBean = (CRFBean) cDAO.findByPK(cvBean.getCrfId());
                request.setAttribute("sections", sections);
                session.setAttribute("cBean", cBean);
                session.setAttribute("cvBean", cvBean);// for further pages,
                // tbh

                forwardPage(Page.CREATE_FILTER_SCREEN_3_1);
            } else {
                addPageMessage(respage.getString("select_a_CRF_before_picking"));
                IStudyEventDAO sedao = this.studyEventDao;
                HashMap events = sedao.findCRFsByStudy(studyWithEventDefs);

                request.setAttribute("events", events);
                forwardPage(Page.CREATE_FILTER_SCREEN_3);
            }
        } else if ("sectionselected".equalsIgnoreCase(action)) {
            // TODO set the crf and the section into session,
            // allow for the user to go back and forth,
            // set up the questions to be picked,
            // allow the user to move on to create_filter_screen_4
            FormProcessor fp = new FormProcessor(request);
            int sectionId = fp.getInt("sectionId");
            if (sectionId > 0) {
                SectionDAO secDAO = this.sectionDao;
                SectionBean secBean = (SectionBean) secDAO.findByPK(sectionId);
                session.setAttribute("secBean", secBean);
                ItemFormMetadataDAO ifmDAO = this.itemFormMetadataDao;
                Collection metadatas = ifmDAO.findAllBySectionId(sectionId);
                if (metadatas.size() > 0) {
                    request.setAttribute("metadatas", metadatas);
                    forwardPage(Page.CREATE_FILTER_SCREEN_3_2);
                } else {
                    CRFVersionBean cvBean = (CRFVersionBean) session.getAttribute("cvBean");
                    addPageMessage(respage.getString("section_not_have_questions_select_another"));
                    // SectionDAO secDAO = this.sectionDao;
                    Collection sections = secDAO.findByVersionId(cvBean.getId());

                    request.setAttribute("sections", sections);

                    forwardPage(Page.CREATE_FILTER_SCREEN_3_1);
                }
            } else {
                CRFVersionBean cvBean = (CRFVersionBean) session.getAttribute("cvBean");
                addPageMessage(respage.getString("select_section_before_select_question"));
                SectionDAO secDAO = this.sectionDao;
                Collection sections = secDAO.findByVersionId(cvBean.getId());

                request.setAttribute("sections", sections);

                forwardPage(Page.CREATE_FILTER_SCREEN_3_1);
            }
        } else if ("questionsselected".equalsIgnoreCase(action)) {
            ArrayList alist = this.extractIdsFromForm();

            // TODO this is where we begin the 'specify criteria' phase
            // of the servlet; we grab the list of questions, get each
            // item form metadata bean, and stick them in a collection
            // and send the user to create_filter_screen_4
            if (alist.size() > 0) {
                ItemFormMetadataDAO ifmDAO = this.itemFormMetadataDao;
                Collection questions = ifmDAO.findByMultiplePKs(alist);
                session.setAttribute("questions", questions);
                forwardPage(Page.CREATE_FILTER_SCREEN_4);
            } else {
                SectionBean secBean = (SectionBean) session.getAttribute("secBean");
                addPageMessage(respage.getString("select_questions_before_set_parameters"));
                ItemFormMetadataDAO ifmDAO = this.itemFormMetadataDao;
                Collection metadatas = ifmDAO.findAllBySectionId(secBean.getId());
                request.setAttribute("metadatas", metadatas);
                forwardPage(Page.CREATE_FILTER_SCREEN_3_2);
            }

        } else if ("validatecriteria".equalsIgnoreCase(action)) {
            // TODO look at the criteria and create a list of filterobjectdata
            // beans, so that we can create the SQL later on in
            // the process.
            // also, throw the user back to the process or throw
            // them forward into the createServletThree process
            FormProcessor fp = new FormProcessor(request);
            String logical = fp.getString("logical");
            ArrayList questions = (ArrayList) session.getAttribute("questions");
            ArrayList filterobjects = new ArrayList();
            // (ArrayList)session.getAttribute("filterobjects");
            Iterator q_it = questions.iterator();
            int arrCnt = 0;
            while (q_it.hasNext()) {
                ItemFormMetadataBean ifmBean = (ItemFormMetadataBean) q_it.next();
                String opString = "operator:" + ifmBean.getId();
                String valString = "value:" + ifmBean.getId();
                String remString = "remove:" + ifmBean.getId();
                if ("remove".equals(fp.getString(remString))) {
                    logger.info("found the string: " + remString);
                    // TODO remove the question from from the list,
                    // redirect to that page again????? <--maybe not?
                    // questions.remove(arrCnt);
                    // shouldn't have to remove the above, just do nothing
                    arrCnt++;
                } else {
                    String operator = fp.getString(opString);
                    String value = fp.getString(valString);
                    FilterObjectBean fob = new FilterObjectBean();
                    fob.setItemId(ifmBean.getId());
                    fob.setItemName(ifmBean.getHeader() + " " + ifmBean.getLeftItemText() + " " + ifmBean.getRightItemText());
                    // case operator:
                    if ("equal to".equalsIgnoreCase(operator)) {
                        fob.setOperand("=");
                    } else if ("greater than".equalsIgnoreCase(operator)) {
                        fob.setOperand(">");
                    } else if ("less than".equalsIgnoreCase(operator)) {
                        fob.setOperand("<");
                    } else if ("greater than or equal".equalsIgnoreCase(operator)) {
                        fob.setOperand(">=");
                    } else if ("less than or equal".equalsIgnoreCase(operator)) {
                        fob.setOperand("<=");
                    } else if ("like".equalsIgnoreCase(operator)) {
                        fob.setOperand(" like ");
                    } else if ("not like".equalsIgnoreCase(operator)) {
                        fob.setOperand(" not like ");
                    } else {
                        fob.setOperand("!=");
                    }
                    fob.setValue(value);
                    filterobjects.add(fob);
                }// end else
                //

            }// end while
            session.setAttribute("questions", questions);
            // TODO where does the connector come into play?
            // session.setAttribute("filterobjects",filterobjects);
            FilterDAO fDAO = this.filterDao;
            String newSQL = (String) session.getAttribute("newSQL");
            ArrayList newExp = (ArrayList) session.getAttribute("newExp");
            // human readable explanation
            String newNewSQL = fDAO.genSQLStatement(newSQL, logical, filterobjects);
            ArrayList newNewExp = fDAO.genExplanation(newExp, logical, filterobjects);
            if (arrCnt == questions.size()) {
                newNewSQL = newSQL;
                newNewExp = newExp;
                // don't change anything, if we've removed everything from this
                // list
            }
            logger.info("new SQL Generated: " + newNewSQL);

            String sub = fp.getString("submit");
            if ("Specify Filter Metadata".equals(sub)) {
                // add new params, create the filter object,
                // and go to create metadata
                FilterBean fb = new FilterBean();

                fb.setSQLStatement(newNewSQL + ")");
                // adding parens here to finish off other
                // statement--might add first part of statement here
                // for legibility's sake
                // tbh 06-02-2005
                session.removeAttribute("newSQL");
                // end of the road
                session.setAttribute("newFilter", fb);
                request.setAttribute("statuses", getStatuses());
                forwardPage(Page.CREATE_FILTER_SCREEN_5);
            } else {
                // replace the 'old' sql with the new sql gathered from the
                // session
                session.setAttribute("newSQL", newNewSQL);
                session.setAttribute("newExp", newNewExp);
                // add new params, and go back
                IStudyEventDAO sedao = this.studyEventDao;
                HashMap events = sedao.findCRFsByStudy(currentStudy);
                //
                request.setAttribute("events", events);
                forwardPage(Page.CREATE_FILTER_SCREEN_3);
            }
        }
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);
        // < restext =
        // ResourceBundle.getBundle("org.researchedc.i18n.notes",locale);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);

        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    public ArrayList extractIdsFromForm() {
        ArrayList retMe = new ArrayList();
        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String title = (String) en.nextElement();
            if (title.startsWith("ID")) {
                String newId = title.replaceAll("ID", "");
                Integer ifmId = new Integer(newId);
                // TODO throw an error here if it's not applicable?
                retMe.add(ifmId);
            }
        }
        return retMe;
    }

    private ArrayList getStatuses() {
        Status statusesArray[] = { Status.AVAILABLE, Status.PENDING, Status.PRIVATE, Status.UNAVAILABLE };
        List statuses = Arrays.asList(statusesArray);
        return new ArrayList(statuses);
    }

}

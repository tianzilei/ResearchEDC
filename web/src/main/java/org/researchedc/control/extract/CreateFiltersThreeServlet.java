/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.extract;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.TermType;
import org.researchedc.bean.extract.FilterBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.form.Validator;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.FilterDao;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.web.bean.EntityBeanTable;
import org.researchedc.web.bean.FilterRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <P>
 * Meant to fill in steps 5 and 6 of the create filter process, that is, to
 * serve as spcifying the metadata and saving the filter, returning to list
 * filters when the process is done.
 *
 * @author thickerson
 *
 */
public class CreateFiltersThreeServlet extends SecureController {

    @Autowired
    protected FilterDao filterDao;

    Locale locale;

    // < ResourceBundlerestext,resword,respage,resexception;

    @Override
    public void processRequest() throws Exception {
        String action = request.getParameter("action");
        if (StringUtil.isBlank(action)) {
            // throw an error

        } else if ("validate".equalsIgnoreCase(action)) {
            FormProcessor fp = new FormProcessor(request);
            Validator v = new Validator(request);

            v.addValidation("fName", Validator.NO_BLANKS);
            v.addValidation("fDesc", Validator.NO_BLANKS);
            v.addValidation("fStatusId", Validator.IS_VALID_TERM, TermType.STATUS);

            errors = v.validate();
            if (!errors.isEmpty()) {
                String fieldNames[] = { "fName", "fDesc" };
                fp.setCurrentStringValuesAsPreset(fieldNames);
                fp.addPresetValue("fStatusId", fp.getInt("fStatusId"));

                addPageMessage(respage.getString("errors_in_submission_see_below"));
                setInputMessages(errors);
                setPresetValues(fp.getPresetValues());

                request.setAttribute("statuses", getStatuses());
                forwardPage(Page.CREATE_FILTER_SCREEN_5);
            } else {
                FilterBean fb = (FilterBean) session.getAttribute("newFilter");
                fb.setName(fp.getString("fName"));
                session.removeAttribute("newFilter");
                session.removeAttribute("newExp");// remove explanation for
                // filter here,
                // tbh
                fb.setDescription(fp.getString("fDesc"));
                fb.setStatus(Status.get(fp.getInt("fStatusId")));

                // above depreciated?
                fb.setOwner(ub);
                // fb.setOwnerId(ub.getId());
                logger.info("found owner id: " + fb.getOwner().getId());
                FilterDao fDAO = this.filterDao;
                FilterBean fbFinal = (FilterBean) fDAO.create(fb);
                addPageMessage(restext.getString("the_filter_named") +
                // fp.getString("fName")+
                    fbFinal.getName() + respage.getString("X_was_created_succesfully"));

                Integer check = (Integer) session.getAttribute("partOfCreateDataset");
                if (check != null) {
                    // move the creation process on to create a dataset
                    request.setAttribute("statuses", getStatuses());
                    session.removeAttribute("partOfCreateDataset");
                    forwardPage(Page.CREATE_DATASET_4);
                } else {
                    session.removeAttribute("newFilter");
                    FilterDao fdao = this.filterDao;
                    EntityBeanTable table = fp.getEntityBeanTable();

                    ArrayList filters = (ArrayList) fdao.findAll();

                    ArrayList filterRows = FilterRow.generateRowsFromBeans(filters);

                    String[] columns =
                        { resword.getString("filter_name"), resword.getString("description"), resword.getString("created_by"),
                            resword.getString("created_date"), resword.getString("status"), resword.getString("actions") };

                    table.setColumns(new ArrayList(Arrays.asList(columns)));
                    table.hideColumnLink(5);
                    table.setQuery("CreateFiltersOne", new HashMap());
                    table.setRows(filterRows);
                    table.computeDisplay();

                    request.setAttribute("table", table);
                    // request.setAttribute("filters",filters);
                    forwardPage(Page.CREATE_FILTER_SCREEN_1);
                }
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

    private ArrayList getStatuses() {
        Status statusesArray[] = { Status.AVAILABLE, Status.PENDING, Status.PRIVATE, Status.UNAVAILABLE };
        List statuses = Arrays.asList(statusesArray);
        return new ArrayList(statuses);
    }
}

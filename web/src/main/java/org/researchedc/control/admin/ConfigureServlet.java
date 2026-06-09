/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2009 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.form.Validator;
import org.researchedc.dao.spi.ConfigurationDao;
import org.researchedc.domain.technicaladmin.ConfigurationBean;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Krikor Krumlian
 */
public class ConfigureServlet extends SecureController {

    private static final long serialVersionUID = 2729725318725545575L;
    private ConfigurationDao configurationDao;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }

        return;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        if (!fp.isSubmitted()) {
            loadPresetValuesFromBean(fp);
            setPresetValues(fp.getPresetValues());
            forwardPage(Page.ADMIN_SYSTEM);
        } else {
            Validator v = new Validator(request);
            v.addValidation("lockcount", Validator.IS_AN_INTEGER);
            v.addValidation("lockcount", Validator.NO_BLANKS);
            v.addValidation("lockcount", Validator.IS_IN_RANGE, 1, 25);

            HashMap errors = v.validate();

            if (!errors.isEmpty()) {
                loadPresetValuesFromForm(fp);

                setPresetValues(fp.getPresetValues());
                setInputMessages(errors);
                forwardPage(Page.ADMIN_SYSTEM);

            } else {

                ConfigurationBean userLockSwitch = getConfigurationDao().findByKey("user.lock.switch");
                ConfigurationBean userLockAllowedFailedConsecutiveLoginAttempts =
                    getConfigurationDao().findByKey("user.lock.allowedFailedConsecutiveLoginAttempts");

                userLockSwitch.setValue(fp.getString("lockswitch"));
                userLockAllowedFailedConsecutiveLoginAttempts.setValue(fp.getString("lockcount"));
                getConfigurationDao().saveOrUpdate(userLockSwitch);
                getConfigurationDao().saveOrUpdate(userLockAllowedFailedConsecutiveLoginAttempts);
                addPageMessage(respage.getString("lockout_changes_have_been_saved"));
                forwardPage(Page.ADMIN_SYSTEM); // was LIST_USER_ACCOUNTS_SERVLET — removed in phase-1-login-profile
            }
        }

    }

    private void loadPresetValuesFromBean(FormProcessor fp) {
        ConfigurationBean userLockSwitch = getConfigurationDao().findByKey("user.lock.switch");
        ConfigurationBean userLockAllowedFailedConsecutiveLoginAttempts = getConfigurationDao().findByKey("user.lock.allowedFailedConsecutiveLoginAttempts");

        fp.addPresetValue("lockswitch", userLockSwitch.getValue());
        fp.addPresetValue("lockcount", userLockAllowedFailedConsecutiveLoginAttempts.getValue());
    }

    private void loadPresetValuesFromForm(FormProcessor fp) {
        fp.clearPresetValues();

        String textFields[] = { "lockswitch", "lockcount" };
        fp.setCurrentStringValuesAsPreset(textFields);
    }

    public ConfigurationDao getConfigurationDao() {
        configurationDao =
            this.configurationDao != null ? configurationDao : (ConfigurationDao) SpringServletAccess.getApplicationContext(context)
                    .getBean("configurationDao");
        return configurationDao;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}

package org.researchedc.ws;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.i18n.util.ResourceBundleProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;

import java.util.Locale;

import javax.sql.DataSource;

public class UserPermissionInterceptor implements EndpointInterceptor {

    private final DataSource dataSource;
    @Autowired
    private UserAccountDAO userAccountDao;

    public UserPermissionInterceptor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        UserAccountBean userAccountBean = ((UserAccountBean) userAccountDao.findByUserName(username));
        Boolean result = userAccountBean.getRunWebservices();
        if (!result) {
            SoapBody response = ((SoapMessage) messageContext.getResponse()).getSoapBody();
            response.addClientOrSenderFault("Authorization is required to execute SOAP web services with this account.Please contact your administrator.",
                    Locale.ENGLISH);
            return false;

        } else {
            return result;
        }
    }

    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
        // no-op
    }

}

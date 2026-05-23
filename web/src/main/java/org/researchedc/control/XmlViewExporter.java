package org.researchedc.control;

import org.jmesa.core.CoreContext;
import org.jmesa.core.CoreContextSupport;
import org.jmesa.view.View;
import org.jmesa.view.ViewExporter;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @since 2.0
 * @author Jeff Johnston
 */
public class XmlViewExporter implements ViewExporter, CoreContextSupport {

    private final HttpServletRequest request;
    private HttpServletResponse response;
    private View view;
    private CoreContext coreContext;

    public XmlViewExporter(View view, CoreContext coreContext, HttpServletRequest request, HttpServletResponse response) {
        this.view = view;
        this.coreContext = coreContext;
        this.request = request;
        this.response = response;
    }

    public XmlViewExporter(View view, CoreContext coreContext, HttpServletRequest request, HttpServletResponse response, String fileName) {
        this.view = view;
        this.coreContext = coreContext;
        this.request = request;
        this.response = response;
    }

    public void export() throws Exception {
        RequestDispatcher dispatcher = request.getRequestDispatcher("DownloadRuleSetXml?ruleSetRuleIds=" + (String) view.render());
        dispatcher.forward(request, response);
    }

    public String getContextType() {
        return "text/plain";
    }

    public String getExtensionName() {
        return "txt";
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public CoreContext getCoreContext() {
        return coreContext;
    }

    @Override
    public void setCoreContext(CoreContext coreContext) {
        this.coreContext = coreContext;
    }
}

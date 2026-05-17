package org.akaza.openclinica.control;

import org.jmesa.core.CoreContext;
import org.jmesa.view.AbstractViewExporter;
import org.jmesa.view.View;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @since 2.0
 * @author Jeff Johnston
 */
public class XmlViewExporter extends AbstractViewExporter {

    private final HttpServletRequest request;

    public XmlViewExporter(View view, CoreContext coreContext, HttpServletRequest request, HttpServletResponse response) {
        super(view, coreContext, response, null);
        this.request = request;
    }

    public XmlViewExporter(View view, CoreContext coreContext, HttpServletRequest request, HttpServletResponse response, String fileName) {
        super(view, coreContext, response, fileName);
        this.request = request;
    }

    public void export() throws Exception {
        //responseHeaders(getResponse());
        //String viewData = (String) getView().render();
        //byte[] contents = (viewData).getBytes();
        //getResponse().getOutputStream().write(contents);
        RequestDispatcher dispatcher = request.getRequestDispatcher("DownloadRuleSetXml?ruleSetRuleIds=" + (String) getView().render());
        dispatcher.forward(request, getResponse());
    }

    @Override
    public String getContextType() {
        return "text/plain";
    }

    @Override
    public String getExtensionName() {
        return "txt";
    }
}

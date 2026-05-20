package org.researchedc.control;

import org.jmesa.core.CoreContext;
import org.jmesa.core.CoreContextSupport;
import org.jmesa.util.ExportUtils;
import org.jmesa.view.View;
import org.jmesa.view.ViewExporter;

import java.io.File;
import java.io.FileOutputStream;

import jakarta.servlet.http.HttpServletResponse;

public class OCCsvViewExporter implements ViewExporter, CoreContextSupport {

    private View view;
    private CoreContext coreContext;
    String fileName;

    public OCCsvViewExporter(View view, CoreContext coreContext, HttpServletResponse response) {
        this.view = view;
        this.coreContext = coreContext;
        if (fileName == null) {
            fileName = ExportUtils.exportFileName(view, getExtensionName());
        }
    }

    public OCCsvViewExporter(View view, CoreContext coreContext, HttpServletResponse response, String fileName) {
        this.view = view;
        this.coreContext = coreContext;
        this.fileName = fileName + "." + getExtensionName();
    }

    public void export() throws Exception {
        String viewData = (String) view.render();
        byte[] contents = viewData.getBytes();
        File f = new File(fileName);
        FileOutputStream fos = new FileOutputStream(f, true);
        fos.write(contents);
        fos.flush();
    }

    public String getContextType() {
        return "text/csv";
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

package org.researchedc.web.table.sdv;

import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The drop list for the CRF Status filter.
 */
public class CrfStatusFilter extends DroplistFilterEditor {
    @Override
    protected List<Option> getOptions() {
        List<Option> options = new ArrayList<Option>();
        //options.add(new Option("1", "Scheduled"));
        //options.add(new Option("2", "Not scheduled"));
        //options.add(new Option("3", "Data entry started"));
        options.add(new Option("Completed", "Completed"));
        //options.add(new Option("5", "Stopped"));
        //options.add(new Option("6", "Skipped"));
        options.add(new Option("Locked", "Locked"));
        //options.add(new Option("8", "Signed"));
        return options;
    }
}

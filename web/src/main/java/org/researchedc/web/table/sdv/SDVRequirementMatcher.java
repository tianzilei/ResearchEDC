package org.researchedc.web.table.sdv;

import org.jmesa.core.filter.FilterMatcher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: May 19, 2009
 */
public class SDVRequirementMatcher implements FilterMatcher {
    public boolean evaluate(Object itemValue, String filterValue) {

        String item = String.valueOf(itemValue);
        String filter = String.valueOf(filterValue);

        return filter.contains(item);
    }
}

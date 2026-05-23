/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 *
 * Created on Sep 21, 2005
 */
package org.researchedc.control.form;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A Validator for 'calculation' and 'group-calculation' type Items whose
 * fieldNames are always from request attribute.
 *
 * @author ywang (Feb. 2008)
 *
 */
public class ScoreItemValidator extends DiscrepancyValidator {
    private FormDiscrepancyNotes notes;

    public ScoreItemValidator(HttpServletRequest request, FormDiscrepancyNotes notes) {
        // super(request);
        super(request, notes);
        this.notes = notes;
    }

    @Override
    protected String getFieldValue(String fieldName) {
        return (String) request.getAttribute(fieldName);
    }

}

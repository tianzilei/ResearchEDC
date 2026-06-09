/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2008-2009 Akaza Research
 */
package org.researchedc.control.managestudy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Builds on top of ViewSectionDataEntryServlet, Doesn't add much other than using OIDs to get to the View Screen.
 * 
 * @author Krikor Krumlian
 */
public class ViewSectionDataEntryByIdServlet extends ViewSectionDataEntryServlet {

    @Autowired
    protected IStudyDAO studyDao;

    
    @Autowired
    private ICrfVersionDAO crfVersionDao;

private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * @see org.researchedc.control.managestudy.ViewSectionDataEntryServlet#mayProceed()
     */
    @Override
    public void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {
        return;
    }

    /*
     * (non-Javadoc)
     * @see org.researchedc.control.managestudy.ViewSectionDataEntryServlet#processRequest()
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IStudyDAO studyDao = this.studyDao;
       
        StudyBean  currentStudy = (StudyBean) studyDao.findByPK(1);
        ICrfVersionDAO crfVersionDao = this.crfVersionDao;
        if (request.getParameter("id") == null) {
            response.sendRedirect(request.getContextPath() + "/app/login");
            return;
        }
        request.setAttribute("study", currentStudy);
        CRFVersionBean crfVersion = crfVersionDao.findByOid(request.getParameter("id"));
        if (crfVersion != null) {
            request.setAttribute("crfVersionId", String.valueOf(crfVersion.getId()));
            request.setAttribute("crfId", String.valueOf(crfVersion.getCrfId()));
            super.processRequest(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/app/login");
            return;
        }
    }
}

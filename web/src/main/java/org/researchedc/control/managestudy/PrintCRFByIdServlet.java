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
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * Builds on top of PrintCRFServlet
 * 
 * @author Krikor Krumlian
 */
public class PrintCRFByIdServlet extends PrintCRFServlet {

    @Autowired
    protected IStudyDAO studyDao;

    
    @Autowired
    private CRFVersionDAO crfVersionDao;

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
        
        StudyBean currentStudy =    (StudyBean) request.getSession().getAttribute("study");
        IStudyDAO studyDao = this.studyDao;
        currentStudy = (StudyBean) studyDao.findByPK(1);
        CRFVersionDAO crfVersionDao = this.crfVersionDao;
        if (request.getParameter("id") == null) {
            forwardPage(Page.LOGIN, request, response);
        }
        CRFVersionBean crfVersion = crfVersionDao.findByOid(request.getParameter("id"));
        request.setAttribute("study", currentStudy);
        if (crfVersion != null) {
            request.setAttribute("id", String.valueOf(crfVersion.getId()));
            super.processRequest(request, response);
        } else {
            forwardPage(Page.LOGIN, request, response);
        }
    }
}

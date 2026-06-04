/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.submit;

import org.researchedc.bean.core.Utils;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ywang (Dec., 2008)
 */
public class DownloadAttachedFileServlet extends SecureController {

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        Locale locale = LocaleResolver.getLocale(request);
        FormProcessor fp = new FormProcessor(request);
/*        int eventCRFId = fp.getInt("eventCRFId");
        EventCRFDao edao = this.eventCrfDao;

        if (eventCRFId > 0) {
            if (!entityIncluded(eventCRFId, ub.getName(), edao, sm.getDataSource())) {
                request.setAttribute("downloadStatus", "false");
                addPageMessage(respage.getString("you_not_have_permission_download_attached_file"));
                throw new InsufficientPermissionException(Page.DOWNLOAD_ATTACHED_FILE, resexception.getString("no_permission"), "1");
            }
        } else {
            request.setAttribute("downloadStatus", "false");
            addPageMessage(respage.getString("you_not_have_permission_download_attached_file"));
            throw new InsufficientPermissionException(Page.DOWNLOAD_ATTACHED_FILE, resexception.getString("no_permission"), "1");
        }*/

        if (ub.isSysAdmin()) {
            return;
        }
        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        request.setAttribute("downloadStatus", "false");
        addPageMessage(respage.getString("you_not_have_permission_download_attached_file"));
        throw new InsufficientPermissionException(Page.DOWNLOAD_ATTACHED_FILE, resexception.getString("no_permission"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String filePathName = "";
        String fileName = fp.getString("fileName");
        File f = new File(fileName);
              
        if (fileName != null && fileName.length() > 0) {
            int parentStudyId = currentStudy.getParentStudyId();           
            String testPath = Utils.getAttachedFileRootPath();
            String tail = File.separator + f.getName();
            String testName = testPath + currentStudy.getOid() + tail;
            
            String filePath = testPath + currentStudy.getOid() +File.separator;            
            File temp = new File(filePath,f.getName());            
            String canonicalPath= temp.getCanonicalPath();
            
            if (canonicalPath.startsWith(filePath)) {
            	;
            }else {
            	throw new RuntimeException("Traversal attempt - file path not allowed " + fileName);
            }
            
            if (temp.exists()) {
                filePathName = testName;
                logger.info(currentStudy.getName() + " existing filePathName=" + filePathName);
            } else {
                if (currentStudy.isSite(parentStudyId)) {
                    testName = testPath + ((StudyBean) this.studyDao.findByPK(parentStudyId)).getOid() + tail;
                    temp = new File(testName);
                    if (temp.exists()) {
                        filePathName = testName;
                        logger.info("parent existing filePathName=" + filePathName);
                    }
                } else {
                    ArrayList<StudyBean> sites = (ArrayList<StudyBean>) this.studyDao.findAllByParent(currentStudy.getId());
                    for (StudyBean s : sites) {
                        testPath = Utils.getAttachedFilePath(s);
                        testName = testPath + tail;//+ s.getIdentifier() + tail;
                        File test = new File(testName);
                        if (test.exists()) {
                            filePathName = testName;
                            logger.info("site of currentStudy existing filePathName=" + filePathName);
                            break;
                        }
                    }
                }
            }
        }
        logger.info("filePathName=" + filePathName + " fileName=" + fileName);
        File file = null;
        if(filePathName != null && filePathName.trim().length() >0) {
        	file = new File(filePathName);
        }else {
        	file = new File(fileName);
        }
        
        if (file != null && file.exists()) {           
            /*
             *  try to use the passed in the existing file
             *  OC-17868 remove any possible path traversal, will make sure only download files from defined download folder            
             */                 	                    
            String canonicalPath= file.getCanonicalPath();            
            String definedDownloadPath = Utils.getAttachedFileRootPath();
            
            if(!(canonicalPath.startsWith(definedDownloadPath))) {
            	throw new RuntimeException("Traversal attempt - file path not allowed " + fileName);
            }
        	
        }
        
        if (!file.exists() || file.length() <= 0) {
            addPageMessage("File " + filePathName + " " + respage.getString("not_exist"));
        } else {
//            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\";");
            response.setHeader("Pragma", "public");

            ServletOutputStream outStream = response.getOutputStream();
            DataInputStream inStream = null;
            try {
                response.setContentType("application/download");
                response.setHeader("Cache-Control", "max-age=0");
                response.setContentLength((int) file.length());

                byte[] bbuf = new byte[(int) file.length()];
                inStream = new DataInputStream(new FileInputStream(file));
                int length;
                while (inStream != null && (length = inStream.read(bbuf)) != -1) {
                    outStream.write(bbuf, 0, length);
                }

                inStream.close();
                outStream.flush();
                outStream.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
            }
        }
    }

}
/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.extract;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.submit.SubmitDataServlet;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.dao.submit.ItemGroupMetadataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jxu
 *
 * View all related metadata for an item
 */
public class ViewItemDetailServlet extends SecureController {

    @Autowired
    protected SectionDAO sectionDao;

    @Autowired
    protected ItemFormMetadataDAO itemFormMetadataDao;

    @Autowired
    protected ItemGroupMetadataDAO itemGroupMetadataDao;

    @Autowired
    protected CRFVersionDAO crfVersionDao;

    Locale locale;
    // < ResourceBundle respage;

    public static String ITEM_ID = "itemId";
    public static String ITEM_OID = "itemOid";
    public static String ITEM_BEAN = "item";
    public static String VERSION_ITEMS = "versionItems";

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);

        if (currentStudy.getParentStudyId() == 0 && SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int itemId = fp.getInt(ITEM_ID);
        String itemOid = fp.getString(ITEM_OID);
        ItemDAO idao = this.itemDao;
        ItemFormMetadataDAO ifmdao = this.itemFormMetadataDao;
        ItemGroupMetadataDAO igmdao = this.itemGroupMetadataDao;
        CRFVersionDAO cvdao = this.crfVersionDao;
        ICrfDAO cdao = this.crfDao;
        SectionDAO sectionDao = this.sectionDao;

        if (itemId == 0 && itemOid == null) {
            addPageMessage(respage.getString("please_choose_an_item_first"));
            forwardPage(Page.ITEM_DETAIL);
            return;
        }
        ItemBean item = itemId > 0 ? (ItemBean) idao.findByPK(itemId) : (ItemBean) idao.findByOid(itemOid).get(0);
        ArrayList versions = idao.findAllVersionsByItemId(item.getId());
        ArrayList versionItems = new ArrayList();
        CRFBean crf = null;
        ItemFormMetadataBean imfBean = null;
        // finds each item metadata for each version
        for (int i = 0; i < versions.size(); i++) {
            Integer versionId = (Integer) versions.get(i);
            CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(versionId.intValue());
            if (versionId != null && versionId.intValue() > 0) {
                // YW 08-22-2007
                if (igmdao.versionIncluded(versionId)) {
                    imfBean = ifmdao.findByItemIdAndCRFVersionId(item.getId(), versionId.intValue());
                    imfBean.setCrfVersionName(version.getName());
                    crf = (CRFBean) cdao.findByPK(version.getCrfId());
                    imfBean.setCrfName(crf.getName());
                    versionItems.add(imfBean);
                } else {
                    imfBean = ifmdao.findByItemIdAndCRFVersionIdNotInIGM(item.getId(), versionId.intValue());
                    imfBean.setCrfVersionName(version.getName());
                    crf = (CRFBean) cdao.findByPK(version.getCrfId());
                    imfBean.setCrfName(crf.getName());
                    versionItems.add(imfBean);
                }
            }

        }

        SectionBean section = (SectionBean) sectionDao.findByPK(imfBean.getSectionId());
        request.setAttribute(VERSION_ITEMS, versionItems);
        request.setAttribute(ITEM_BEAN, item);
        request.setAttribute("crf", crf);
        request.setAttribute("section", section);
        request.setAttribute("ifmdBean", imfBean);
        forwardPage(Page.ITEM_DETAIL);

    }

}

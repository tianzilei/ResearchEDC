/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.ResolutionStatus;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.DisplayEventCRFBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ItemGroupMetadataBean;
import org.researchedc.bean.submit.ResponseSetBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.hibernate.DynamicsItemFormMetadataDao;
import org.researchedc.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.researchedc.dao.hibernate.RuleActionRunLogDao;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.domain.datamap.DnItemDataMap;
import org.researchedc.domain.datamap.DnItemDataMapId;
import org.researchedc.domain.rule.action.RuleActionRunLogBean;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;

/**
 * @author jxu
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class DeleteEventCRFServlet extends SecureController {
    @Autowired
    IStudyEventDAO studyEventDao;
    @Autowired
    IStudySubjectDAO studySubjectDao;
    @Autowired
    EventCRFDao eventCrfDao;
    @Autowired
    IStudyDAO studyDao;
    @Autowired
    ICrfDAO crfDao;
    @Autowired
    IStudyEventDefinitionDAO studyEventDefinitionDao;
    @Autowired
    IItemDataDAO itemDataDao;

    @Autowired
    private ICrfVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;
	public static String STUDY_SUB_ID = "ssId";

	public static String EVENT_CRF_ID = "ecId";
	@Autowired
	private IDiscrepancyNoteDAO dnDao;
	RuleActionRunLogDao ruleActionRunLogDao;
	DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
	DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
	ItemFormMetadataDAO ifmdao;
	IItemDataDAO iddao;
    IStudyEventDAO sedao;
	/**
     * 
     */
	@Override
	public void mayProceed() throws InsufficientPermissionException {
		if (ub.isSysAdmin()) {
			return;
		}
		addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
		throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS, resexception.getString("not_admin"), "1");

	}

	@Override
	public void processRequest() throws Exception {
		FormProcessor fp = new FormProcessor(request);
		int studySubId = fp.getInt(STUDY_SUB_ID, true);
		int eventCRFId = fp.getInt(EVENT_CRF_ID);

		String action = request.getParameter("action");

		IStudyEventDAO sedao = this.studyEventDao;
		IStudySubjectDAO subdao = this.studySubjectDao;
		EventCRFDao ecdao = this.eventCrfDao;
		IStudyDAO sdao = this.studyDao;

		if (eventCRFId == 0) {
			addPageMessage(respage.getString("please_choose_an_event_CRF_to_delete"));
			request.setAttribute("id", Integer.valueOf(studySubId).toString());
			forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
		} else {
			EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);

			StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
			request.setAttribute("studySub", studySub);

			// construct info needed on view event crf page
			ICrfDAO cdao = this.crfDao;
			ICrfVersionDAO cvdao = this.crfVersionDao;

			int crfVersionId = eventCRF.getCRFVersionId();
			CRFBean cb = cdao.findByVersionId(crfVersionId);
			eventCRF.setCrf(cb);

			CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
			eventCRF.setCrfVersion(cvb);

			// then get the definition so we can call
			// DisplayEventCRFBean.setFlags
			int studyEventId = eventCRF.getStudyEventId();

			StudyEventBean event = (StudyEventBean) sedao.findByPK(studyEventId);

			int studyEventDefinitionId = sedao.getDefinitionIdFromStudyEventId(studyEventId);
			IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;
			StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEventDefinitionId);
			event.setStudyEventDefinition(sed);
			request.setAttribute("event", event);

			EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;

			StudyBean study = (StudyBean) sdao.findByPK(studySub.getStudyId());
			EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());

			DisplayEventCRFBean dec = new DisplayEventCRFBean();
			dec.setEventCRF(eventCRF);
			dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());

			// find all item data
			IItemDataDAO iddao = this.itemDataDao;
			// dnDao is now @Autowired
			ArrayList<ItemDataBean> itemData = iddao.findAllByEventCRFId(eventCRF.getId());
			request.setAttribute("items", itemData);

			if ("confirm".equalsIgnoreCase(action)) {

				request.setAttribute("displayEventCRF", dec);

				forwardPage(Page.DELETE_EVENT_CRF);
			} else {
				logger.info("submit to delete the event CRF from event");

				// OC-6303  Deleting Event CRF resets Show / Hide logic
				// delete records from DynamicItemForm and DynamicItemGroup
				getDynamicsItemFormMetadataDao().delete(eventCRFId);
		        getDynamicsItemGroupMetadataDao().delete(eventCRFId);

				eventCRF.setOldStatus(eventCRF.getStatus());
				eventCRF.setStatus(Status.RESET);
				eventCRF.setUpdater(ub);
				eventCRF.setValidatorId(0);//OC-12177 added to fix displaying DDE workflow status
				ecdao.update(eventCRF);

				for (ItemDataBean itemdata : itemData) {
					// OC-6343 Rule behaviour must be reset if an Event CRF is deleted
					// delete the records from ruleActionRunLogDao
					getRuleActionRunLogDao().delete(itemdata.getId());

					
					// OC-6344 Notes & Discrepancies must be set to "closed" when event CRF is deleted
					// parentDiscrepancyNoteList is the list of the parent DNs records only
					ArrayList<DiscrepancyNoteBean> parentDiscrepancyNoteList = getDnDao().findParentNotesOnlyByItemData(itemdata.getId());
					for (DiscrepancyNoteBean parentDiscrepancyNote : parentDiscrepancyNoteList) {
						if (parentDiscrepancyNote.getResolutionStatusId() != 4) { // if the DN's resolution status is not set to Closed
							String description = resword.getString("dn_auto-closed_description");
							String detailedNotes =resword.getString("dn_auto_closed_detailed_notes");
							// create new DN record , new DN Map record , also update the parent record
							createDiscrepancyNoteBean(description, detailedNotes, itemdata.getId(), study, ub, parentDiscrepancyNote);
						}
					}
					iddao = this.itemDataDao;
					// ifmdao is now @Autowired
					ItemDataBean idBean = (ItemDataBean) iddao.findByPK(itemdata.getId());

					ItemFormMetadataBean ifmBean = ifmdao.findByItemIdAndCRFVersionId(idBean.getItemId(), crfVersionId);

					// Updating Dn_item_data_map actovated column into false for the existing DNs
					ArrayList<DiscrepancyNoteBean> dnBeans = getDnDao().findExistingNotesForItemData(itemdata.getId());
					if (dnBeans.size() != 0) {
						DiscrepancyNoteBean dnBean = new DiscrepancyNoteBean();
						dnBean.setEntityId(itemdata.getId());
						dnBean.setActivated(false);
						getDnDao().updateDnMapActivation(dnBean);
					}

					// Default Values are not addressed

					
					itemdata.setValue("");
					itemdata.setOldStatus(itemdata.getStatus());
					itemdata.setOwner(ub);
					itemdata.setStatus(Status.AVAILABLE);
					itemdata.setUpdater(ub);
					iddao.updateUser(itemdata);
					iddao.update(itemdata);					
					
				}
				// OC-6291 event_crf status change
	
				eventCRF.setOldStatus(eventCRF.getStatus());
				eventCRF.setStatus(Status.AVAILABLE);
				eventCRF.setUpdater(ub);
				ecdao.update(eventCRF);
								
				if(event.getSubjectEventStatus().isCompleted() || event.getSubjectEventStatus().isSigned()){
					event.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
					event.setUpdater(ub);
                   sedao = this.studyEventDao;
                   sedao.update(event);
				}
				
				String emailBody = respage.getString("the_event_CRF") + cb.getName() + respage.getString("has_been_deleted_from_the_event") + event.getStudyEventDefinition().getName() + ". " + respage.getString("has_been_deleted_from_the_event_cont");

				addPageMessage(emailBody);
				// sendEmail(emailBody);
				request.setAttribute("id", Integer.valueOf(studySubId).toString());
				forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
			}

		}
	}

	private void createDiscrepancyNoteBean(String description, String detailedNotes, int itemDataId, StudyBean studyBean, UserAccountBean ub, DiscrepancyNoteBean parentDiscrepancyNote) {
		DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
		dnb.setEntityId(itemDataId); // this is needed for DN Map object
		dnb.setStudyId(studyBean.getId());
		dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
		dnb.setDescription(description);
		dnb.setDetailedNotes(detailedNotes);
		dnb.setDiscrepancyNoteTypeId(parentDiscrepancyNote.getDiscrepancyNoteTypeId()); // set to parent DN Type Id
		dnb.setResolutionStatusId(4); // set to closed
		dnb.setColumn("value"); // this is needed for DN Map object
		dnb.setAssignedUserId(ub.getId());
		dnb.setOwner(ub);
		dnb.setParentDnId(parentDiscrepancyNote.getId());
		dnb.setActivated(false);
		dnb = (DiscrepancyNoteBean) getDnDao().create(dnb); // create child DN
		getDnDao().createMapping(dnb); // create DN mapping

		DiscrepancyNoteBean itemParentNote = (DiscrepancyNoteBean) getDnDao().findByPK(dnb.getParentDnId());
		itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
		itemParentNote.setAssignedUserId(ub.getId());
		getDnDao().update(itemParentNote); // update parent DN
		getDnDao().updateAssignedUser(itemParentNote); // update parent DN assigned user

	}

	public IDiscrepancyNoteDAO getDnDao() {
		return dnDao;
	}

	public void setDnDao(IDiscrepancyNoteDAO dnDao) {
		this.dnDao = dnDao;
	}
	
	
	private RuleActionRunLogDao getRuleActionRunLogDao() {
		ruleActionRunLogDao = this.ruleActionRunLogDao != null ? ruleActionRunLogDao : (RuleActionRunLogDao) SpringServletAccess.getApplicationContext(context).getBean("ruleActionRunLogDao");
		return ruleActionRunLogDao;
	}
	private DynamicsItemFormMetadataDao getDynamicsItemFormMetadataDao() {
		dynamicsItemFormMetadataDao = this.dynamicsItemFormMetadataDao != null ? dynamicsItemFormMetadataDao : (DynamicsItemFormMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("dynamicsItemFormMetadataDao");
		return dynamicsItemFormMetadataDao;
	}
	private DynamicsItemGroupMetadataDao getDynamicsItemGroupMetadataDao() {
		dynamicsItemGroupMetadataDao = this.dynamicsItemGroupMetadataDao != null ? dynamicsItemGroupMetadataDao : (DynamicsItemGroupMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("dynamicsItemGroupMetadataDao");
		return dynamicsItemGroupMetadataDao;
	}

	
}

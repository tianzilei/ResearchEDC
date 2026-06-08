/*
 * Minimal stub for AddNewSubjectServlet — retains only constants and saveFieldNotes() still referenced by 15+ active servlets.
 * Full implementation deleted in Phase 1 study/subject/event slice.
 */
package org.researchedc.control.submit;

import java.util.ArrayList;

import org.researchedc.bean.core.DiscrepancyNoteType;
import org.researchedc.bean.core.ResolutionStatus;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.control.form.FormDiscrepancyNotes;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;


public class AddNewSubjectServlet {

    public static final String INPUT_UNIQUE_IDENTIFIER = "uniqueIdentifier";
    public static final String INPUT_DOB = "dob";
    public static final String INPUT_YOB = "yob";
    public static final String INPUT_GENDER = "gender";
    public static final String INPUT_LABEL = "label";
    public static final String INPUT_SECONDARY_LABEL = "secondaryLabel";
    public static final String INPUT_ENROLLMENT_DATE = "enrollmentDate";
    public static final String INPUT_EVENT_START_DATE = "startDate";
    public static final String INPUT_GROUP = "group";
    public static final String FORM_DISCREPANCY_NOTES_NAME = "fdnotes";
    public static final String BEAN_GROUPS = "groups";

    private AddNewSubjectServlet() {
    }

    public static void saveFieldNotes(String field, FormDiscrepancyNotes notes, IDiscrepancyNoteDAO dndao, int entityId, String entityType, StudyBean sb) {
        saveFieldNotes(field, notes, dndao, entityId, entityType, sb, -1);
    }

    public static void saveFieldNotes(String field, FormDiscrepancyNotes notes, IDiscrepancyNoteDAO dndao, int entityId, String entityType, StudyBean sb, int event_crf_id) {
        if (notes == null || dndao == null || sb == null) {
            return;
        }
        ArrayList fieldNotes = notes.getNotes(field);
        if (fieldNotes == null || fieldNotes.size() < 1) {
            return;
        }
        for (int i = 0; i < fieldNotes.size(); i++) {
            DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) fieldNotes.get(i);
            dnb.setEntityId(entityId);
            dnb.setStudyId(sb.getId());
            dnb.setEntityType(entityType);
            if (event_crf_id > 0) {
                dnb.setEventCRFId(event_crf_id);
            }
            dnb.setStatus(Status.AVAILABLE);
            dnb.setAssignedUserId(0);
            if (dnb.getResolutionStatusId() == 0) {
                dnb.setResStatus(ResolutionStatus.NOT_APPLICABLE);
                dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
                if (!dnb.getDisType().equals(DiscrepancyNoteType.REASON_FOR_CHANGE)) {
                    dnb.setResStatus(ResolutionStatus.OPEN);
                    dnb.setResolutionStatusId(ResolutionStatus.OPEN.getId());
                }
            }
            dndao.create(dnb);
        }
    }
}

/*
 * Minimal stub for ViewNotesServlet — retains only constants still referenced by active servlets.
 * Full implementation deleted in Phase 1 study/subject/event slice.
 */
package org.researchedc.control.managestudy;

/**
 * Stub retaining session attribute keys used by DataEntryServlet, AdministrativeEditingServlet,
 * ViewSectionDataEntryServlet, and ViewSectionDataEntryRESTUrlServlet.
 */
public class ViewNotesServlet {

    /** Session attribute key for the notes window return URL. */
    public static final String WIN_LOCATION = "window_location";

    /** Session attribute key for the notes table state. */
    public static final String NOTES_TABLE = "notesTable";

    private ViewNotesServlet() {
        // Utility stub — not instantiable
    }
}

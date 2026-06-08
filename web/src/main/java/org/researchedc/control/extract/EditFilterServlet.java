/*
 * Minimal stub for EditFilterServlet — retains only getLink() still referenced by FilterTable.
 * Full implementation deleted in Phase 1 export/dataset/filter slice.
 */
package org.researchedc.control.extract;

/**
 * Stub retaining the link-generation helper used by FilterTable.
 */
public class EditFilterServlet {

    private EditFilterServlet() {
        // Utility stub — not instantiable
    }

    /**
     * Generates the edit-filter link for the given filter ID.
     *
     * @param filterId the filter ID
     * @return a relative URL string for the edit-filter action
     */
    public static String getLink(int filterId) {
        return "EditFilter?filterId=" + filterId;
    }
}

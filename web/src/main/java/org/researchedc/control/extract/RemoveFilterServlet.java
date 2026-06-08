/*
 * Minimal stub for RemoveFilterServlet — retains only getLink() still referenced by FilterTable.
 * Full implementation deleted in Phase 1 export/dataset/filter slice.
 */
package org.researchedc.control.extract;

/**
 * Stub retaining the link-generation helper used by FilterTable.
 */
public class RemoveFilterServlet {

    private static final String PATH = "RemoveFilter";
    private static final String ARG_FILTER_ID = "filterId";

    private RemoveFilterServlet() {
        // Utility stub — not instantiable
    }

    /**
     * Generates the remove-filter link for the given filter ID.
     *
     * @param filterId the filter ID
     * @return a relative URL string for the remove-filter action
     */
    public static String getLink(int filterId) {
        return PATH + '?' + ARG_FILTER_ID + '=' + filterId;
    }
}

package org.researchedc.module.study.dto;

import java.util.Map;

/**
 * DTO for study-level feature flags.
 *
 * <p>Each flag controls whether a specific legacy JSP page area is
 * replaced by the React SPA for this study. Flags are stored as a
 * JSONB column on the {@code study} table.
 *
 * <p>Example payload:
 * <pre>{@code
 * {
 *   "use_new_subject_ui": true,
 *   "use_new_event_ui": false,
 *   "use_new_data_entry_ui": false
 * }
 * }</pre>
 */
public class FeatureFlagsDTO {

    private Map<String, Boolean> flags;

    public FeatureFlagsDTO() {
    }

    public FeatureFlagsDTO(Map<String, Boolean> flags) {
        this.flags = flags;
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Boolean> flags) {
        this.flags = flags;
    }
}

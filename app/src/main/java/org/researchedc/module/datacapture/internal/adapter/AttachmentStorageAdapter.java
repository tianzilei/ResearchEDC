package org.researchedc.module.datacapture.internal.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.researchedc.module.datacapture.internal.support.AttachmentStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AttachmentStorageAdapter {

    private static final Logger log = LoggerFactory.getLogger(AttachmentStorageAdapter.class);
    private static final int STATUS_AVAILABLE = 1;
    private static final Set<String> VALID_STUDY_ROLE_NAMES = Set.of(
            "admin",
            "coordinator",
            "director",
            "Investigator",
            "ra",
            "monitor",
            "ra2",
            "Data Specialist");

    private final JdbcTemplate jdbc;
    private final AttachmentStorageProperties storageProperties;

    public AttachmentStorageAdapter(JdbcTemplate jdbc, AttachmentStorageProperties storageProperties) {
        this.jdbc = jdbc;
        this.storageProperties = storageProperties;
    }

    public String getStudyOidByEventCrf(int eventCrfId) {
        try {
            return jdbc.query("""
                    SELECT st.oc_oid
                    FROM module_event_crf ecrf
                    JOIN module_study_subject ss ON ss.study_subject_id = ecrf.study_subject_id
                    JOIN module_study st ON st.study_id = ss.study_id
                    WHERE ecrf.event_crf_id = ?
                    """, rs -> rs.next() ? rs.getString("oc_oid") : null, eventCrfId);
        } catch (Exception e) {
            log.warn("Failed to resolve study OID for eventCrfId={}: {}", eventCrfId, e.getMessage());
            return null;
        }
    }


    public boolean canViewEventCrfData(int eventCrfId, Integer userId) {
        if (userId == null) {
            return false;
        }
        try {
            AttachmentUser user = findUser(userId);
            if (user == null) {
                return false;
            }
            if (user.isAdmin()) {
                return true;
            }

            AttachmentStudy study = findStudyByEventCrf(eventCrfId);
            if (study == null) {
                return false;
            }

            AttachmentRole directRole = roleByStudy(user.userName(), study.id());
            if (directRole.canAccess()) {
                return true;
            }

            if (study.parentStudyId() > 0) {
                AttachmentRole parentRole = roleByStudy(user.userName(), study.parentStudyId());
                return parentRole.canAccess();
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to authorize attachment access: eventCrfId={}, userId={}, error={}",
                    eventCrfId, userId, e.getMessage());
            return false;
        }
    }

    private AttachmentStudy findStudyByEventCrf(int eventCrfId) {
        return jdbc.query("""
                SELECT st.study_id, COALESCE(st.parent_study_id, 0) AS parent_study_id
                FROM module_event_crf ecrf
                JOIN module_study_subject ss ON ss.study_subject_id = ecrf.study_subject_id
                JOIN module_study st ON st.study_id = ss.study_id
                WHERE ecrf.event_crf_id = ?
                """, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new AttachmentStudy(rs.getInt("study_id"), rs.getInt("parent_study_id"));
        }, eventCrfId);
    }

    private AttachmentUser findUser(int userId) {
        String sql = """
                SELECT user_id, user_name, user_type_id
                FROM module_user_account
                WHERE user_id = ?
                """;
        return jdbc.query(sql, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new AttachmentUser(
                    rs.getInt("user_id"),
                    rs.getString("user_name"),
                    rs.getInt("user_type_id"));
        }, userId);
    }

    private AttachmentRole roleByStudy(String userName, int studyId) {
        String sql = """
                SELECT study_user_role_id, user_name, role_name, study_id, status_id
                FROM module_study_user_role
                WHERE user_name = ? AND study_id = ?
                ORDER BY study_user_role_id
                LIMIT 1
                """;
        return jdbc.query(sql, rs -> {
            if (!rs.next()) {
                return AttachmentRole.none();
            }
            return new AttachmentRole(
                    rs.getInt("study_user_role_id"),
                    rs.getString("role_name"),
                    rs.getInt("study_id"),
                    rs.getInt("status_id"));
        }, userName, studyId);
    }

    public List<String> getCandidateStudyOids(String studyOid) {
        if (studyOid == null || studyOid.isBlank()) {
            return List.of();
        }

        List<String> oids = new ArrayList<>();
        oids.add(studyOid);

        AttachmentStudyWithOid study = findStudyByOid(studyOid);
        if (study == null) {
            return oids;
        }

        if (study.parentStudyId() > 0) {
            AttachmentStudyWithOid parent = findStudyById(study.parentStudyId());
            if (parent != null && parent.oid() != null && !oids.contains(parent.oid())) {
                oids.add(parent.oid());
            }
        }

        for (String childOid : findChildStudyOids(study.id())) {
            if (childOid != null && !oids.contains(childOid)) {
                oids.add(childOid);
            }
        }

        return oids;
    }

    private AttachmentStudyWithOid findStudyByOid(String studyOid) {
        return jdbc.query("""
                SELECT study_id, COALESCE(parent_study_id, 0) AS parent_study_id, oc_oid
                FROM module_study
                WHERE oc_oid = ?
                """, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new AttachmentStudyWithOid(
                    rs.getInt("study_id"),
                    rs.getInt("parent_study_id"),
                    rs.getString("oc_oid"));
        }, studyOid);
    }

    private AttachmentStudyWithOid findStudyById(int studyId) {
        return jdbc.query("""
                SELECT study_id, COALESCE(parent_study_id, 0) AS parent_study_id, oc_oid
                FROM module_study
                WHERE study_id = ?
                """, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new AttachmentStudyWithOid(
                    rs.getInt("study_id"),
                    rs.getInt("parent_study_id"),
                    rs.getString("oc_oid"));
        }, studyId);
    }

    private List<String> findChildStudyOids(int parentStudyId) {
        return jdbc.queryForList("""
                SELECT oc_oid
                FROM module_study
                WHERE parent_study_id = ?
                ORDER BY study_id
                """, String.class, parentStudyId);
    }

    private record AttachmentUser(int id, String userName, int userTypeId) {
        boolean isAdmin() {
            return userTypeId == 1 || userTypeId == 3;
        }
    }

    private record AttachmentRole(int id, String roleName, int studyId, int statusId) {
        static AttachmentRole none() {
            return new AttachmentRole(0, null, 0, 0);
        }

        boolean canAccess() {
            return id > 0 && studyId > 0 && statusId == STATUS_AVAILABLE && VALID_STUDY_ROLE_NAMES.contains(roleName);
        }
    }

    private record AttachmentStudy(int id, int parentStudyId) {
    }

    private record AttachmentStudyWithOid(int id, int parentStudyId, String oid) {
    }

    public File resolveAttachmentFile(String fileName, String studyOid) {
        if (fileName == null || fileName.isEmpty() || studyOid == null || studyOid.isEmpty()) {
            return new File("");
        }
        String safeName = new File(fileName).getName();
        String rootPath = storageProperties.attachedFileRootPath();
        File resolved = new File(rootPath, studyOid + File.separator + safeName);
        try {
            String canonical = resolved.getCanonicalPath();
            String expectedPrefix = new File(rootPath).getCanonicalPath();
            if (!canonical.startsWith(expectedPrefix)) {
                log.warn("Path traversal attempt blocked: {} (study={})", fileName, studyOid);
                return new File("");
            }
        } catch (IOException e) {
            log.warn("Failed to resolve canonical path: {} (study={})", fileName, studyOid);
            return new File("");
        }
        return resolved;
    }

    public File studyDirectory(String studyOid) {
        return new File(storageProperties.attachedFileRootPath(), studyOid);
    }
}

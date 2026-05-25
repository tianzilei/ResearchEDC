package org.researchedc.module.dashboard.dto;

import java.util.List;

/**
 * Lightweight bootstrap payload returned by GET /api/v1/dashboard/bootstrap.
 * Contains the minimum data needed to render the Dashboard's first screen:
 * user greeting, study/site context, and permission-filtered module list.
 */
public class BootstrapResponse {

    private UserInfo user;
    private List<StudyInfo> studies;
    private StudyInfo defaultStudy;
    private List<ModuleInfo> modules;

    public BootstrapResponse() {
    }

    public BootstrapResponse(UserInfo user, List<StudyInfo> studies,
                             StudyInfo defaultStudy, List<ModuleInfo> modules) {
        this.user = user;
        this.studies = studies;
        this.defaultStudy = defaultStudy;
        this.modules = modules;
    }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    public List<StudyInfo> getStudies() { return studies; }
    public void setStudies(List<StudyInfo> studies) { this.studies = studies; }

    public StudyInfo getDefaultStudy() { return defaultStudy; }
    public void setDefaultStudy(StudyInfo defaultStudy) { this.defaultStudy = defaultStudy; }

    public List<ModuleInfo> getModules() { return modules; }
    public void setModules(List<ModuleInfo> modules) { this.modules = modules; }

    // --- nested types ---

    public static class UserInfo {
        private Integer userId;
        private String username;
        private String firstName;
        private String lastName;
        private List<String> roles;

        public UserInfo() {
        }

        public UserInfo(Integer userId, String username, String firstName,
                        String lastName, List<String> roles) {
            this.userId = userId;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.roles = roles;
        }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
    }

    public static class StudyInfo {
        private Integer studyId;
        private String name;
        private boolean site;
        private Integer parentStudyId;
        private String role;

        public StudyInfo() {
        }

        public StudyInfo(Integer studyId, String name, boolean site,
                         Integer parentStudyId, String role) {
            this.studyId = studyId;
            this.name = name;
            this.site = site;
            this.parentStudyId = parentStudyId;
            this.role = role;
        }

        public Integer getStudyId() { return studyId; }
        public void setStudyId(Integer studyId) { this.studyId = studyId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isSite() { return site; }
        public void setSite(boolean site) { this.site = site; }
        public Integer getParentStudyId() { return parentStudyId; }
        public void setParentStudyId(Integer parentStudyId) { this.parentStudyId = parentStudyId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class ModuleInfo {
        private String key;
        private String name;
        private String description;
        private String path;
        private int priority;

        public ModuleInfo() {
        }

        public ModuleInfo(String key, String name, String description,
                          String path, int priority) {
            this.key = key;
            this.name = name;
            this.description = description;
            this.path = path;
            this.priority = priority;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }
}

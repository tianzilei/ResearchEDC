package org.researchedc.module.dashboard.dto;

/**
 * Response payload for GET /api/v1/dashboard/system-info.
 * Non-sensitive system information for the admin system configuration page.
 * Replaces the legacy /auth/api/v1/system/systemstatus and /auth/api/v1/system/config
 * endpoints, which exposed sensitive credentials.
 */
public class SystemInfoResponse {

    private String appName;
    private String appVersion;
    private String javaVersion;
    private String javaVendor;
    private String osName;
    private String osVersion;
    private String osArch;
    private String databaseProduct;
    private String databaseVersion;
    private long totalMemoryMB;
    private long freeMemoryMB;
    private long usableDiskSpaceMB;

    public SystemInfoResponse() {
    }

    public SystemInfoResponse(String appName, String appVersion, String javaVersion,
                              String javaVendor, String osName, String osVersion,
                              String osArch, String databaseProduct, String databaseVersion,
                              long totalMemoryMB, long freeMemoryMB, long usableDiskSpaceMB) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.javaVersion = javaVersion;
        this.javaVendor = javaVendor;
        this.osName = osName;
        this.osVersion = osVersion;
        this.osArch = osArch;
        this.databaseProduct = databaseProduct;
        this.databaseVersion = databaseVersion;
        this.totalMemoryMB = totalMemoryMB;
        this.freeMemoryMB = freeMemoryMB;
        this.usableDiskSpaceMB = usableDiskSpaceMB;
    }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }

    public String getJavaVendor() { return javaVendor; }
    public void setJavaVendor(String javaVendor) { this.javaVendor = javaVendor; }

    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }

    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }

    public String getOsArch() { return osArch; }
    public void setOsArch(String osArch) { this.osArch = osArch; }

    public String getDatabaseProduct() { return databaseProduct; }
    public void setDatabaseProduct(String databaseProduct) { this.databaseProduct = databaseProduct; }

    public String getDatabaseVersion() { return databaseVersion; }
    public void setDatabaseVersion(String databaseVersion) { this.databaseVersion = databaseVersion; }

    public long getTotalMemoryMB() { return totalMemoryMB; }
    public void setTotalMemoryMB(long totalMemoryMB) { this.totalMemoryMB = totalMemoryMB; }

    public long getFreeMemoryMB() { return freeMemoryMB; }
    public void setFreeMemoryMB(long freeMemoryMB) { this.freeMemoryMB = freeMemoryMB; }

    public long getUsableDiskSpaceMB() { return usableDiskSpaceMB; }
    public void setUsableDiskSpaceMB(long usableDiskSpaceMB) { this.usableDiskSpaceMB = usableDiskSpaceMB; }
}

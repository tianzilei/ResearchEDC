package org.researchedc.module.openrosa.dto;

import java.util.ArrayList;
import java.util.List;

public class FormListResponse {

    private List<XFormEntry> xforms = new ArrayList<>();

    public List<XFormEntry> getXforms() { return xforms; }
    public void setXforms(List<XFormEntry> xforms) { this.xforms = xforms; }
    public void addXForm(XFormEntry xform) { this.xforms.add(xform); }

    public static class XFormEntry {
        private String formID;
        private String name;
        private String majorMinorVersion;
        private String version;
        private String hash;
        private String downloadUrl;
        private String manifestUrl;

        public String getFormID() { return formID; }
        public void setFormID(String v) { this.formID = v; }
        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public String getMajorMinorVersion() { return majorMinorVersion; }
        public void setMajorMinorVersion(String v) { this.majorMinorVersion = v; }
        public String getVersion() { return version; }
        public void setVersion(String v) { this.version = v; }
        public String getHash() { return hash; }
        public void setHash(String v) { this.hash = v; }
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String v) { this.downloadUrl = v; }
        public String getManifestUrl() { return manifestUrl; }
        public void setManifestUrl(String v) { this.manifestUrl = v; }
    }
}

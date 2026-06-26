package org.researchedc.module.openrosa.dto;

import java.util.ArrayList;
import java.util.List;

public class ManifestResponse {

    private List<MediaFileEntry> mediaFiles = new ArrayList<>();

    public List<MediaFileEntry> getMediaFiles() { return mediaFiles; }
    public void setMediaFiles(List<MediaFileEntry> mediaFiles) { this.mediaFiles = mediaFiles; }
    public void addMediaFile(MediaFileEntry mf) { this.mediaFiles.add(mf); }

    public static class MediaFileEntry {
        private String filename;
        private String hash;
        private String downloadUrl;

        public String getFilename() { return filename; }
        public void setFilename(String v) { this.filename = v; }
        public String getHash() { return hash; }
        public void setHash(String v) { this.hash = v; }
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String v) { this.downloadUrl = v; }
    }
}

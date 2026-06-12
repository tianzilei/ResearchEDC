package org.researchedc.module.datacapture.internal.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.researchedc.bean.core.Utils;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AttachmentStorageAdapter {

    private static final Logger log = LoggerFactory.getLogger(AttachmentStorageAdapter.class);

    private final EventCRFDao eventCrfDao;
    private final IStudyDAO studyDao;
    private final IStudySubjectDAO studySubjectDao;

    public AttachmentStorageAdapter(EventCRFDao eventCrfDao,
                                    IStudyDAO studyDao,
                                    IStudySubjectDAO studySubjectDao) {
        this.eventCrfDao = eventCrfDao;
        this.studyDao = studyDao;
        this.studySubjectDao = studySubjectDao;
    }

    public String getStudyOidByEventCrf(int eventCrfId) {
        try {
            EventCRFBean eventCrf = (EventCRFBean) eventCrfDao.findByPK(eventCrfId);
            if (eventCrf == null) {
                return null;
            }

            StudySubjectBean studySubject = (StudySubjectBean) studySubjectDao.findByPK(eventCrf.getStudySubjectId());
            if (studySubject == null) {
                return null;
            }

            StudyBean study = (StudyBean) studyDao.findByPK(studySubject.getStudyId());
            return study != null ? study.getOid() : null;
        } catch (Exception e) {
            log.warn("Failed to resolve study OID for eventCrfId={}: {}", eventCrfId, e.getMessage());
            return null;
        }
    }

    public List<String> getCandidateStudyOids(String studyOid) {
        if (studyOid == null || studyOid.isBlank()) {
            return List.of();
        }

        List<String> oids = new ArrayList<>();
        oids.add(studyOid);

        StudyBean study = (StudyBean) studyDao.findByOid(studyOid);
        if (study == null) {
            return oids;
        }

        if (study.getParentStudyId() > 0) {
            StudyBean parent = (StudyBean) studyDao.findByPK(study.getParentStudyId());
            if (parent != null && parent.getOid() != null && !oids.contains(parent.getOid())) {
                oids.add(parent.getOid());
            }
        }

        var children = studyDao.findAllByParent(study.getId());
        if (children != null) {
            for (Object child : children) {
                StudyBean childStudy = (StudyBean) child;
                if (childStudy.getOid() != null && !oids.contains(childStudy.getOid())) {
                    oids.add(childStudy.getOid());
                }
            }
        }

        return oids;
    }

    public File resolveAttachmentFile(String fileName, String studyOid) {
        if (fileName == null || fileName.isEmpty() || studyOid == null || studyOid.isEmpty()) {
            return new File("");
        }
        String safeName = new File(fileName).getName();
        String rootPath = Utils.getAttachedFileRootPath();
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
        return new File(Utils.getAttachedFileRootPath(), studyOid);
    }
}

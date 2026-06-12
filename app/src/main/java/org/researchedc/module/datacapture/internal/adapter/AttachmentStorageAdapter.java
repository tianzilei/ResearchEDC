package org.researchedc.module.datacapture.internal.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.researchedc.bean.core.Utils;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AttachmentStorageAdapter {

    private static final Logger log = LoggerFactory.getLogger(AttachmentStorageAdapter.class);

    private final EventCRFDao eventCrfDao;
    private final IStudyDAO studyDao;
    private final IStudySubjectDAO studySubjectDao;
    private final IUserAccountDAO userAccountDao;

    public AttachmentStorageAdapter(EventCRFDao eventCrfDao,
                                    IStudyDAO studyDao,
                                    IStudySubjectDAO studySubjectDao,
                                    IUserAccountDAO userAccountDao) {
        this.eventCrfDao = eventCrfDao;
        this.studyDao = studyDao;
        this.studySubjectDao = studySubjectDao;
        this.userAccountDao = userAccountDao;
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


    public boolean canViewEventCrfData(int eventCrfId, Integer userId) {
        if (userId == null) {
            return false;
        }
        try {
            UserAccountBean user = (UserAccountBean) userAccountDao.findByPK(userId);
            if (user == null || !user.isActive()) {
                return false;
            }
            if (user.isSysAdmin()) {
                return true;
            }

            EventCRFBean eventCrf = (EventCRFBean) eventCrfDao.findByPK(eventCrfId);
            if (eventCrf == null) {
                return false;
            }
            StudySubjectBean studySubject = (StudySubjectBean) studySubjectDao.findByPK(eventCrf.getStudySubjectId());
            if (studySubject == null) {
                return false;
            }
            StudyBean study = (StudyBean) studyDao.findByPK(studySubject.getStudyId());
            if (study == null) {
                return false;
            }

            StudyUserRoleBean directRole = user.getRoleByStudy(study.getId());
            if (directRole != null && directRole.isActive() && !directRole.isInvalid()) {
                return true;
            }

            if (study.getParentStudyId() > 0) {
                StudyUserRoleBean parentRole = user.getRoleByStudy(study.getParentStudyId());
                return parentRole != null && parentRole.isActive() && !parentRole.isInvalid();
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to authorize attachment access: eventCrfId={}, userId={}, error={}",
                    eventCrfId, userId, e.getMessage());
            return false;
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

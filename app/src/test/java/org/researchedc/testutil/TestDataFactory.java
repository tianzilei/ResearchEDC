package org.researchedc.testutil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.researchedc.module.export.entity.ExportJob;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.randomization.entity.RandomizationArm;
import org.researchedc.module.randomization.entity.RandomizationAssignment;
import org.researchedc.module.randomization.entity.RandomizationScheme;
import org.researchedc.module.randomization.enums.AssignmentStatus;
import org.researchedc.module.randomization.enums.RandomizationAlgorithm;
import org.researchedc.module.randomization.enums.SchemeStatus;
import org.researchedc.module.study.entity.StudyEntity;
import org.researchedc.module.subject.entity.SubjectEntity;
import org.researchedc.module.subject.entity.StudySubjectEntity;

/**
 * Test data factory providing factory methods for creating domain objects
 * used across Modulith module tests. Reduces repetitive setup code.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    // ─── Randomization ──────────────────────────────────────

    public static RandomizationScheme createScheme(Long id, String name, SchemeStatus status) {
        RandomizationScheme s = new RandomizationScheme();
        s.setId(id);
        s.setName(name);
        s.setStatus(status);
        s.setAlgorithm(RandomizationAlgorithm.SIMPLE);
        s.setStudyId(1);
        return s;
    }

    public static RandomizationArm createArm(Long id, String name, int ratio, int order) {
        RandomizationArm a = new RandomizationArm();
        a.setId(id);
        a.setName(name);
        a.setRatio(ratio);
        a.setOrderNumber(order);
        return a;
    }

    public static RandomizationAssignment createAssignment(Long id, Long schemeId,
            Integer studySubjectId, RandomizationArm arm, AssignmentStatus status) {
        RandomizationAssignment a = new RandomizationAssignment();
        a.setId(id);
        a.setStudySubjectId(studySubjectId);
        a.setArm(arm);
        a.setStatus(status);
        if (arm != null && arm.getScheme() != null) {
            a.setScheme(arm.getScheme());
        }
        return a;
    }

    public static List<RandomizationArm> createArms(int count) {
        List<RandomizationArm> arms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            arms.add(createArm((long) i + 1, "Arm " + (char) ('A' + i), 1, i + 1));
        }
        return arms;
    }

    // ─── Export ─────────────────────────────────────────────

    public static ExportJob createExportJob(Long id, String name, ExportJobStatus status) {
        ExportJob j = new ExportJob();
        j.setId(id);
        j.setName(name);
        j.setStatus(status);
        j.setStudyId(1);
        j.setExportFormat(ExportFormat.CSV);
        j.setRequestedBy(1);
        j.setRetryCount(0);
        return j;
    }

    public static ExportJob createCompletedJob(Long id, String name, String filePath, Long fileSize) {
        ExportJob j = createExportJob(id, name, ExportJobStatus.COMPLETED);
        j.setFilePath(filePath);
        j.setFileSize(fileSize);
        j.setCompletedDate(LocalDateTime.now());
        return j;
    }

    // ─── Study ──────────────────────────────────────────────

    public static StudyEntity createStudy(Integer id, String name) {
        StudyEntity s = new StudyEntity();
        s.setStudyId(id);
        s.setName(name);
        s.setUniqueIdentifier("STUDY_" + id);
        return s;
    }

    // ─── Subject ────────────────────────────────────────────

    public static SubjectEntity createSubject(Integer id, String uniqueId) {
        SubjectEntity s = new SubjectEntity();
        s.setSubjectId(id);
        s.setUniqueIdentifier(uniqueId);
        s.setDobCollected(false);
        return s;
    }

    public static StudySubjectEntity createStudySubject(Integer id, Integer studyId,
            Integer subjectId, String label) {
        StudySubjectEntity ss = new StudySubjectEntity();
        ss.setStudySubjectId(id);
        ss.setStudyId(studyId);
        ss.setSubjectId(subjectId);
        ss.setLabel(label);
        ss.setEnrollmentDate(LocalDateTime.now());
        return ss;
    }
}

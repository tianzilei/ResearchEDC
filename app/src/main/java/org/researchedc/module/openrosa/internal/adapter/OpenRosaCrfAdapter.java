package org.researchedc.module.openrosa.internal.adapter;

import java.util.List;
import java.util.Optional;

import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.study.entity.StudyEntity;
import org.researchedc.module.study.repository.StudyRepository;
import org.springframework.stereotype.Component;

@Component
public class OpenRosaCrfAdapter {

    private final CrfRepository crfRepository;
    private final CrfVersionRepository crfVersionRepository;
    private final StudyRepository studyRepository;

    public OpenRosaCrfAdapter(CrfRepository crfRepository,
                              CrfVersionRepository crfVersionRepository,
                              StudyRepository studyRepository) {
        this.crfRepository = crfRepository;
        this.crfVersionRepository = crfVersionRepository;
        this.studyRepository = studyRepository;
    }

    public Optional<StudyEntity> findStudyByOcOid(String ocOid) {
        return studyRepository.findByOcOid(ocOid);
    }

    public Optional<CrfVersionEntity> findCrfVersionByOcOid(String ocOid) {
        return crfVersionRepository.findByOcOid(ocOid);
    }

    public List<CrfVersionEntity> findActiveCrfVersions() {
        return crfVersionRepository.findByCrfIdAndStatusIdNot(0, 5);
    }

    /**
     * Find all CRF versions associated with a study (via event definition CRF default version).
     */
    public List<CrfVersionEntity> findCrfVersionsByStudyOid(String studyOid) {
        return crfVersionRepository.findByStudyOcOid(studyOid);
    }

    public Optional<org.researchedc.module.crf.entity.CrfEntity> findCrfById(Integer crfId) {
        return crfRepository.findById(crfId);
    }
}

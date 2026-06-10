package org.researchedc.module.crf.internal.adapter.repository;

import java.util.ArrayList;

import org.researchedc.domain.datamap.CrfVersionMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrfVersionMediaRepository extends JpaRepository<CrfVersionMedia, Integer> {

    ArrayList<CrfVersionMedia> findByCrfVersion_CrfVersionId(int crfVersionId);
}

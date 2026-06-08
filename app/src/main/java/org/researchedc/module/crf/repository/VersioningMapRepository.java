package org.researchedc.module.crf.repository;

import org.researchedc.domain.datamap.VersioningMap;
import org.researchedc.domain.datamap.VersioningMapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersioningMapRepository extends JpaRepository<VersioningMap, VersioningMapId> {
}

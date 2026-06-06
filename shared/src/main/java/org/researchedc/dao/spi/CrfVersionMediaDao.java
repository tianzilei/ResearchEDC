package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.CrfVersionMedia;

import java.util.ArrayList;

public interface CrfVersionMediaDao {

    ArrayList<CrfVersionMedia> findByCrfVersionId(int crf_version_id);

    CrfVersionMedia findById(Integer id);
    default void saveOrUpdate(org.researchedc.domain.datamap.CrfVersionMedia entity) { throw new UnsupportedOperationException(); }
}

package org.researchedc.module.crf.internal.adapter;

import org.researchedc.dao.spi.VersioningMapDao;
import org.researchedc.module.crf.internal.adapter.repository.VersioningMapRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("versioningMapDao")
@Primary
public class VersioningMapDaoAdapter implements VersioningMapDao {

    private final VersioningMapRepository repository;

    public VersioningMapDaoAdapter(VersioningMapRepository repository) {
        this.repository = repository;
    }

}

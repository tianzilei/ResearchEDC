package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;

import org.researchedc.dao.spi.CrfVersionMediaDao;
import org.researchedc.domain.datamap.CrfVersionMedia;
import org.researchedc.module.crf.repository.CrfVersionMediaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("crfVersionMediaDao")
@Primary
public class CrfVersionMediaDaoAdapter implements CrfVersionMediaDao {

    private final CrfVersionMediaRepository repository;

    public CrfVersionMediaDaoAdapter(CrfVersionMediaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ArrayList<CrfVersionMedia> findByCrfVersionId(int crfVersionId) {
        return repository.findByCrfVersion_CrfVersionId(crfVersionId);
    }

    @Override
    public CrfVersionMedia findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void saveOrUpdate(CrfVersionMedia entity) {
        repository.save(entity);
    }
}

package org.researchedc.dao.spi;

import java.util.List;

import org.researchedc.domain.datamap.ResponseSet;

public interface ResponseSetDomainDao {

    ResponseSet findByLabelVersion(String label, Integer version);

    List<ResponseSet> findAllByItemId(int itemId);

    ResponseSet saveOrUpdate(ResponseSet responseSet);
}

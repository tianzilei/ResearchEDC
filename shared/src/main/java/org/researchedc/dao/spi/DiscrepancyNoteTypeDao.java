package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.DiscrepancyNoteType;

public interface DiscrepancyNoteTypeDao {

    DiscrepancyNoteType findByDiscrepancyNoteTypeId(Integer discrepancyNoteTypeId);

}

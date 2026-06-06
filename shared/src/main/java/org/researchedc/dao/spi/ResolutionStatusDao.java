package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.ResolutionStatus;

public interface ResolutionStatusDao {

    ResolutionStatus findByResolutionStatusId(Integer resolutionStatusId);

}

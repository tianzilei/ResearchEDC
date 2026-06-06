package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.CompletionStatus;

public interface CompletionStatusDao {

    CompletionStatus findByCompletionStatusId(int completion_status_id);

}

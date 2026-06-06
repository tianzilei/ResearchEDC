package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.ItemDataFlagWorkflow;

public interface ItemDataFlagWorkflowDao {

    default ItemDataFlagWorkflow findById(Integer id) {
        throw new UnsupportedOperationException();
    }

    default ItemDataFlagWorkflow saveOrUpdate(ItemDataFlagWorkflow itemDataFlagWorkflow) {
        throw new UnsupportedOperationException();
    }

}

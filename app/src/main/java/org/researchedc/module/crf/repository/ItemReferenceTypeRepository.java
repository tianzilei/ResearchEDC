package org.researchedc.module.crf.repository;

import org.researchedc.domain.datamap.ItemReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemReferenceTypeRepository extends JpaRepository<ItemReferenceType, Integer> {

    ItemReferenceType findByItemReferenceTypeId(int itemReferenceTypeId);
}

package org.researchedc.module.crf.repository;

import org.researchedc.domain.datamap.ItemDataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDataTypeRepository extends JpaRepository<ItemDataType, Integer> {

    ItemDataType findByCode(String code);

    ItemDataType findByItemDataTypeId(int itemDataTypeId);

    @Query(value = "select idt.* from item_data_type idt join item i on idt.item_data_type_id=i.item_data_type_id where i.item_id = :itemId", nativeQuery = true)
    ItemDataType findByItemId(@Param("itemId") int itemId);
}

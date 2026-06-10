package org.researchedc.module.crf.internal.adapter.repository;

import org.researchedc.domain.datamap.ResponseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseTypeRepository extends JpaRepository<ResponseType, Integer> {

    ResponseType findByName(String name);

    @Query(value = "select rt.* from response_type rt, response_set rs, item_form_metadata ifm where ifm.response_set_id=rs.response_set_id"
            + " and rs.response_type_id=rt.response_type_id and ifm.item_form_metadata_id = :itemFormMetadataId", nativeQuery = true)
    ResponseType findByItemFormMetaDataId(@Param("itemFormMetadataId") Integer itemFormMetadataId);
}

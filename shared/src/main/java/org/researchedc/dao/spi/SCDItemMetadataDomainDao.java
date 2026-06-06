package org.researchedc.dao.spi;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.domain.crfdata.SCDItemMetadataBean;

public interface SCDItemMetadataDomainDao {

    ArrayList<SCDItemMetadataBean> findAllBySectionId(Integer sectionId);

    List<Integer> findAllSCDItemFormMetadataIdsBySectionId(Integer sectionId);

    ArrayList<SCDItemMetadataBean> findAllSCDByItemFormMetadataId(Integer itemFormMetadataId);
}

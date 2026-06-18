package org.researchedc.module.dataimport.service;

import java.util.List;

public interface ImportItemGroupPort {

    List<Object[]> findImportItemGroupsByOid(String oid);
}

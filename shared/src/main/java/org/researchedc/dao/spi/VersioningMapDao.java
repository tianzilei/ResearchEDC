package org.researchedc.dao.spi;

public interface VersioningMapDao {

    default void saveOrUpdate(org.researchedc.domain.datamap.VersioningMap entity) { throw new UnsupportedOperationException(); }
}

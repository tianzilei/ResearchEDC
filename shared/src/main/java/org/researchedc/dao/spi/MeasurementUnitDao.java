package org.researchedc.dao.spi;

import java.util.TreeSet;

public interface MeasurementUnitDao {

    TreeSet<String> findAllOIDs();

    TreeSet<String> findAllNames();

    TreeSet<String> findAllNamesInUpperCase();

}

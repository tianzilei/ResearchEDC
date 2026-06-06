package org.researchedc.dao.spi;

import org.researchedc.domain.technicaladmin.DatabaseChangeLogBean;

import java.util.ArrayList;

public interface DatabaseChangeLogDao {

    ArrayList<DatabaseChangeLogBean> findAll();

    DatabaseChangeLogBean findById(String id, String author, String fileName);

    Long count();

}

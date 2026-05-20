package org.researchedc.service.rule.expression;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.templates.OcDbTestCase;

public class SampleTest extends OcDbTestCase {

    public SampleTest() {
        super();
    }

    public void testStatement() {
        StudyDAO studyDao = new StudyDAO(getDataSource());
        StudyBean study = (StudyBean) studyDao.findByPK(1);
        assertNotNull(study);
    }
}
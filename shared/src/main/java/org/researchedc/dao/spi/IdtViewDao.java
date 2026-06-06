package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.IdtView;

import java.util.ArrayList;
import java.util.List;

public interface IdtViewDao {

    List<IdtView> findFilter1(int studyId, int pStudyId, int per_page, int page, ArrayList<String> studySubjects,
            ArrayList<String> eventDefs, ArrayList<String> crfs, int tagId, String operation);

    String getListOf(ArrayList<String> objects);

}

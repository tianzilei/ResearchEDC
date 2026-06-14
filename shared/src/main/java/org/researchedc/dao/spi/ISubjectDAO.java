package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.dao.submit.ListSubjectFilter;
import org.researchedc.dao.submit.ListSubjectSort;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface ISubjectDAO {
    EntityBean findByPK(int ID);
    EntityBean create(EntityBean eb);
    SubjectBean create(SubjectBean sb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByLimit(boolean hasLimit);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    Collection findAllChildrenByPK(int subjectId);
    ArrayList findAllSubjectsAndStudies();
    ArrayList findAllByGender(char gender);
    ArrayList findAllMales();
    ArrayList findAllFemales();
    ArrayList findAllByGenderNotSelf(char gender, int id);
    ArrayList findAllMalesNotSelf(int id);
    ArrayList findAllFemalesNotSelf(int id);
    ArrayList<SubjectBean> getWithFilterAndSort(StudyBean currentStudy, ListSubjectFilter filter, ListSubjectSort sort, int rowStart, int rowEnd);
    Integer getCountWithFilter(ListSubjectFilter filter, StudyBean currentStudy);
    EntityBean findAnotherByIdentifier(String name, int subjectId);
    SubjectBean findByUniqueIdentifier(String uniqueIdentifier);
    SubjectBean findByUniqueIdentifierAndAnyStudy(String uniqueIdentifier, int studyId);
    SubjectBean findByUniqueIdentifierAndStudy(String uniqueIdentifier, int studyId);
    SubjectBean findByUniqueIdentifierAndParentStudy(String uniqueIdentifier, int studyId);
    void deleteTestSubject(String uniqueIdentifier);
    Object getEntityFromHashMap(HashMap hm);
}

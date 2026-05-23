package org.researchedc.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.bean.oid.OidGenerator;
import org.researchedc.bean.oid.StudySubjectOidGenerator;
import org.researchedc.domain.datamap.Study;
import org.researchedc.domain.datamap.StudyEvent;
import org.researchedc.domain.datamap.StudySubject;

public class StudySubjectDao extends AbstractDomainDao<StudySubject> {

    @Override
    Class<StudySubject> domainClass() {
        // TODO Auto-generated method stub
        return StudySubject.class;
    }
    
    @SuppressWarnings("unchecked")
    public List<StudySubject> findAllByStudy(Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.study.studyId = :studyid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", studyId);
        return (List<StudySubject>) q.list();
      
    }

    public StudySubject findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("OCOID", OCOID);
        return (StudySubject) q.uniqueResult();
    }

    public StudySubject findByLabelAndStudy(String embeddedStudySubjectId, Study study) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.study.studyId = :studyid and do.label = :label";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", study.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
        return (StudySubject) q.uniqueResult();
    }

    public StudySubject findByLabelAndStudyOrParentStudy(String embeddedStudySubjectId, Study study) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where (do.study.studyId = :studyid or do.study.study.studyId = :studyid) and do.label = :label";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", study.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
        return (StudySubject) q.uniqueResult();
    }

    public ArrayList<StudySubject> findByLabelAndParentStudy(String embeddedStudySubjectId, Study parentStudy) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.study.study.studyId = :studyid and do.label = :label";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", parentStudy.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
        return (ArrayList<StudySubject>) q.list();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ArrayList<StudyEvent> fetchListSEs(String id) {
        String query = " from StudyEvent se where se.studySubject.ocOid = :id order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("id", id.toString());

        return (ArrayList<StudyEvent>) q.list();

    }
    public String getValidOid(StudySubject studySubject, ArrayList<String> oidList) {
    OidGenerator oidGenerator = new StudySubjectOidGenerator();
        String oid = getOid(studySubject);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(StudySubject studySubject) {
        OidGenerator oidGenerator = new StudySubjectOidGenerator();
        String oid;
        try {
            oid = studySubject.getOcOid() != null ? studySubject.getOcOid() : oidGenerator.generateOid(studySubject.getLabel());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public int findTheGreatestLabelByStudy(Integer studyId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where (do.study.studyId = :studyid or do.study.study.studyId = :studyid)";

        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", studyId);
        List<StudySubject> allStudySubjects = (ArrayList<StudySubject>) q.list();
        
        int greatestLabel = 0;
        for (StudySubject subject:allStudySubjects) {
            int labelInt = 0;
            try {
                labelInt = Integer.parseInt(subject.getLabel());
            } catch (NumberFormatException ne) {
                labelInt = 0;
            }
            if (labelInt > greatestLabel) {
                greatestLabel = labelInt;
            }
        }
        return greatestLabel;
    }

}

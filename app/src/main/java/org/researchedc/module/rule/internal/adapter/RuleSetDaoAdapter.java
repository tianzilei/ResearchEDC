package org.researchedc.module.rule.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.rule.RuleSetBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.spi.IRuleSetDAO;
import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.repository.RuleSetRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleSetDAO")
@Primary
@Transactional(readOnly = true)
public class RuleSetDaoAdapter implements IRuleSetDAO {

    private final RuleSetRepository repository;

    public RuleSetDaoAdapter(RuleSetRepository repository) {
        this.repository = repository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(RuleSetBean::new);
    }

    @Override
    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                          String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy) {
        if (currentStudy == null || currentStudy.getId() <= 0) {
            return new ArrayList<>();
        }
        return toBeans(repository.findByStudyIdOrderByRuleSetId(currentStudy.getId()));
    }

    @Override
    public ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy) {
        if (crfBean == null || currentStudy == null
                || crfBean.getId() <= 0 || currentStudy.getId() <= 0) {
            return new ArrayList<>();
        }
        return toBeans(repository.findByCrfIdAndStudyId(crfBean.getId(), currentStudy.getId()));
    }

    @Override
    public ArrayList<RuleSetBean> findByCrfVersionStudyAndStudyEventDefinition(
            CRFVersionBean crfVersionBean, StudyBean currentStudy, StudyEventDefinitionBean sed) {
        if (crfVersionBean == null || currentStudy == null || sed == null
                || crfVersionBean.getId() <= 0 || currentStudy.getId() <= 0 || sed.getId() <= 0) {
            return new ArrayList<>();
        }
        return toBeans(repository.findByCrfVersionIdAndStudyIdAndStudyEventDefinitionId(
                crfVersionBean.getId(), currentStudy.getId(), sed.getId()));
    }

    @Override
    public ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(
            CRFVersionBean crfVersion, CRFBean crfBean, StudyBean currentStudy, StudyEventDefinitionBean sed) {
        if (currentStudy == null || sed == null
                || currentStudy.getId() <= 0 || sed.getId() <= 0) {
            return new ArrayList<>();
        }
        Integer crfVersionId = crfVersion != null && crfVersion.getId() > 0 ? crfVersion.getId() : 0;
        Integer crfId = crfBean != null && crfBean.getId() > 0 ? crfBean.getId() : 0;
        return toBeans(repository.findByCrfVersionIdOrCrfIdAndStudyIdAndStudyEventDefinitionId(
                crfVersionId, crfId, currentStudy.getId(), sed.getId()));
    }

    @Override
    public ArrayList<RuleSetBean> findByCrfStudyAndStudyEventDefinition(
            CRFBean crfBean, StudyBean currentStudy, StudyEventDefinitionBean sed) {
        if (crfBean == null || currentStudy == null || sed == null
                || crfBean.getId() <= 0 || currentStudy.getId() <= 0 || sed.getId() <= 0) {
            return new ArrayList<>();
        }
        return toBeans(repository.findByCrfIdAndStudyIdAndStudyEventDefinitionId(
                crfBean.getId(), currentStudy.getId(), sed.getId()));
    }

    @Override
    public RuleSetBean findByExpression(RuleSetBean ruleSetBean) {
        if (ruleSetBean == null || ruleSetBean.getTarget() == null) {
            return null;
        }
        Integer expressionId = ruleSetBean.getTarget().getId();
        if (expressionId == null || expressionId <= 0) {
            return null;
        }
        return repository.findByRuleExpressionId(expressionId)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public RuleSetBean findByStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        if (studyEventDefinition == null || studyEventDefinition.getId() <= 0) {
            return null;
        }
        List<RuleSetEntity> results = repository.findByStudyEventDefinitionId(studyEventDefinition.getId());
        return results.isEmpty() ? null : toBean(results.get(0));
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        RuleSetBean bean = (RuleSetBean) eb;
        RuleSetEntity entity = new RuleSetEntity();
        apply(bean, entity);
        entity.setStatusId(Status.AVAILABLE.getId());
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        RuleSetBean bean = (RuleSetBean) eb;
        RuleSetEntity entity = repository.findById(bean.getId()).orElseGet(RuleSetEntity::new);
        entity.setRuleSetId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(bean.getUpdaterId());
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean remove(RuleSetBean ruleSetBean, UserAccountBean ub) {
        RuleSetEntity entity = repository.findById(ruleSetBean.getId()).orElse(null);
        if (entity == null) {
            return new RuleSetBean();
        }
        entity.setStatusId(Status.DELETED.getId());
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(ub != null ? ub.getId() : 0);
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean restore(RuleSetBean ruleSetBean, UserAccountBean ub) {
        RuleSetEntity entity = repository.findById(ruleSetBean.getId()).orElse(null);
        if (entity == null) {
            return new RuleSetBean();
        }
        entity.setStatusId(Status.AVAILABLE.getId());
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(ub != null ? ub.getId() : 0);
        return toBean(repository.save(entity));
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        RuleSetEntity entity = new RuleSetEntity();
        entity.setRuleSetId((Integer) hm.get("rule_set_id"));
        entity.setRuleExpressionId((Integer) hm.get("rule_expression_id"));
        entity.setStudyEventDefinitionId((Integer) hm.get("study_event_definition_id"));
        entity.setCrfId((Integer) hm.get("crf_id"));
        entity.setCrfVersionId((Integer) hm.get("crf_version_id"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setRunSchedule((Boolean) hm.get("run_schedule"));
        entity.setRunTime((String) hm.get("run_time"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setUpdateId((Integer) hm.get("update_id"));
        return toBean(entity);
    }

    private void apply(RuleSetBean bean, RuleSetEntity entity) {
        entity.setRuleExpressionId(bean.getTarget() != null ? bean.getTarget().getId() : null);
        entity.setStudyEventDefinitionId(bean.getStudyEventDefinition() != null
                ? bean.getStudyEventDefinition().getId() : null);
        entity.setCrfId(bean.getCrf() != null ? bean.getCrf().getId() : null);
        entity.setCrfVersionId(bean.getCrfVersion() != null ? bean.getCrfVersion().getId() : null);
        entity.setStudyId(bean.getStudy() != null ? bean.getStudy().getId() : null);
        entity.setRunSchedule(bean.getRuleSetRules() != null && !bean.getRuleSetRules().isEmpty());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList<RuleSetBean> toBeans(List<RuleSetEntity> entities) {
        ArrayList<RuleSetBean> beans = new ArrayList<>();
        entities.stream()
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private RuleSetBean toBean(RuleSetEntity entity) {
        RuleSetBean bean = new RuleSetBean();
        if (entity.getRuleSetId() != null) {
            bean.setId(entity.getRuleSetId());
        }
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));

        // Reference beans with minimal population
        if (entity.getStudyId() != null && entity.getStudyId() > 0) {
            StudyBean study = new StudyBean();
            study.setId(entity.getStudyId());
            bean.setStudy(study);
        }
        if (entity.getCrfId() != null && entity.getCrfId() > 0) {
            CRFBean crf = new CRFBean();
            crf.setId(entity.getCrfId());
            bean.setCrf(crf);
        }
        if (entity.getCrfVersionId() != null && entity.getCrfVersionId() > 0) {
            CRFVersionBean crfVersion = new CRFVersionBean();
            crfVersion.setId(entity.getCrfVersionId());
            bean.setCrfVersion(crfVersion);
        }
        if (entity.getStudyEventDefinitionId() != null && entity.getStudyEventDefinitionId() > 0) {
            StudyEventDefinitionBean sed = new StudyEventDefinitionBean();
            sed.setId(entity.getStudyEventDefinitionId());
            bean.setStudyEventDefinition(sed);
        }
        return bean;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return new Date(0);
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }
}

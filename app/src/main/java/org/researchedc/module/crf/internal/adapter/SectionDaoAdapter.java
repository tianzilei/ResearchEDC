package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.dao.spi.ISectionDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.module.crf.entity.SectionEntity;
import org.researchedc.module.crf.repository.SectionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("sectionDAO")
@Primary
@Transactional(readOnly = true)
public class SectionDaoAdapter extends SectionDAO implements ISectionDAO {

    private final SectionRepository repository;

    public SectionDaoAdapter(SectionRepository repository, DataSource dataSource) {
        super(dataSource);
        this.repository = repository;
    }

    @Override
    public void setTypesExpected() {
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        SectionEntity entity = new SectionEntity();
        entity.setSectionId(asInteger(hm.get("section_id")));
        entity.setCrfVersionId(asInteger(hm.get("crf_version_id")));
        entity.setLabel((String) hm.get("label"));
        entity.setTitle((String) hm.get("title"));
        entity.setSubtitle((String) hm.get("subtitle"));
        entity.setInstructions((String) hm.get("instructions"));
        entity.setPageNumberLabel((String) hm.get("page_number_label"));
        entity.setOrdinal(asInteger(hm.get("ordinal")));
        entity.setParentId(asInteger(hm.get("parent_id")));
        entity.setBorders(asInteger(hm.get("borders")));
        return toBean(entity);
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
    public Collection findByVersionId(int ID) {
        return toBeans(repository.findByCrfVersionIdOrderByOrdinal(ID));
    }

    @Override
    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(SectionBean::new);
    }

    @Override
    public ArrayList findAllByCRFVersionId(int crfVersionId) {
        ArrayList<SectionBean> result = new ArrayList<>();
        toBeans(repository.findByCrfVersionIdOrderByOrdinal(crfVersionId)).forEach(result::add);
        return result;
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        SectionBean bean = (SectionBean) eb;
        SectionEntity entity = new SectionEntity();
        apply(bean, entity);
        SectionBean saved = toBean(repository.save(entity));
        eb.setId(saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        SectionBean bean = (SectionBean) eb;
        SectionEntity entity = repository.findById(bean.getId())
                .orElseGet(SectionEntity::new);
        entity.setSectionId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        return toBean(repository.save(entity));
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                           String strOrderByColumn, boolean blnAscendingSort,
                                           String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    /*
     * Complex analytical methods below are NOT overridden —
     * they delegate to the parent SectionDAO (which uses the legacy `section` table via SQL).
     * The bidirectional sync triggers keep legacy and module tables in sync.
     */

    private void apply(SectionBean bean, SectionEntity entity) {
        entity.setCrfVersionId(bean.getCRFVersionId());
        entity.setLabel(bean.getLabel());
        entity.setTitle(bean.getTitle());
        entity.setSubtitle(bean.getSubtitle());
        entity.setInstructions(bean.getInstructions());
        entity.setPageNumberLabel(bean.getPageNumberLabel());
        entity.setOrdinal(bean.getOrdinal());
        entity.setParentId(bean.getParentId());
        entity.setBorders(bean.getBorders());
    }

    private List<SectionBean> toBeans(List<SectionEntity> entities) {
        List<SectionBean> beans = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(SectionEntity::getOrdinal, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private SectionBean toBean(SectionEntity entity) {
        SectionBean bean = new SectionBean();
        if (entity.getSectionId() != null) {
            bean.setId(entity.getSectionId());
        }
        bean.setCRFVersionId(valueOrZero(entity.getCrfVersionId()));
        bean.setLabel(valueOrEmpty(entity.getLabel()));
        bean.setTitle(valueOrEmpty(entity.getTitle()));
        bean.setSubtitle(valueOrEmpty(entity.getSubtitle()));
        bean.setInstructions(valueOrEmpty(entity.getInstructions()));
        bean.setPageNumberLabel(valueOrEmpty(entity.getPageNumberLabel()));
        bean.setOrdinal(valueOrZero(entity.getOrdinal()));
        bean.setParentId(valueOrZero(entity.getParentId()));
        bean.setBorders(valueOrZero(entity.getBorders()));
        return bean;
    }

    private Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}

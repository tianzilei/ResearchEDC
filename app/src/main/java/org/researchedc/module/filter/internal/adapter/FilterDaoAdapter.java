package org.researchedc.module.filter.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.extract.FilterBean;
import org.researchedc.bean.extract.FilterObjectBean;
import org.researchedc.dao.spi.FilterDao;
import org.researchedc.module.filter.entity.FilterEntity;
import org.researchedc.module.filter.repository.FilterRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("filterDao")
@Primary
@Transactional(readOnly = true)
public class FilterDaoAdapter implements FilterDao {

    private final FilterRepository filterRepository;

    public FilterDaoAdapter(FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return filterRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(FilterBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        FilterBean bean = (FilterBean) eb;
        FilterEntity entity = new FilterEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(filterRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        FilterBean bean = (FilterBean) eb;
        FilterEntity entity = filterRepository.findById(bean.getId())
                .orElseGet(FilterEntity::new);
        entity.setFilterId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(filterRepository.save(entity));
    }

    @Override
    public Collection findAll() {
        return toBeans(filterRepository.findByStatusId(Status.AVAILABLE.getId()));
    }

    @Override
    public Collection findAllAdmin() {
        return toBeans(filterRepository.findAll());
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
                                          boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public String genSQLStatement(String oldSQLStatement, String connector, ArrayList filterObjs) {
        StringBuffer sb = new StringBuffer();
        if (oldSQLStatement != null) {
            sb.append(oldSQLStatement);
        } else {
            sb.append(" and subject_id in " + "(select subject_id from extract_data_table where ");
        }
        String tailEnd = "";
        int count = 0;
        for (Object filterObj : filterObjs) {
            FilterObjectBean fob = (FilterObjectBean) filterObj;
            tailEnd = "(" + tailEnd;
            if (count != 0) {
                tailEnd = tailEnd + " " + connector + " ";
            }
            count++;
            if (fob.getOperand().equals(" like ") || fob.getOperand().equals(" not like ")) {
                fob.setValue("%" + fob.getValue() + "%");
            }
            tailEnd = tailEnd + "(item_id = " + fob.getItemId() + " and value " + fob.getOperand() + " '" + fob.getValue() + "'))";
        }
        if (oldSQLStatement != null) {
            sb.append(" and ");
        }
        sb.append(tailEnd);
        return sb.toString();
    }

    @Override
    public ArrayList genExplanation(ArrayList oldExplanation, String connector, ArrayList filterObjs) {
        ArrayList explanation = new ArrayList();
        if (oldExplanation != null) {
            explanation.addAll(oldExplanation);
        } else {
            explanation.add("This Filter will look for:");
        }
        int count = 0;
        for (Object filterObj : filterObjs) {
            FilterObjectBean fob = (FilterObjectBean) filterObj;
            explanation.add("A value " + fob.getOperand() + " " + fob.getValue() + " " + "for question " + fob.getItemName());
            count++;
            if (count < filterObjs.size()) {
                explanation.add(connector + " ");
            }
        }
        return explanation;
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        FilterEntity entity = new FilterEntity();
        entity.setFilterId((Integer) hm.get("filter_id"));
        entity.setName((String) hm.get("name"));
        entity.setDescription((String) hm.get("description"));
        entity.setSqlStatement((String) hm.get("sql_statement"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        return toBean(entity);
    }

    private void apply(FilterBean bean, FilterEntity entity) {
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setSqlStatement(bean.getSQLStatement());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList toBeans(List<FilterEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(FilterEntity::getFilterId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private FilterBean toBean(FilterEntity entity) {
        FilterBean bean = new FilterBean();
        if (entity.getFilterId() != null) {
            bean.setId(entity.getFilterId());
        }
        bean.setName(entity.getName());
        bean.setDescription(entity.getDescription());
        bean.setSQLStatement(entity.getSqlStatement());
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
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

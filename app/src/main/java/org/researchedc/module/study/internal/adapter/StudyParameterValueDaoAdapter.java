package org.researchedc.module.study.internal.adapter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.service.StudyParameter;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studyParameterValueDAO")
@Primary
@Transactional(readOnly = true)
public class StudyParameterValueDaoAdapter implements IStudyParameterValueDAO {

    private static final Logger log = LoggerFactory.getLogger(StudyParameterValueDaoAdapter.class);

    private final JdbcTemplate jdbc;

    // ── StudyParameterValueBean row mapper ──────────────────────────────
    private final RowMapper<StudyParameterValueBean> spvRowMapper = (rs, rowNum) -> {
        StudyParameterValueBean bean = new StudyParameterValueBean();
        bean.setId(rs.getInt("study_parameter_value_id"));
        if (rs.wasNull()) bean.setId(0);
        bean.setStudyId(rs.getInt("study_id"));
        bean.setParameter(rs.getString("parameter"));
        bean.setValue(rs.getString("value"));
        return bean;
    };

    // ── StudyParameter row mapper ────────────────────────────────────────
    private final RowMapper<StudyParameter> spRowMapper = (rs, rowNum) -> {
        StudyParameter sp = new StudyParameter();
        sp.setId(rs.getInt("study_parameter_id"));
        sp.setHandle(rs.getString("handle"));
        sp.setName(rs.getString("name"));
        sp.setDescription(rs.getString("description"));
        sp.setDefaultValue(rs.getString("default_value"));
        sp.setInheritable(rs.getBoolean("inheritable"));
        sp.setOverridable(rs.getBoolean("overridable"));
        return sp;
    };

    public StudyParameterValueDaoAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    // ── ACTIVE METHODS ───────────────────────────────────────────────────

    @Override
    public StudyParameterValueBean findByHandleAndStudy(int studyId, String handle) {
        String sql = "SELECT study_parameter_value_id, study_id, parameter, value " +
                     "FROM study_parameter_value WHERE study_id = ? AND parameter = ?";
        List<StudyParameterValueBean> results = jdbc.query(sql, spvRowMapper, studyId, handle);
        if (results.isEmpty()) {
            return new StudyParameterValueBean();
        }
        return results.get(0);
    }

    @Override
    public StudyParameter findParameterByHandle(String handle) {
        String sql = "SELECT study_parameter_id, handle, name, description, " +
                     "default_value, inheritable, overridable " +
                     "FROM study_parameter WHERE handle = ?";
        List<StudyParameter> results = jdbc.query(sql, spRowMapper, handle);
        if (results.isEmpty()) {
            return new StudyParameter();
        }
        return results.get(0);
    }

    @Override
    @Transactional
    public boolean setParameterValue(int studyId, String parameterHandle, String value) {
        String delSql = "DELETE FROM study_parameter_value WHERE study_id = ? AND parameter = ?";
        int deleted = jdbc.update(delSql, studyId, parameterHandle);
        log.debug("setParameterValue: deleted {} rows for study={} handle={}", deleted, studyId, parameterHandle);

        String insSql = "INSERT INTO study_parameter_value (study_parameter_value_id, study_id, parameter, value) " +
                        "VALUES (nextval('study_parameter_value_study_parameter_value_id_seq'), ?, ?, ?)";
        jdbc.update(insSql, studyId, parameterHandle, value);
        return true;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ArrayList findAllParameters() {
        String sql = "SELECT study_parameter_id, handle, name, description, " +
                     "default_value, inheritable, overridable " +
                     "FROM study_parameter ORDER BY handle";
        List<StudyParameter> results = jdbc.query(sql, spRowMapper);
        return new ArrayList<>(results);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ArrayList findAllParameterValuesByStudy(StudyBean study) {
        if (study == null || study.getId() <= 0) {
            return new ArrayList();
        }
        String sql = "SELECT study_parameter_value_id, study_id, parameter, value " +
                     "FROM study_parameter_value WHERE study_id = ? ORDER BY parameter";
        List<StudyParameterValueBean> results = jdbc.query(sql, spvRowMapper, study.getId());
        return new ArrayList<>(results);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ArrayList findParamConfigByStudy(StudyBean study) {
        if (study == null || study.getId() <= 0) {
            return new ArrayList();
        }
        String sql = "SELECT study_parameter_value_id, study_id, parameter, value " +
                     "FROM study_parameter_value WHERE study_id = ? ORDER BY parameter";
        List<StudyParameterValueBean> results = jdbc.query(sql, spvRowMapper, study.getId());
        return new ArrayList<>(results);
    }

    // ── CRUD (lightweight – used by AbstractDomainDao parent chain) ──────

    @Override
    public EntityBean findByPK(int ID) {
        String sql = "SELECT study_parameter_value_id, study_id, parameter, value " +
                     "FROM study_parameter_value WHERE study_parameter_value_id = ?";
        List<StudyParameterValueBean> results = jdbc.query(sql, spvRowMapper, ID);
        if (results.isEmpty()) {
            return new StudyParameterValueBean();
        }
        return results.get(0);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Collection findAll() {
        String sql = "SELECT study_parameter_value_id, study_id, parameter, value " +
                     "FROM study_parameter_value ORDER BY study_id, parameter";
        List<StudyParameterValueBean> results = jdbc.query(sql, spvRowMapper);
        return new ArrayList<>(results);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort,
                              String strSearchPhrase) {
        log.warn("findAll(sort) not implemented; returning empty");
        return new ArrayList();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                          String strOrderByColumn, boolean blnAscendingSort,
                                          String strSearchPhrase) {
        log.warn("findAllByPermission(5 args) not implemented; returning empty");
        return new ArrayList();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        log.warn("findAllByPermission(2 args) not implemented; returning empty");
        return new ArrayList();
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        StudyParameterValueBean bean = (StudyParameterValueBean) eb;
        String sql = "INSERT INTO study_parameter_value (study_parameter_value_id, study_id, parameter, value) " +
                     "VALUES (nextval('study_parameter_value_study_parameter_value_id_seq'), ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                    new String[] { "study_parameter_value_id" });
            ps.setInt(1, bean.getStudyId());
            ps.setString(2, bean.getParameter());
            ps.setString(3, bean.getValue());
            return ps;
        }, keyHolder);
        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            bean.setId(generatedKey.intValue());
        }
        return bean;
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        StudyParameterValueBean bean = (StudyParameterValueBean) eb;
        String sql = "UPDATE study_parameter_value SET study_id = ?, parameter = ?, value = ? " +
                     "WHERE study_parameter_value_id = ?";
        jdbc.update(sql, bean.getStudyId(), bean.getParameter(), bean.getValue(), bean.getId());
        return bean;
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        int id = hm.get("study_parameter_value_id") != null
                ? Integer.parseInt(hm.get("study_parameter_value_id").toString()) : 0;
        return findByPK(id);
    }

}

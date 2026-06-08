package org.researchedc.module.crf.internal.adapter;

import java.util.List;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.researchedc.dao.spi.MeasurementUnitDao;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("measurementUnitDao")
@Primary
@Transactional(readOnly = true)
public class MeasurementUnitDaoAdapter implements MeasurementUnitDao {

    private final JdbcTemplate jdbcTemplate;

    public MeasurementUnitDaoAdapter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TreeSet<String> findAllOIDs() {
        String sql = "SELECT oc_oid FROM measurement_unit ORDER BY oc_oid ASC";
        List<String> list = jdbcTemplate.queryForList(sql, String.class);
        return new TreeSet<>(list);
    }

    @Override
    public TreeSet<String> findAllNames() {
        String sql = "SELECT DISTINCT name FROM measurement_unit ORDER BY name ASC";
        List<String> list = jdbcTemplate.queryForList(sql, String.class);
        return new TreeSet<>(list);
    }

    @Override
    public TreeSet<String> findAllNamesInUpperCase() {
        String sql = "SELECT UPPER(name) FROM measurement_unit ORDER BY name ASC";
        List<String> list = jdbcTemplate.queryForList(sql, String.class);
        return new TreeSet<>(list);
    }
}

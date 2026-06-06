package org.researchedc.dao.spi;

import java.util.Map;

import org.researchedc.domain.usageStats.LogUsageStatsBean;

public interface UsageStatsServiceDao {
    LogUsageStatsBean findLatestUsageStatParamValue(String paramKey);
    void saveOCStartTimeToDB();
    void saveOCStopTimeToDB();
    Map<String, String> getEventDetailsOCStart();
}

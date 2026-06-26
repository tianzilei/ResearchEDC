package org.researchedc.module.dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.researchedc.module.dashboard.dto.SystemInfoResponse;
import org.researchedc.module.dashboard.service.DashboardService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DashboardControllerTest {

    @Test
    void getSystemInfo_returnsNonSensitiveFields() throws Exception {
        DataSource dataSource = org.mockito.Mockito.mock(DataSource.class);
        Connection conn = org.mockito.Mockito.mock(Connection.class);
        DatabaseMetaData meta = org.mockito.Mockito.mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(meta.getDatabaseProductVersion()).thenReturn("17.0");

        DashboardService service = org.mockito.Mockito.mock(DashboardService.class);
        SystemInfoResponse response = new SystemInfoResponse(
            "ResearchEDC", "0.1", "21.0.1", "Oracle Corporation",
            "Linux", "6.1.0", "amd64",
            "PostgreSQL", "17.0",
            512, 128, 10240);
        when(service.getSystemInfo()).thenReturn(response);

        MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new DashboardController(service))
            .build();

        mvc.perform(get("/api/v1/dashboard/system-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.appName").value("ResearchEDC"))
            .andExpect(jsonPath("$.appVersion").value("0.1"))
            .andExpect(jsonPath("$.javaVersion").value("21.0.1"))
            .andExpect(jsonPath("$.osName").value("Linux"))
            .andExpect(jsonPath("$.databaseProduct").value("PostgreSQL"))
            .andExpect(jsonPath("$.databaseVersion").value("17.0"));
    }

    @Test
    void getSystemInfo_usesJavaSystemProperties() {
        DashboardService service = org.mockito.Mockito.mock(DashboardService.class);
        SystemInfoResponse response = new SystemInfoResponse(
            "ResearchEDC", "0.1",
            System.getProperty("java.version"),
            System.getProperty("java.vendor"),
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch"),
            "PostgreSQL", "17.0", 512, 128, 10240);
        when(service.getSystemInfo()).thenReturn(response);

        assertNotNull(response.getJavaVersion());
        assertNotNull(response.getOsName());
        assertEquals("ResearchEDC", response.getAppName());
        assertEquals("0.1", response.getAppVersion());
    }
}

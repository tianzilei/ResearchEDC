package org.researchedc.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.researchedc.exception.OpenClinicaSystemException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class CoreResources implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    public static String PROPERTIES_DIR;
    private static String DB_NAME;
    private static Properties DATAINFO;
    private static Properties EXTRACTINFO;

    private Properties dataInfo;
    private Properties dataInfoProp;
    private Properties extractInfo;
    private Properties extractProp;

    public static final Integer PDF_ID = 10;
    public static final Integer TAB_ID = 8;
    public static final Integer CDISC_ODM_1_2_ID = 5;
    public static final Integer CDISC_ODM_1_2_EXTENSION_ID = 4;
    public static final Integer CDISC_ODM_1_3_ID = 3;
    public static final Integer CDISC_ODM_1_3_EXTENSION_ID = 2;
    public static final Integer SPSS_ID = 9;

    private static String webapp;
    protected final static Logger logger = LoggerFactory.getLogger(CoreResources.class);
    // private MessageSource messageSource;

    public static String ODM_MAPPING_DIR;

    // TODO:Clean up all system outs
    // default no arg constructor
    public CoreResources() {

    }

    /**
     * TODO: Delete me!
     * 
     * @param dataInfoProps
     * @throws IOException
     */
    public CoreResources(Properties dataInfoProps) throws IOException {
        this.dataInfo = dataInfoProps;
        if (resourceLoader == null)
            resourceLoader = new DefaultResourceLoader();
        try {
            String path = resourceLoader.getResource("/").getURI().getPath();
            String[] tokens = path.split("/");
            webapp = tokens[(tokens.length - 1)].trim();
        } catch (Exception e) {
            webapp = "ROOT";
        }
        if (webapp == null) {
            webapp = "ROOT";
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        try {
            // setPROPERTIES_DIR(resourceLoader);
            // @pgawade 18-April-2011 Fix for issue 8394
            try {
                String path = resourceLoader.getResource("/").getURI().getPath();
                String[] tokens = path.split("/");
                webapp = tokens[(tokens.length - 1)].trim();
            } catch (Exception e) {
                webapp = "ROOT";
            }
            if (webapp == null) {
                webapp = "ROOT";
            }
            String filePath = "$catalina.home/$WEBAPP.lower.config";

            filePath = replaceWebapp(filePath);
            filePath = replaceCatHome(filePath);

            String dataInfoPropFileName = filePath + "/datainfo.properties";
            String extractPropFileName = filePath + "/extract.properties";

            File df = new File(dataInfoPropFileName);
            if (df.exists()) {
                Properties p = new Properties();
                p.load(new FileInputStream(dataInfoPropFileName));
                dataInfo = p;
            }
            File ef = new File(extractPropFileName);
            if (ef.exists()) {
                Properties p = new Properties();
                p.load(new FileInputStream(extractPropFileName));
                extractInfo = p;
            }

            // Fallback: load from classpath for embedded mode where catalina.home is a temp dir
            if (dataInfo == null) {
                try {
                    Resource r = resourceLoader.getResource("classpath:datainfo.properties");
                    if (r.exists()) {
                        dataInfo = new Properties();
                        dataInfo.load(r.getInputStream());
                    }
                } catch (Exception e) {
                    // Classpath fallback failed, will error downstream
                }
            }
            if (extractInfo == null) {
                try {
                    Resource r = resourceLoader.getResource("classpath:extract.properties");
                    if (r.exists()) {
                        extractInfo = new Properties();
                        extractInfo.load(r.getInputStream());
                    }
                } catch (Exception e) {
                    // Classpath fallback failed, will error downstream
                }
            }

            String dbName = dataInfo.getProperty("dbType");

            DATAINFO = dataInfo;
            dataInfo = setDataInfoProperties();// weird, but there are references to dataInfo...MainMenuServlet for
                                               // instance

            EXTRACTINFO = extractInfo;

            DB_NAME = dbName;
            try {
                ODM_MAPPING_DIR = getField("filePath");
            } catch (Exception e) {
                // In embedded mode, file path resolution may fail silently
            }
            if (extractInfo != null) {
                copyBaseToDest(resourceLoader);
                // @pgawade 18-April-2011 Fix for issue 8394
                copyODMMappingXMLtoResources(resourceLoader);
                copyConfig();
            }

            // tbh, following line to be removed
            // reportUrl();

        } catch (OpenClinicaSystemException e) {
            logger.debug(e.getMessage());
            logger.debug(e.toString());
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * For changing values which are applicable to all properties, for ex webapp name can be used in any properties
     */
    private void setDataInfoVals() {

        Enumeration<String> properties = (Enumeration<String>) DATAINFO.propertyNames();
        String vals, key;
        while (properties.hasMoreElements()) {
            key = properties.nextElement();
            vals = DATAINFO.getProperty(key);
            // replacePaths(vals);
            vals = replaceWebapp(vals);
            vals = replaceCatHome(vals);
            DATAINFO.setProperty(key, vals);
        }

    }

    private static String replaceWebapp(String value) {

        if (value.contains("${WEBAPP}")) {
            value = value.replace("${WEBAPP}", webapp);
        }

        else if (value.contains("${WEBAPP.lower}")) {
            value = value.replace("${WEBAPP.lower}", webapp.toLowerCase());
        }
        if (value.contains("$WEBAPP.lower")) {
            value = value.replace("$WEBAPP.lower", webapp.toLowerCase());
        } else if (value.contains("$WEBAPP")) {
            value = value.replace("$WEBAPP", webapp);
        }

        return value;
    }

    private static String replaceCatHome(String value) {
        String catalina = null;
        if (catalina == null) {
            catalina = System.getProperty("CATALINA_HOME");
        }

        if (catalina == null) {
            catalina = System.getProperty("catalina.home");
        }

        if (catalina == null) {
            catalina = System.getenv("CATALINA_HOME");
        }

        if (catalina == null) {
            catalina = System.getenv("catalina.home");
        }
        // logMe("catalina home - " + value);
        // logMe("CATALINA_HOME system variable is " + System.getProperty("CATALINA_HOME"));
        // logMe("CATALINA_HOME system env variable is " + System.getenv("CATALINA_HOME"));
        // logMe(" -Dcatalina.home system property variable is"+System.getProperty(" -Dcatalina.home"));
        // logMe("CATALINA.HOME system env variable is"+System.getenv("catalina.home"));
        // logMe("CATALINA_BASE system env variable is"+System.getenv("CATALINA_BASE"));
        // Map<String, String> env = System.getenv();
        // for (String envName : env.keySet()) {
        // logMe("%s=%s%n"+ envName+ env.get(envName));
        // }

        if (value.contains("${catalina.home}") && catalina != null) {
            value = value.replace("${catalina.home}", catalina);
        }

        if (value.contains("$catalina.home") && catalina != null) {
            value = value.replace("$catalina.home", catalina);
        }

        return value;
    }

    private static String replacePaths(String vals) {
        if (vals != null) {
            if (vals.contains("/")) {
                vals = vals.replace("/", File.separator);
            } else if (vals.contains("\\")) {
                vals = vals.replace("\\", File.separator);
            } else if (vals.contains("\\\\")) {
                vals = vals.replace("\\\\", File.separator);
            }
        }
        return vals;
    }

    private Properties setDataInfoProperties() {

        String filePath = DATAINFO.getProperty("filePath");
        if (filePath == null || filePath.isEmpty())
            filePath = "$catalina.home/$WEBAPP.lower.data";
        String database = DATAINFO.getProperty("dbType");

        setDatabaseProperties(database);

        setDataInfoVals();
        if (DATAINFO.getProperty("filePath") == null || DATAINFO.getProperty("filePath").length() <= 0)
            DATAINFO.setProperty("filePath", filePath);

        DATAINFO.setProperty("changeLogFile", "src/main/resources/migration/master.xml");
        // sysURL.base
        String sysURLBase = DATAINFO.getProperty("sysURL").replace("MainMenu", "");
        DATAINFO.setProperty("sysURL.base", sysURLBase);

        if (DATAINFO.getProperty("org.quartz.jobStore.misfireThreshold") == null)
            DATAINFO.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        DATAINFO.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");

        if (database.equalsIgnoreCase("oracle")) {
            DATAINFO.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate");
        } else if (database.equalsIgnoreCase("postgres")) {
            DATAINFO.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        }

        DATAINFO.setProperty("org.quartz.jobStore.useProperties", "false");
        DATAINFO.setProperty("org.quartz.jobStore.tablePrefix", "oc_qrtz_");
        if (DATAINFO.getProperty("org.quartz.threadPool.threadCount") == null)
            DATAINFO.setProperty("org.quartz.threadPool.threadCount", "1");
        if (DATAINFO.getProperty("org.quartz.threadPool.threadPriority") == null)
            DATAINFO.setProperty("org.quartz.threadPool.threadPriority", "5");

        String attached_file_location = DATAINFO.getProperty("attached_file_location");
        if (attached_file_location == null || attached_file_location.isEmpty()) {
            attached_file_location = DATAINFO.getProperty("filePath") + "attached_files" + File.separator;
            DATAINFO.setProperty("attached_file_location", attached_file_location);
        }

        String change_passwd_required = DATAINFO.getProperty("change_passwd_required");
        if (change_passwd_required == null || change_passwd_required.isEmpty()) {
            change_passwd_required = "1";
            DATAINFO.setProperty("change_passwd_required", change_passwd_required);

        }
        setMailProps();
        setRuleDesignerProps();
        if (DATAINFO.getProperty("crfFileExtensions") != null)
            DATAINFO.setProperty("crf_file_extensions", DATAINFO.getProperty("crfFileExtensions"));
        if (DATAINFO.getProperty("crfFileExtensionSettings") != null)
            DATAINFO.setProperty("crf_file_extension_settings", DATAINFO.getProperty("crfFileExtensionSettings"));

        String dataset_file_delete = DATAINFO.getProperty("dataset_file_delete");
        if (dataset_file_delete == null)
            DATAINFO.setProperty("dataset_file_delete", "true");
        ;// TODO:Revisit me!
        String password_expiration_time = DATAINFO.getProperty("passwdExpirationTime");
        if (password_expiration_time != null)
            DATAINFO.setProperty("passwd_expiration_time", password_expiration_time);

        if (DATAINFO.getProperty("maxInactiveInterval") != null)
            DATAINFO.setProperty("max_inactive_interval", DATAINFO.getProperty("maxInactiveInterval"));

        DATAINFO.setProperty("ra", "Data_Entry_Person");
        DATAINFO.setProperty("ra2", "site_Data_Entry_Person2");
        DATAINFO.setProperty("investigator", "Investigator");
        DATAINFO.setProperty("director", "Study_Director");

        DATAINFO.setProperty("coordinator", "Study_Coordinator");
        DATAINFO.setProperty("monitor", "Monitor");
        DATAINFO.setProperty("ccts.waitBeforeCommit", "6000");

        String rss_url = DATAINFO.getProperty("rssUrl");
        if (rss_url == null || rss_url.isEmpty())
            rss_url = "http://blog.openclinica.com/feed/";
        DATAINFO.setProperty("rss.url", rss_url);
        String rss_more = DATAINFO.getProperty("rssMore");
        if (rss_more == null || rss_more.isEmpty())
            rss_more = "http://blog.openclinica.com/";
        DATAINFO.setProperty("rss.more", rss_more);

        String supportURL = DATAINFO.getProperty("supportURL");
        if (supportURL == null || supportURL.isEmpty())
            supportURL = "https://www.openclinica.com/support";
        DATAINFO.setProperty("supportURL", supportURL);

        DATAINFO.setProperty("show_unique_id", "1");

        DATAINFO.setProperty("auth_mode", "password");
        if (DATAINFO.getProperty("userAccountNotification") != null)
            DATAINFO.setProperty("user_account_notification", DATAINFO.getProperty("userAccountNotification"));
        logger.debug("DataInfo..." + DATAINFO);

        String designerURL = DATAINFO.getProperty("designerURL");
        if (designerURL == null || designerURL.isEmpty()) {
            DATAINFO.setProperty("designer.url", designerURL);
        }

        String xformEnabled = DATAINFO.getProperty("xformEnabled");
        if (xformEnabled == null || xformEnabled.isEmpty())
            DATAINFO.setProperty("xformEnabled", "");

        String portalURL = DATAINFO.getProperty("portalURL");
        if (portalURL == null || portalURL.isEmpty()) {
            DATAINFO.setProperty("portal.url", "");
            logger.debug(" Portal URL NOT Defined in datainfo ");
        } else {
            logger.debug("Portal URL IS Defined in datainfo:  " + portalURL);
        }
        String moduleManager = DATAINFO.getProperty("moduleManager");
        if (moduleManager == null || moduleManager.isEmpty()) {
            DATAINFO.setProperty("moduleManager.url", "");
            logger.debug(" Module Manager URL NOT Defined in datainfo ");
        } else {
            logger.debug("Module Manager URL IS Defined in datainfo:  " + moduleManager);
        }

        return DATAINFO;

    }

    private void setMailProps() {
        // Mail service disabled — no configuration needed
    }

    private void setRuleDesignerProps() {

        DATAINFO.setProperty("designer.url", DATAINFO.getProperty("designerURL"));
    }

    private void setDatabaseProperties(String database) {

        DATAINFO.setProperty("username", DATAINFO.getProperty("dbUser"));
        DATAINFO.setProperty("password", DATAINFO.getProperty("dbPass"));
        String url = null, driver = null, hibernateDialect = null;
        if (database.equalsIgnoreCase("postgres")) {
            url = "jdbc:postgresql:" + "//" + DATAINFO.getProperty("dbHost") + ":" + DATAINFO.getProperty("dbPort") + "/" + DATAINFO.getProperty("db");
            driver = "org.postgresql.Driver";
            hibernateDialect = "org.hibernate.dialect.PostgreSQLDialect";
        } else if (database.equalsIgnoreCase("oracle")) {
            url = "jdbc:oracle:thin:" + "@" + DATAINFO.getProperty("dbHost") + ":" + DATAINFO.getProperty("dbPort") + ":" + DATAINFO.getProperty("db");
            driver = "oracle.jdbc.driver.OracleDriver";
            hibernateDialect = "org.hibernate.dialect.OracleDialect";
        }
     
        DATAINFO.setProperty("dataBase", database);
        DATAINFO.setProperty("url", url);
        DATAINFO.setProperty("hibernate.dialect", hibernateDialect);
        DATAINFO.setProperty("driver", driver);

    }

    private void copyBaseToDest(ResourceLoader resourceLoader) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        Resource[] resources;
        try {
            /*
             * Use classpath* to search for resources that match this pattern in ALL of the jars in the application
             * class path. See:
             * http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/resources
             * .html#resources-classpath-wildcards
             */
            resources = resolver.getResources("classpath*:properties/xslt/*.xsl");

        } catch (IOException ioe) {
            logger.debug(ioe.getMessage(), ioe);
            throw new OpenClinicaSystemException("Unable to read source files", ioe);
        }

        File dest = new File(getField("filePath") + "xslt");
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                throw new OpenClinicaSystemException("Copying files, Could not create direcotry: " + dest.getAbsolutePath() + ".");
            }
        }

        for (Resource r : resources) {
            File f = new File(dest, r.getFilename());
            try {

                FileOutputStream out = new FileOutputStream(f);
                IOUtils.copy(r.getInputStream(), out);
                out.close();

            } catch (IOException ioe) {
                logger.debug(ioe.getMessage(), ioe);
                throw new OpenClinicaSystemException("Unable to copy file: " + r.getFilename() + " to " + f.getAbsolutePath(), ioe);

            }
        }
    }

    private void copyConfig() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        Resource[] resources = null;
        FileOutputStream out = null;
        Resource resource1 = null;
        Resource resource2 = null;

        resource1 = resolver.getResource("classpath:datainfo.properties");
        resource2 = resolver.getResource("classpath:extract.properties");

        String filePath = "$catalina.home/$WEBAPP.lower.config";

        filePath = replaceWebapp(filePath);
        filePath = replaceCatHome(filePath);

        File dest = new File(filePath);
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                throw new OpenClinicaSystemException("Copying files, Could not create directory: " + dest.getAbsolutePath() + ".");
            }
        }

        File f1 = new File(dest, resource1.getFilename());
        File f2 = new File(dest, resource2.getFilename());
        if (!f1.exists()) {
            out = new FileOutputStream(f1);
            IOUtils.copy(resource1.getInputStream(), out);
            out.close();
        }
        if (!f2.exists()) {
            out = new FileOutputStream(f2);
            IOUtils.copy(resource2.getInputStream(), out);
            out.close();
        }

        /*
         * 
         * for (Resource r: resources) { File f = new File(dest, r.getFilename()); if(!f.exists()){ out = new
         * FileOutputStream(f); IOUtils.copy(r.getInputStream(), out); out.close(); } }
         */
    }

    /**
     * @deprecated. ByteArrayInputStream keeps the whole file in memory needlessly. Use Commons IO's
     *              {@link IOUtils#copy(java.io.InputStream, java.io.OutputStream)} instead.
     */
    @Deprecated
    private void copyFiles(ByteArrayInputStream fis, File dest) {
        FileOutputStream fos = null;
        byte[] buffer = new byte[512]; // Buffer 4K at a time (you can change this).
        int bytesRead;
        logger.debug("fis?" + fis);
        try {
            fos = new FileOutputStream(dest);
            while ((bytesRead = fis.read(buffer)) >= 0) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException ioe) {// error while copying files
            OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to copy file: " + fis + "to" + dest.getAbsolutePath() + "."
                    + dest.getAbsolutePath() + ".");
            oe.initCause(ioe);
            oe.setStackTrace(ioe.getStackTrace());
            throw oe;
        } finally { // Ensure that the files are closed (if they were open).
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                    OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to copy file: " + fis + "to" + dest.getAbsolutePath() + "."
                            + dest.getAbsolutePath() + ".");
                    oe.initCause(ioe);
                    oe.setStackTrace(ioe.getStackTrace());
                    logger.debug(ioe.getMessage());
                    throw oe;

                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to copy file: " + fis + "to" + dest.getAbsolutePath() + "."
                            + dest.getAbsolutePath() + ".");
                    oe.initCause(ioe);
                    oe.setStackTrace(ioe.getStackTrace());
                    logger.debug(ioe.getMessage());
                    throw oe;

                }
            }
        }
    }

    private void copyODMMappingXMLtoResources(ResourceLoader resourceLoader) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        String[] fileNames = { "cd_odm_mapping.xml" };
        Resource[] resources;
        try {
            resources = resolver.getResources("classpath*:properties/cd_odm_mapping.xml");
        } catch (IOException ioe) {
            OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to read source files");
            oe.initCause(ioe);
            oe.setStackTrace(ioe.getStackTrace());
            logger.debug(ioe.getMessage());
            throw oe;
        }

        File dest = null;
        try {
            dest = new File(getField("filePath"));
            if (!dest.exists()) {
                if (!dest.mkdirs()) {
                    throw new OpenClinicaSystemException("Copying files, Could not create direcotry: " + dest.getAbsolutePath() + ".");
                }
            }
            File f = new File(dest, resources[0].getFilename());
            FileOutputStream out = new FileOutputStream(f);
            IOUtils.copy(resources[0].getInputStream(), out);
            out.close();

        } catch (IOException ioe) {
            OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to get web app base path");
            oe.initCause(ioe);
            oe.setStackTrace(ioe.getStackTrace());
            throw oe;
        }

    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public InputStream getInputStream(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:properties/" + fileName).getInputStream();
    }

    /**
     * @deprecated Use {@link #getFile(String,String)} instead
     */
    @Deprecated
    public File getFile(String fileName) {
        return getFile(fileName, "filePath");
    }

    public File getFile(String fileName, String relDirectory) {
        try {

            InputStream inputStream = getInputStream(fileName);

            File f = new File(getField("filePath") + relDirectory + fileName);

            /*
             * OutputStream outputStream = new FileOutputStream(f); byte buf[] = new byte[1024]; int len; try { while
             * ((len = inputStream.read(buf)) > 0) outputStream.write(buf, 0, len); } finally { outputStream.close();
             * inputStream.close(); }
             */
            return f;

        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }
    }

    public static String getDBName() {
        if (null == DB_NAME)
            return "postgres";
        return DB_NAME;
    }

    public static String getField(String key) {
        String value = DATAINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return value == null ? "" : value;
    }
    public Properties getDataInfo() {
        return DATAINFO;
    }

    public void setDataInfo(Properties dataInfo) {
        this.dataInfo = dataInfo;
        DATAINFO = dataInfo;
    }

    public void setExtractInfo(Properties extractInfo) {
        this.extractInfo = extractInfo;
    }

}

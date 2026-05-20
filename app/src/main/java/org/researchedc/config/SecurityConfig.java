package org.researchedc.config;

import javax.sql.DataSource;

import org.researchedc.core.CRFLocker;
import org.researchedc.core.OpenClinicaPasswordEncoder;
import org.researchedc.core.SecurityManager;
import org.researchedc.dao.hibernate.AuditUserLoginDao;
import org.researchedc.dao.hibernate.ConfigurationDao;
import org.researchedc.dao.hibernate.RuleSetRuleDao;
import org.researchedc.dao.hibernate.StudyDao;
import org.researchedc.domain.xform.XformParser;
import org.researchedc.service.extract.GenerateClinicalDataService;
import org.researchedc.web.filter.ApiSecurityFilter;
import org.researchedc.web.filter.OpenClinicaJdbcService;
import org.researchedc.web.filter.OpenClinicaLdapAuthoritiesPopulator;
import org.researchedc.web.filter.OpenClinicaSecurityContextLogoutHandler;
import org.researchedc.web.filter.OpenClinicaSessionRegistryImpl;
import org.researchedc.web.filter.OpenClinicaUsernamePasswordAuthenticationFilter;
import org.researchedc.web.pform.OpenRosaServices;
import org.researchedc.web.restful.ClinicalDataCollectorResource;
import org.researchedc.web.restful.MetadataCollectorResource;
import org.researchedc.web.restful.ODMClinicaDataResource;
import org.researchedc.web.restful.ODMMetadataRestResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.session.ConcurrentSessionFilter;

/**
 * Migrated from applicationContext-core-security.xml + applicationContext-security.xml.
 * Replaces legacy Spring Security 3.x XML with Java Config for Spring Security 6.x.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${ldap.host:ldap://localhost:389}")
    private String ldapHost;

    @Value("${ldap.userDn:}")
    private String ldapUserDn;

    @Value("${ldap.password:}")
    private String ldapPassword;

    @Value("${ldap.userSearchBase:}")
    private String ldapUserSearchBase;

    @Value("${ldap.userSearchFilter:}")
    private String ldapUserSearchFilter;

    @Value("${ldap.enabled:false}")
    private boolean ldapEnabled;

    /* ---- Password Encoders ---- */

    @Bean("sha256PasswordEncoder")
    public PasswordEncoder sha256PasswordEncoder() {
        return new MessageDigestPasswordEncoder("SHA-256");
    }

    @Bean("bcryptPasswordEncoder")
    public PasswordEncoder bcryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean("openClinicaPasswordEncoder")
    public OpenClinicaPasswordEncoder openClinicaPasswordEncoder() {
        OpenClinicaPasswordEncoder encoder = new OpenClinicaPasswordEncoder();
        encoder.setCurrentPasswordEncoder(bcryptPasswordEncoder());
        encoder.setOldPasswordEncoder(sha256PasswordEncoder());
        return encoder;
    }

    /* ---- User Details Service ---- */

    @Bean("ocUserDetailsService")
    public OpenClinicaJdbcService ocUserDetailsService(DataSource dataSource) {
        OpenClinicaJdbcService service = new OpenClinicaJdbcService();
        service.setDataSource(dataSource);
        service.setUsersByUsernameQuery(
            "SELECT user_name,passwd,enabled,account_non_locked FROM user_account WHERE user_name = ?");
        return service;
    }

    /* ---- LDAP Configuration ---- */

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapHost);
        contextSource.setUserDn(ldapUserDn);
        contextSource.setPassword(ldapPassword);
        return contextSource;
    }

    @Bean
    public FilterBasedLdapUserSearch openClinicaLdapUserSearch() {
        return new FilterBasedLdapUserSearch(
            ldapUserSearchBase, ldapUserSearchFilter, contextSource());
    }

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider() {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource());
        authenticator.setUserSearch(openClinicaLdapUserSearch());
        return new LdapAuthenticationProvider(authenticator, new OpenClinicaLdapAuthoritiesPopulator());
    }

    /* ---- Authentication Manager ---- */

    @Bean("authenticationManager")
    public AuthenticationManager authenticationManager(
            OpenClinicaJdbcService ocUserDetailsService,
            OpenClinicaPasswordEncoder openClinicaPasswordEncoder) {

        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(ocUserDetailsService);
        daoProvider.setPasswordEncoder(openClinicaPasswordEncoder);

        if (ldapEnabled) {
            return new org.springframework.security.authentication.ProviderManager(
                daoProvider, ldapAuthenticationProvider());
        }
        return new org.springframework.security.authentication.ProviderManager(daoProvider);
    }

    /* ---- SecurityManager (password generation & verification) ---- */

    @Bean("securityManager")
    public SecurityManager securityManager(
            OpenClinicaPasswordEncoder openClinicaPasswordEncoder,
            OpenClinicaJdbcService ocUserDetailsService) {

        SecurityManager manager = new SecurityManager();
        manager.setEncoder(openClinicaPasswordEncoder);

        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(ocUserDetailsService);
        daoProvider.setPasswordEncoder(openClinicaPasswordEncoder);

        if (ldapEnabled) {
            manager.setProviders(new org.springframework.security.authentication.AuthenticationProvider[]{
                ldapAuthenticationProvider(), daoProvider});
        } else {
            manager.setProviders(new org.springframework.security.authentication.AuthenticationProvider[]{
                daoProvider});
        }
        return manager;
    }

    /* ---- XformParser (was in core-security.xml) ---- */

    @Bean
    public XformParser xformParser(DataSource dataSource, org.researchedc.dao.core.CoreResources coreResources) {
        XformParser parser = new XformParser();
        parser.setDataSource(dataSource);
        parser.setCoreResources(coreResources);
        return parser;
    }

    /* ---- API Security Filter ---- */

    @Bean
    public ApiSecurityFilter apiSecurityFilter() {
        return new ApiSecurityFilter();
    }

    /* ---- Session Management ---- */

    @Bean("sessionRegistry")
    public OpenClinicaSessionRegistryImpl sessionRegistry(
            AuditUserLoginDao auditUserLoginDao, DataSource dataSource, CRFLocker crfLocker) {
        OpenClinicaSessionRegistryImpl registry = new OpenClinicaSessionRegistryImpl();
        registry.setAuditUserLoginDao(auditUserLoginDao);
        registry.setDataSource(dataSource);
        registry.setCrfLocker(crfLocker);
        return registry;
    }

    @Bean("sas")
    public ConcurrentSessionControlAuthenticationStrategy sessionAuthenticationStrategy(
            SessionRegistry sessionRegistry) {
        ConcurrentSessionControlAuthenticationStrategy strategy =
            new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        strategy.setMaximumSessions(1);
        return strategy;
    }

    /* ---- Authentication Filter & Handlers ---- */

    @Bean("authenticationProcessingFilterEntryPoint")
    public LoginUrlAuthenticationEntryPoint authenticationProcessingFilterEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/pages/login/login");
    }

    @Bean("successHandler")
    public SavedRequestAwareAuthenticationSuccessHandler successHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/MainMenu");
        return handler;
    }

    @Bean("failureHandler")
    public ExceptionMappingAuthenticationFailureHandler failureHandler() {
        ExceptionMappingAuthenticationFailureHandler handler = new ExceptionMappingAuthenticationFailureHandler();
        handler.setDefaultFailureUrl("/pages/login/login?action=errorLogin");
        handler.setExceptionMappings(java.util.Map.of(
            "org.springframework.security.authentication.LockedException",
            "/pages/login/login?action=errorLocked"));
        return handler;
    }

    @Bean("myFilter")
    public OpenClinicaUsernamePasswordAuthenticationFilter myFilter(
            AuthenticationManager authenticationManager,
            SavedRequestAwareAuthenticationSuccessHandler successHandler,
            ExceptionMappingAuthenticationFailureHandler failureHandler,
            AuditUserLoginDao auditUserLoginDao,
            ConfigurationDao configurationDao,
            DataSource dataSource,
            SessionRegistry sessionRegistry,
            CRFLocker crfLocker) {

        OpenClinicaUsernamePasswordAuthenticationFilter filter = new OpenClinicaUsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);
        filter.setAuditUserLoginDao(auditUserLoginDao);
        filter.setConfigurationDao(configurationDao);
        filter.setDataSource(dataSource);
        filter.setAllowSessionCreation(true);
        filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy(sessionRegistry));
        filter.setCrfLocker(crfLocker);
        return filter;
    }

    @Bean("concurrencyFilter")
    public ConcurrentSessionFilter concurrencyFilter(SessionRegistry sessionRegistry) {
        return new ConcurrentSessionFilter(sessionRegistry, "/MainMenu");
    }

    /* ---- Logout Handler ---- */

    @Bean("openClinicaLogoutHandler")
    public OpenClinicaSecurityContextLogoutHandler openClinicaLogoutHandler(
            AuditUserLoginDao auditUserLoginDao, DataSource dataSource) {
        OpenClinicaSecurityContextLogoutHandler handler = new OpenClinicaSecurityContextLogoutHandler();
        handler.setAuditUserLoginDao(auditUserLoginDao);
        handler.setDataSource(dataSource);
        return handler;
    }

    /* ---- API Security Filter Chain (JWT / OAuth2 Resource Server) ---- */

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        // @formatter:on

        return http.build();
    }

    /* ---- Web Security Filter Chain (OIDC Login + Form Login + Session) ---- */

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(
            HttpSecurity http,
            OpenClinicaUsernamePasswordAuthenticationFilter myFilter,
            ConcurrentSessionFilter concurrencyFilter,
            OpenClinicaSecurityContextLogoutHandler openClinicaLogoutHandler,
            SessionRegistry sessionRegistry) throws Exception {

        // @formatter:off
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/pages/login/login",
                    "/SystemStatus",
                    "/RssReader",
                    "/RequestPassword",
                    "/RequestAccount",
                    "/includes/**",
                    "/images/**",
                    "/help/**",
                    "/ws/**",
                    "/Contact",
                    "/rest2/openrosa/**",
                    "/pages/odmk/**",
                    "/pages/openrosa/**",
                    "/pages/accounts/**",
                    "/pages/itemdata/**",
                    "/pages/auth/api/v1/studies/**",
                    "/pages/odmss/**",
                    "/pages/healthcheck/**",
                    "/pages/api/v1/anonymousform/**",
                    "/pages/api/v2/anonymousform/**",
                    "/pages/api/v1/editform/**",
                    "/pages/auth/api/v1/discrepancynote/**",
                    "/pages/auth/api/v1/forms/migrate/**",
                    "/pages/api/v1/forms/migrate/**",
                    "/pages/auth/api/**",
                    "/pages/auth/api/v1/system/**",
                    "/app/**",
                    "/q/**",
                    "/api/**",
                    "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .anonymous(anonymous -> {})
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/pages/login/login")
                .successHandler(new OidcSessionBridgeSuccessHandler())
            )
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.migrateSession())
                .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                    .sessionRegistry(sessionRegistry)
            )
            .addFilterAt(myFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(concurrencyFilter, org.springframework.security.web.session.ConcurrentSessionFilter.class)
            .logout(logout -> logout
                .logoutUrl("/j_spring_security_logout")
                .logoutSuccessUrl("/MainMenu")
                .addLogoutHandler(openClinicaLogoutHandler)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationProcessingFilterEntryPoint())
            )
            .httpBasic(httpBasic -> {})
        ;
        // @formatter:on

        return http.build();
    }

    /* ---- REST Resources (were in applicationContext-security.xml) ---- */

    @Bean("metadataCollectorResource")
    public MetadataCollectorResource metadataCollectorResource(
            DataSource dataSource,
            RuleSetRuleDao ruleSetRuleDao,
            org.researchedc.dao.core.CoreResources coreResources,
            StudyDao studyDaoDomain) {
        MetadataCollectorResource resource = new MetadataCollectorResource();
        resource.setDataSource(dataSource);
        resource.setRuleSetRuleDao(ruleSetRuleDao);
        resource.setCoreResources(coreResources);
        resource.setStudyDaoHib(studyDaoDomain);
        return resource;
    }

    @Bean("odmMetadataRestResource")
    public ODMMetadataRestResource odmMetadataRestResource(MetadataCollectorResource metadataCollectorResource) {
        ODMMetadataRestResource resource = new ODMMetadataRestResource();
        resource.setMetadataCollectorResource(metadataCollectorResource);
        return resource;
    }

    @Bean("clinicalDataCollectorResource")
    public ClinicalDataCollectorResource clinicalDataCollectorResource(
            GenerateClinicalDataService generateClinicalDataService) {
        ClinicalDataCollectorResource resource = new ClinicalDataCollectorResource();
        resource.setGenerateClinicalDataService(generateClinicalDataService);
        return resource;
    }

    @Bean("odmClinicalDataRestResource")
    public ODMClinicaDataResource odmClinicalDataRestResource(
            ClinicalDataCollectorResource clinicalDataCollectorResource,
            MetadataCollectorResource metadataCollectorResource,
            DataSource dataSource) {
        ODMClinicaDataResource resource = new ODMClinicaDataResource();
        resource.setClinicalDataCollectorResource(clinicalDataCollectorResource);
        resource.setMetadataCollectorResource(metadataCollectorResource);
        resource.setDataSource(dataSource);
        return resource;
    }

    @Bean("openRosaServices")
    public OpenRosaServices openRosaServices(
            DataSource dataSource,
            org.researchedc.dao.core.CoreResources coreResources,
            org.researchedc.dao.hibernate.RuleActionPropertyDao ruleActionPropertyDao,
            org.researchedc.controller.openrosa.OpenRosaSubmissionController openRosaSubmissionController) {
        OpenRosaServices services = new OpenRosaServices();
        services.setDataSource(dataSource);
        services.setCoreResources(coreResources);
        services.setRuleActionPropertyDao(ruleActionPropertyDao);
        services.setOpenRosaSubmissionController(openRosaSubmissionController);
        return services;
    }

    @Bean("openRosaSubmissionController")
    public org.researchedc.controller.openrosa.OpenRosaSubmissionController openRosaSubmissionController() {
        return new org.researchedc.controller.openrosa.OpenRosaSubmissionController();
    }
}

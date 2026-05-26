# ResearchEDC 内置登录认证模块落地方案（方案 A）

生成日期：2026-05-25  
适用项目：OpenClinica 改造版 / ResearchEDC  
方案定位：不依赖 Keycloak、LDAP、外部 SSO 或云端 IAM，在系统内部实现足够安全的登录、会话、权限、审计与账户变更审批机制。

---

## 1. 目标与边界

本方案用于在 ResearchEDC 中增加一个可落地的内置登录认证模块。目标不是单纯增加一个登录页面，而是建立完整的账户安全基础设施，包括：

1. 用户登录与登出；
2. 服务端 Session 会话管理；
3. 密码安全存储；
4. 登录失败限制与账户锁定；
5. 基于角色的权限控制；
6. 系统管理员审核敏感账户信息变更；
7. 审计日志；
8. 后续支持电子签名、数据锁定、数据导出审批和多研究项目权限隔离。

本方案不使用 Keycloak，不依赖外部认证服务。所有用户、角色、权限、会话和审批记录均由 ResearchEDC 自身管理。

---

## 2. 推荐技术选择

### 2.1 后端

推荐使用：

```text
Spring Boot 3.x
Spring Security 6.x
Spring Data JPA
PostgreSQL
Flyway / Liquibase
Spring Session JDBC 或默认 HttpSession
```

如果项目短期仍保留旧 Java 技术栈，也可以先以 Spring Security 为核心逐步接入。但从长期重构角度看，更推荐迁移到 Spring Boot 3.x + Spring Security 6.x。

### 2.2 前端

可根据当前项目实际情况选择：

```text
方案 1：Thymeleaf / 服务端模板页面
方案 2：React / Vue / Vite 前端 + 后端 Session Cookie
```

如果项目正在前端现代化，推荐使用 React / Vite，但认证态仍由后端 Session 和 HttpOnly Cookie 管理。

### 2.3 不推荐路线

不推荐：

```text
纯前端登录页 + localStorage JWT
简单 username/password 表
前端控制菜单作为唯一权限控制
无审计日志
无登录失败限制
管理员能看到用户明文密码
用户自行修改邮箱、密码而无审核
```

---

## 3. 总体架构

推荐架构如下：

```text
Browser
  |
  |  HTTPS
  v
ResearchEDC Web Frontend
  |
  |  SameSite + HttpOnly + Secure Session Cookie
  v
ResearchEDC Backend
  |
  |-- Spring Security
  |-- AuthController
  |-- AccountController
  |-- AdminUserController
  |-- AccountChangeApprovalService
  |-- AuditLogService
  |-- RBAC Permission Service
  |
  v
PostgreSQL
  |
  |-- users
  |-- roles
  |-- permissions
  |-- user_roles
  |-- role_permissions
  |-- login_attempts
  |-- audit_logs
  |-- account_change_requests
  |-- password_history
  |-- password_change_tickets
  |-- spring_session / server sessions
```

核心原则：

1. 登录认证由 Spring Security 处理；
2. 认证状态保存在服务端 Session；
3. 浏览器只保存 Session Cookie；
4. Cookie 必须设置 `HttpOnly`、`Secure`、`SameSite`；
5. 后端 API 必须做权限校验；
6. 敏感账户信息变更必须进入管理员审批流；
7. 任何账户、权限和数据相关关键行为必须写入审计日志。

---

## 4. 会话与登录模式

### 4.1 推荐模式

推荐采用：

```text
Form Login + Server-side Session + HttpOnly Cookie
```

而不是：

```text
JWT + localStorage
```

原因：

1. 研究数据系统更适合服务端集中控制会话；
2. 服务端可以强制踢出用户、撤销会话、限制并发登录；
3. Cookie 设置为 HttpOnly 后，前端 JavaScript 无法读取；
4. 对传统 Web 系统、EDC 系统和内网部署环境更稳。

### 4.2 Cookie 建议配置

生产环境建议：

```properties
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
server.servlet.session.timeout=30m
```

如果系统部署在内网且暂时没有 HTTPS，`secure=true` 在纯 HTTP 下会导致 Cookie 不发送。因此生产环境仍建议配置 HTTPS，即使是内网环境，也应使用自签或内网 CA 证书。

### 4.3 Session 生命周期

建议规则：

```text
普通用户 Session 有效期：30 min 不活动自动过期
管理员 Session 有效期：15 min 不活动自动过期
登录成功后轮换 Session ID
登出后立即失效 Session
密码变更后撤销该用户其他全部 Session
账户禁用后撤销该用户全部 Session
角色权限变更后撤销该用户全部 Session 或要求重新登录
```

---

## 5. 用户、角色和权限设计

### 5.1 初始角色

建议内置以下角色：

```text
system_admin      系统管理员
study_admin       研究管理员
investigator      研究者
coordinator       研究协调员
data_entry        数据录入员
monitor           监察员 / 质控员
statistician      统计人员
read_only         只读用户
```

### 5.2 权限粒度

建议先使用 RBAC，即角色绑定权限，用户绑定角色。

初始权限建议：

```text
user:create
user:read
user:update
user:disable
user:approve_account_change

role:create
role:read
role:update
role:assign

study:create
study:read
study:update
study:archive

subject:create
subject:read
subject:update
subject:delete

crf:enter
crf:read
crf:update
crf:verify
crf:lock
crf:unlock

randomization:execute
randomization:read

survey:create
survey:read
survey:update
survey:publish

data:export
data:import

audit:read
system:configure
```

### 5.3 推荐权限控制方式

后端接口必须使用方法级权限控制，例如：

```java
@PreAuthorize("hasAuthority('user:read')")
@GetMapping("/admin/users")
public Page<UserDto> listUsers(...) {
    ...
}
```

不要仅依赖前端隐藏按钮或菜单。

---

## 6. 数据库表设计

以下为推荐基础表结构。实际开发时可使用 Flyway 或 Liquibase 管理版本。

### 6.1 users

```sql
create table users (
    id uuid primary key,
    username varchar(100) not null unique,
    email varchar(255),
    display_name varchar(100),
    password_hash text not null,

    status varchar(30) not null default 'active',
    -- active, locked, disabled, pending

    failed_login_count int not null default 0,
    locked_until timestamp,
    must_change_password boolean not null default false,

    last_login_at timestamp,
    last_login_ip varchar(64),

    created_at timestamp not null,
    updated_at timestamp not null,
    created_by uuid,
    updated_by uuid
);
```

建议登录主标识使用 `username`，不要强依赖 `email`。在不可用或半离线环境中，邮箱可能只是联系人字段，不一定能用于邮件验证和找回密码。

### 6.2 roles

```sql
create table roles (
    id uuid primary key,
    code varchar(100) not null unique,
    name varchar(100) not null,
    description text,
    system_role boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp not null
);
```

### 6.3 permissions

```sql
create table permissions (
    id uuid primary key,
    code varchar(100) not null unique,
    name varchar(100) not null,
    description text,
    created_at timestamp not null
);
```

### 6.4 user_roles

```sql
create table user_roles (
    user_id uuid not null references users(id),
    role_id uuid not null references roles(id),
    created_at timestamp not null,
    created_by uuid,
    primary key (user_id, role_id)
);
```

### 6.5 role_permissions

```sql
create table role_permissions (
    role_id uuid not null references roles(id),
    permission_id uuid not null references permissions(id),
    created_at timestamp not null,
    created_by uuid,
    primary key (role_id, permission_id)
);
```

### 6.6 login_attempts

```sql
create table login_attempts (
    id uuid primary key,
    username varchar(100),
    user_id uuid,
    success boolean not null,
    failure_reason varchar(100),
    ip_address varchar(64),
    user_agent text,
    created_at timestamp not null
);
```

### 6.7 audit_logs

```sql
create table audit_logs (
    id uuid primary key,
    actor_user_id uuid,
    action varchar(100) not null,
    resource_type varchar(100),
    resource_id varchar(100),
    ip_address varchar(64),
    user_agent text,
    before_json jsonb,
    after_json jsonb,
    reason text,
    created_at timestamp not null
);
```

必须记录的事件：

```text
登录成功
登录失败
登出
账户锁定
账户解锁
用户创建
用户禁用
用户恢复启用
角色分配
权限变更
密码变更申请
密码变更审批
密码变更完成
邮箱变更申请
邮箱变更审批
邮箱变更完成
管理员重置密码
数据导出
审计日志查看
```

### 6.8 account_change_requests

该表用于管理员审批敏感账户信息变更。

```sql
create table account_change_requests (
    id uuid primary key,
    target_user_id uuid not null references users(id),
    request_type varchar(50) not null,
    -- PASSWORD_CHANGE, EMAIL_CHANGE, ADMIN_PASSWORD_RESET, PROFILE_CHANGE

    requested_by uuid not null references users(id),
    reviewed_by uuid references users(id),

    status varchar(30) not null,
    -- pending, approved, rejected, cancelled, expired, completed

    old_value text,
    new_value text,

    reason text,
    review_comment text,

    requested_at timestamp not null,
    reviewed_at timestamp,
    expires_at timestamp,
    completed_at timestamp
);
```

注意：如果是密码变更，`new_value` 不得保存新密码，也不得保存新密码 hash。密码变更请求只保存请求元数据，不保存用户希望设置的新密码。

### 6.9 password_change_tickets

管理员批准密码变更后，系统生成一次性改密票据。

```sql
create table password_change_tickets (
    id uuid primary key,
    user_id uuid not null references users(id),
    request_id uuid references account_change_requests(id),

    ticket_hash text not null,
    status varchar(30) not null,
    -- active, used, expired, revoked

    expires_at timestamp not null,
    used_at timestamp,
    created_at timestamp not null,
    created_by uuid not null
);
```

票据明文只在生成时返回一次，数据库只保存 hash。

### 6.10 password_history

用于阻止用户重复使用近期密码。

```sql
create table password_history (
    id uuid primary key,
    user_id uuid not null references users(id),
    password_hash text not null,
    created_at timestamp not null
);
```

建议至少保存最近 5 次密码 hash，用于禁止重复使用。

---

## 7. 管理员审核敏感账户信息变更

用户要求：修改密码、邮箱等敏感账户信息应当由系统管理员审核。  
本方案按该要求设计。

### 7.1 敏感字段定义

建议将以下字段定义为敏感账户信息：

```text
password
email
username
display_name
phone
MFA 绑定状态
角色
账户状态
所属研究项目
```

第一阶段至少实现：

```text
密码变更审批
邮箱变更审批
角色变更审计
账户禁用 / 解锁审计
```

### 7.2 密码变更审批流程

推荐流程如下：

```text
1. 用户登录系统。
2. 用户进入“账户安全”页面。
3. 用户点击“申请修改密码”。
4. 系统要求用户输入当前密码完成再认证。
5. 系统创建 PASSWORD_CHANGE 请求，状态为 pending。
6. 系统管理员在后台查看待审批请求。
7. 管理员批准或拒绝。
8. 若批准，系统生成一次性改密票据。
9. 用户使用票据进入改密页面。
10. 用户输入新密码。
11. 系统校验密码强度、密码历史、票据有效期。
12. 系统保存新密码 hash。
13. 系统标记请求 completed。
14. 系统撤销该用户其他会话。
15. 系统写入审计日志。
```

关键要求：

```text
管理员不应看到用户的新密码。
数据库不应保存用户拟设置的新密码。
审批记录只保存“谁申请、谁审核、何时审核、审核意见、请求状态”。
新密码只在最终改密接口中接收，并立即 hash。
```

### 7.3 管理员重置密码流程

适用于用户忘记密码或账户无法登录。

推荐流程：

```text
1. 用户线下联系系统管理员。
2. 管理员确认用户身份。
3. 管理员在后台发起 ADMIN_PASSWORD_RESET。
4. 系统生成一次性临时密码或一次性改密票据。
5. 管理员将临时凭据通过线下安全方式交给用户。
6. 用户首次登录后必须修改密码。
7. 系统将 must_change_password 设置为 true。
8. 用户完成改密后，系统清除 must_change_password。
9. 系统写入完整审计日志。
```

不推荐：

```text
管理员直接设置用户长期密码
管理员通过聊天工具发送长期密码
系统保存明文临时密码
多个管理员共用一个账户
```

### 7.4 邮箱变更审批流程

如果环境支持邮件服务，建议流程：

```text
1. 用户提交新邮箱。
2. 系统创建 EMAIL_CHANGE 请求，状态 pending。
3. 管理员审核申请。
4. 管理员批准后，系统向新邮箱发送验证链接。
5. 用户完成邮箱验证。
6. 系统更新 users.email。
7. 系统写入审计日志。
```

如果环境不支持邮件服务，建议流程：

```text
1. 用户提交新邮箱。
2. 系统创建 EMAIL_CHANGE 请求，状态 pending。
3. 管理员线下确认用户身份和邮箱归属。
4. 管理员批准后，系统直接更新 users.email。
5. 系统写入审计日志。
```

建议将邮箱作为联系人字段，而不是唯一登录主标识。登录主标识仍使用 `username`。

### 7.5 审批权限

只有具备以下权限的用户可以审批：

```text
user:approve_account_change
```

建议限制为：

```text
system_admin
```

如后续用于正式临床研究环境，可增加双人审批：

```text
system_admin 发起
另一个 system_admin 复核
```

第一阶段可以先采用单管理员审批，但审计日志必须完整。

---

## 8. Spring Security 关键配置

### 8.1 依赖

Gradle 示例：

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    implementation 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

### 8.2 SecurityFilterChain 示例

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login",
                    "/assets/**",
                    "/error"
                ).permitAll()
                .requestMatchers("/admin/**").hasAuthority("system:admin")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .csrf(csrf -> {
                // 默认启用。除非是明确的 stateless API，否则不要关闭。
            });

        return http.build();
    }
}
```

说明：

1. `sessionFixation().migrateSession()` 用于登录后轮换 Session ID；
2. `maximumSessions(1)` 可限制同一账户并发会话数量；
3. CSRF 不应随意关闭；
4. `/admin/**` 只是示例，实际应细化到具体权限。

### 8.3 PasswordEncoder

推荐使用 Spring Security 的 `DelegatingPasswordEncoder`：

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

默认新密码通常会使用 bcrypt。若团队明确希望使用 Argon2id，可显式配置 Argon2PasswordEncoder，但需要确认运行环境依赖和性能。

### 8.4 UserDetailsService

```java
@Service
public class ResearchEdcUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ResearchEdcUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        if ("disabled".equals(user.getStatus())) {
            throw new DisabledException("Account disabled");
        }

        if ("locked".equals(user.getStatus())) {
            throw new LockedException("Account locked");
        }

        Collection<GrantedAuthority> authorities = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
            .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPasswordHash(),
            authorities
        );
    }
}
```

注意错误提示不要暴露“用户名不存在”或“密码错误”的差异，前端统一显示：

```text
用户名或密码错误。
```

---

## 9. 后端接口设计

### 9.1 登录相关

```text
GET  /login
POST /login
POST /logout
GET  /me
```

说明：

1. `/login` 可由 Spring Security 接管；
2. `/me` 返回当前用户基本信息、角色和权限；
3. `/logout` 必须使用 POST，避免 CSRF 风险。

### 9.2 用户自助账户接口

```text
GET  /account/profile
POST /account/password-change-requests
POST /account/email-change-requests
GET  /account/change-requests
POST /account/password-change-tickets/{ticket}/complete
```

### 9.3 管理员接口

```text
GET  /admin/users
POST /admin/users
GET  /admin/users/{id}
PATCH /admin/users/{id}/status
POST /admin/users/{id}/roles
DELETE /admin/users/{id}/roles/{roleId}

GET  /admin/account-change-requests
GET  /admin/account-change-requests/{id}
POST /admin/account-change-requests/{id}/approve
POST /admin/account-change-requests/{id}/reject

POST /admin/users/{id}/password-reset
GET  /admin/audit-logs
```

### 9.4 接口权限建议

```text
/admin/users                       user:read
/admin/users POST                  user:create
/admin/users/{id}/status           user:update
/admin/users/{id}/roles            role:assign
/admin/account-change-requests     user:approve_account_change
/admin/audit-logs                  audit:read
```

---

## 10. 登录失败限制与账户锁定

建议规则：

```text
连续失败 5 次：锁定 15 分钟
连续失败 10 次：锁定 60 分钟，并提示联系管理员
管理员可手动解锁
所有失败登录记录写入 login_attempts
```

实现位置：

```text
AuthenticationFailureHandler
AuthenticationSuccessHandler
LoginAttemptService
```

登录成功后：

```text
failed_login_count 清零
locked_until 清空
last_login_at 更新
last_login_ip 更新
写入 audit_logs
```

登录失败后：

```text
failed_login_count + 1
必要时设置 locked_until
写入 login_attempts
写入 audit_logs
```

---

## 11. 前端页面设计

### 11.1 登录页

页面字段：

```text
用户名
密码
登录按钮
错误提示
```

错误提示统一：

```text
用户名或密码错误，或账户暂时不可用。
```

不要提示：

```text
用户不存在
密码错误
账户存在但已锁定
```

这样可以降低用户枚举风险。

### 11.2 账户安全页

功能：

```text
查看用户名
查看邮箱
申请修改密码
申请修改邮箱
查看账户变更申请状态
查看最近登录时间
```

### 11.3 管理员账户审批页

列表字段：

```text
申请人
目标用户
申请类型
原值
新值
申请原因
申请时间
状态
操作
```

密码变更请求不显示新密码，也不应该存在新密码。

### 11.4 审计日志页

筛选条件：

```text
用户
操作类型
资源类型
时间范围
IP 地址
```

导出审计日志需要单独权限：

```text
audit:read
data:export
```

---

## 12. 密码策略

建议第一版规则：

```text
最少 12 位
必须包含字母和数字
建议包含特殊字符
禁止与用户名相同
禁止使用最近 5 次密码
管理员重置后首次登录必须改密
密码变更后撤销其他会话
```

如需要更严格：

```text
最少 14 位
检查常见弱密码字典
定期检查长期未修改密码账户
管理员账户强制更高复杂度
```

是否强制 90 天换密需要谨慎。对于科研系统，如果强制频繁换密，用户可能反而使用弱密码或重复密码。更建议采用强密码 + 异常行为再认证 + 管理员审批。

---

## 13. 审计日志策略

审计日志应满足：

```text
不可由普通用户修改
普通管理员也不应直接删除
关键字段记录 before_json 和 after_json
记录 actor_user_id
记录 ip_address
记录 user_agent
记录 created_at
```

建议将以下动作做成统一枚举：

```text
AUTH_LOGIN_SUCCESS
AUTH_LOGIN_FAILURE
AUTH_LOGOUT
AUTH_ACCOUNT_LOCKED
AUTH_PASSWORD_CHANGE_REQUESTED
AUTH_PASSWORD_CHANGE_APPROVED
AUTH_PASSWORD_CHANGE_REJECTED
AUTH_PASSWORD_CHANGED
AUTH_ADMIN_PASSWORD_RESET
AUTH_EMAIL_CHANGE_REQUESTED
AUTH_EMAIL_CHANGE_APPROVED
AUTH_EMAIL_CHANGE_REJECTED
AUTH_EMAIL_CHANGED
USER_CREATED
USER_DISABLED
USER_ENABLED
USER_ROLE_ASSIGNED
USER_ROLE_REMOVED
AUDIT_LOG_VIEWED
DATA_EXPORTED
```

---

## 14. 初始化管理员账户

首次部署时需要创建系统管理员。

推荐方案：

```text
1. 系统启动时检查是否存在 system_admin。
2. 如果不存在，从环境变量读取初始管理员用户名和临时密码。
3. 创建管理员账户。
4. 设置 must_change_password = true。
5. 首次登录后强制修改密码。
6. 修改完成后提示删除或更换环境变量。
```

环境变量示例：

```bash
RESEARCHEDC_BOOTSTRAP_ADMIN_USERNAME=admin
RESEARCHEDC_BOOTSTRAP_ADMIN_PASSWORD=change-me-immediately
```

不要在代码或 Git 仓库中写死初始密码。

---

## 15. 推荐代码模块结构

```text
src/main/java/org/researchedc/security
  SecurityConfig.java
  ResearchEdcUserDetailsService.java
  PasswordPolicyService.java
  LoginAttemptService.java
  AuditAuthenticationSuccessHandler.java
  AuditAuthenticationFailureHandler.java

src/main/java/org/researchedc/account
  AccountController.java
  AccountChangeRequestController.java
  AccountChangeApprovalService.java
  PasswordChangeTicketService.java
  PasswordHistoryService.java

src/main/java/org/researchedc/admin
  AdminUserController.java
  AdminRoleController.java
  AdminAccountChangeController.java

src/main/java/org/researchedc/audit
  AuditLog.java
  AuditLogRepository.java
  AuditLogService.java
  AuditAction.java

src/main/java/org/researchedc/user
  UserEntity.java
  RoleEntity.java
  PermissionEntity.java
  UserRepository.java
  RoleRepository.java
  PermissionRepository.java
```

---

## 16. 分阶段实施计划

### 阶段 0：安全基线确认

目标：明确是否采用 Spring Boot 3.x、PostgreSQL、Session Cookie。

任务：

```text
确认 Java / Spring Boot 版本
确认数据库版本
确认部署方式是否使用 Docker Compose
确认是否有 HTTPS
确认是否需要兼容旧用户表
确认前端是 Thymeleaf 还是 React / Vue
```

交付物：

```text
认证模块技术决策记录 ADR
数据库迁移策略
角色权限初版清单
```

### 阶段 1：基础登录与会话

任务：

```text
引入 Spring Security
实现 users 表
实现 UserDetailsService
实现 PasswordEncoder
实现登录页
实现登出
实现 /me
配置 Session Cookie
启用 CSRF
实现初始管理员账户
```

交付物：

```text
用户可以登录
用户可以登出
未登录用户无法访问受保护页面
管理员首次登录必须修改密码
```

### 阶段 2：RBAC 权限系统

任务：

```text
实现 roles
实现 permissions
实现 user_roles
实现 role_permissions
实现 @PreAuthorize 权限校验
实现管理员用户列表
实现角色分配
实现权限初始化脚本
```

交付物：

```text
不同角色看到不同功能
后端接口按权限拦截
普通用户无法访问管理员接口
```

### 阶段 3：账户变更审批

任务：

```text
实现 account_change_requests
实现 password_change_tickets
实现用户申请修改密码
实现管理员审批密码修改
实现用户完成密码修改
实现用户申请修改邮箱
实现管理员审批邮箱修改
实现管理员重置密码
```

交付物：

```text
用户不能直接修改密码
用户不能直接修改邮箱
密码和邮箱修改必须由系统管理员审核
管理员不能看到用户新密码
所有审批操作可追溯
```

### 阶段 4：审计与安全加固

任务：

```text
实现 audit_logs
记录登录成功 / 失败
记录登出
记录账户锁定
记录密码审批
记录邮箱审批
记录角色变更
实现登录失败限制
实现 Session 撤销
实现审计日志查询
实现安全测试用例
```

交付物：

```text
所有关键账户行为均可审计
登录攻击受到限制
密码修改后旧 Session 被撤销
管理员操作有完整痕迹
```

### 阶段 5：后续增强

可选增强：

```text
TOTP 双因素认证
双管理员审批
研究项目级权限隔离
电子签名
数据导出审批
审计日志防篡改归档
管理员操作二次确认
IP 白名单
内网 CA HTTPS
```

---

## 17. 测试用例清单

### 17.1 登录测试

```text
正确用户名密码可以登录
错误密码不能登录
不存在用户不能登录
禁用用户不能登录
锁定用户不能登录
登录失败达到阈值后锁定账户
登录成功后失败次数清零
```

### 17.2 Session 测试

```text
登录后 Session ID 发生变化
登出后旧 Session 不可用
Session 超时后需要重新登录
同一账户并发登录按配置处理
密码变更后其他 Session 失效
账户禁用后所有 Session 失效
```

### 17.3 权限测试

```text
普通用户不能访问 /admin/users
无 audit:read 权限不能查看审计日志
无 role:assign 权限不能分配角色
前端隐藏按钮但后端仍必须拦截
```

### 17.4 密码审批测试

```text
用户申请改密后状态为 pending
管理员拒绝后用户不能改密
管理员批准后生成一次性票据
票据过期后不能使用
票据使用一次后不能重复使用
新密码不符合策略时拒绝
新密码不能与最近 5 次密码相同
密码修改后其他 Session 失效
```

### 17.5 邮箱审批测试

```text
用户提交新邮箱后不会立即生效
管理员拒绝后邮箱不变
管理员批准后邮箱更新
审批日志记录 old_value 和 new_value
```

### 17.6 审计测试

```text
登录成功写入审计
登录失败写入审计
密码申请写入审计
密码审批写入审计
邮箱申请写入审计
邮箱审批写入审计
角色变更写入审计
审计日志查看写入审计
```

---

## 18. 安全注意事项

### 18.1 禁止明文密码

任何情况下不得：

```text
保存明文密码
通过接口返回密码
管理员查看用户密码
日志中记录密码
审批表中保存用户新密码
```

### 18.2 不要关闭 CSRF

如果使用服务端 Session Cookie，CSRF 防护应保持启用。除非某些接口是完全 stateless 且另有认证机制，否则不要全局关闭 CSRF。

### 18.3 不要暴露登录失败原因

错误提示统一即可：

```text
用户名或密码错误，或账户暂时不可用。
```

后台审计中可以记录真实原因：

```text
USER_NOT_FOUND
BAD_CREDENTIALS
ACCOUNT_LOCKED
ACCOUNT_DISABLED
PASSWORD_EXPIRED
```

### 18.4 管理员审批不等于管理员知道密码

管理员审核密码修改的含义是批准“允许该用户进入改密流程”，而不是管理员输入或查看用户新密码。

### 18.5 账户共享应被禁止

每个研究人员、录入人员、监察员都应有独立账户。共享账户会破坏审计追踪。

---

## 19. 推荐优先级

如果开发资源有限，建议优先级如下：

```text
P0
- Spring Security 登录
- PasswordEncoder
- Session Cookie
- CSRF
- users 表
- 初始管理员
- 基础审计日志

P1
- RBAC
- 登录失败限制
- 用户禁用 / 解锁
- 密码变更管理员审批
- 邮箱变更管理员审批

P2
- 密码历史
- Session 撤销
- 审计日志查询
- 管理员重置密码
- 数据导出审计

P3
- TOTP MFA
- 双管理员审批
- 电子签名
- 审计日志归档防篡改
```

---

## 20. 最小可用版本定义

第一版可定义为：

```text
1. 管理员可创建用户；
2. 用户可登录和登出；
3. 用户登录后可访问系统；
4. 未登录用户不能访问受保护页面；
5. 后端可区分管理员和普通用户；
6. 用户不能自行修改密码或邮箱；
7. 用户可提交密码或邮箱变更申请；
8. 系统管理员可批准或拒绝申请；
9. 密码修改过程中管理员不能看到新密码；
10. 登录、登出、密码审批、邮箱审批均有审计日志。
```

达到以上 10 点后，登录认证模块才可认为基本可用。

---

## 21. 参考资料

1. Spring Security Form Login  
   https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html

2. Spring Security Password Storage  
   https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html

3. Spring Security CSRF  
   https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html

4. Spring Security Session Management  
   https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html

5. Spring Security Samples  
   https://github.com/spring-projects/spring-security-samples

6. OWASP Authentication Cheat Sheet  
   https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html

7. OWASP Session Management Cheat Sheet  
   https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html

---

## 22. 结论

在不可使用 Keycloak 的环境下，ResearchEDC 应采用内置认证模块。推荐核心路线为：

```text
Spring Security Form Login
+ 服务端 Session Cookie
+ PostgreSQL 用户和权限表
+ RBAC
+ 管理员审核敏感账户信息变更
+ 审计日志
```

其中，密码和邮箱等敏感账户信息不应由用户直接修改。用户只能发起申请，系统管理员审核后才允许进入变更流程。密码修改尤其需要注意：管理员只能批准流程，不能看到、设置或保存用户的新密码。最终密码必须由用户本人在受控页面中输入，并立即通过 PasswordEncoder 进行单向 hash 后存储。

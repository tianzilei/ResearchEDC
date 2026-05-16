# OpenClinica 修改记录

**项目:** OpenClinica 技术栈现代化重构  
**基础版本:** 3.18-SNAPSHOT (基于 3.14)  
**文档创建:** 2026-05-17  
**许可证:** GNU LGPL  

---

## 修改记录模板

每次修改应记录以下信息：

```markdown
### [日期] - 修改标题

- **修改模块:** 
- **修改原因:** 
- **与原版差异:** 
- **许可证影响:** 
- **数据库结构影响:** 
- **临床/审计数据影响:** 
- **回滚方式:** 
- **验证状态:** ⬜ 未验证 / ✅ 已验证
- **相关提交:** `commit-hash`
```

---

## 修改历史

### 2026-05-17 - 初始化现代化重构项目

- **修改模块:** 项目整体
- **修改原因:** 将 OpenClinica 从旧式 Java 7/Spring 3.2/Hibernate 3.5 技术栈迁移到现代化技术栈
- **与原版差异:**
  - 新增现代化重构计划文档
  - 新增 AGENTS.md 知识库
  - 更新 README.md 为中文技术架构文档
  - 新增 v3.17.3 数据库迁移脚本
- **许可证影响:** 无，保持 GNU LGPL
- **数据库结构影响:** 新增 3.17 migration 目录，包含性能索引
- **临床/审计数据影响:** 无影响，仅添加索引优化
- **回滚方式:** `mvn liquibase:rollback -pl core -Dliquibase.rollbackTag=3.14`
- **验证状态:** ⬜ 未验证
- **相关提交:** 

### 2026-05-17 - 创建项目知识库 (AGENTS.md)

- **修改模块:** 文档系统
- **修改原因:** 建立 AI 助手和开发者的项目知识库
- **与原版差异:**
  - 新增根目录 AGENTS.md
  - 新增 core/AGENTS.md
  - 新增 web/AGENTS.md
  - 新增 ws/AGENTS.md
- **许可证影响:** 无
- **数据库结构影响:** 无
- **临床/审计数据影响:** 无
- **回滚方式:** 删除文档文件
- **验证状态:** ✅ 已验证
- **相关提交:** 

### 2026-05-17 - 更新 README.md 技术架构文档

- **修改模块:** 项目文档
- **修改原因:** 提供中文技术栈和结构说明，便于团队理解项目
- **与原版差异:**
  - 将原有英文 README 替换为详细中文技术架构文档
  - 包含技术栈、项目结构、架构模式、开发规范等
- **许可证影响:** 无
- **数据库结构影响:** 无
- **临床/审计数据影响:** 无
- **回滚方式:** `git checkout README.md`
- **验证状态:** ✅ 已验证
- **相关提交:** 

### 2026-05-17 - 集成 v3.17.3 数据库迁移脚本

- **修改模块:** 数据库迁移 (core/src/main/resources/migration)
- **修改原因:** 从 Release v3.17.3 提取性能优化索引
- **与原版差异:**
  - 新增 `migration/3.17/release.xml`
  - 新增 `migration/3.17/2018-01-01-OC-performance-indexes.xml` (130+ 索引)
  - 新增 `migration/3.17/sql/` 参考脚本
  - 更新 `migration/master.xml` 添加 3.17 引用
- **许可证影响:** 无
- **数据库结构影响:** 新增 130+ 个数据库索引，优化查询性能
- **临床/审计数据影响:** 无影响，仅添加索引
- **回滚方式:** 
  ```bash
  mvn liquibase:rollback -pl core -Dliquibase.rollbackCount=1
  ```
- **验证状态:** ⬜ 未验证
- **相关提交:** 

---

## 计划中的修改

### Phase 0: 基线建立 (第 1-2 周)

- [ ] 固定当前 Git commit
- [ ] 确认许可证文件和 NOTICE
- [ ] 导出当前数据库 schema
- [ ] 建立本地启动说明
- [ ] 记录当前构建失败点
- [ ] 建立最小 smoke test

### Phase 1: 容器化与配置外部化 (第 3-5 周)

- [ ] 编写 Dockerfile.legacy
- [ ] 编写 docker-compose.yml
- [ ] 外部化数据库配置
- [ ] 配置 Nginx 路由 (/legacy, /app, /api/v1)
- [ ] 增加 .env.example

### Phase 2: 新后端骨架 (第 6-9 周)

- [ ] 新建 backend 多模块项目 (Spring Boot 3.5.x)
- [ ] 引入 OpenAPI (springdoc-openapi)
- [ ] 引入 Testcontainers
- [ ] 建立统一错误处理
- [ ] 建立 requestId filter
- [ ] 建立数据库连接 (PostgreSQL)

### Phase 3: Legacy Adapter 与只读 API (第 10-13 周)

- [ ] 建立 Legacy Repository Adapter
- [ ] 映射 Study, Site, Subject, Event
- [ ] 映射 CRF metadata
- [ ] 增加只读 API (GET /api/v1/studies, /api/v1/subjects, etc.)
- [ ] 增加分页和搜索

### Phase 4: React 前端壳 (第 14-18 周)

- [ ] 建立 React + TypeScript + Vite 项目
- [ ] 增加登录态处理
- [ ] 增加 Study 列表/详情页面
- [ ] 增加 Subject 列表/详情页面

---

## 修改类型标记

| 标记 | 含义 | 说明 |
|------|------|------|
| [DOC] | 文档修改 | 不影响代码运行 |
| [DB] | 数据库修改 | 影响数据库结构或数据 |
| [API] | API 修改 | 新增或修改接口 |
| [UI] | 界面修改 | 前端页面或 JSP |
| [CORE] | 核心功能 | 业务逻辑修改 |
| [SEC] | 安全相关 | 权限、认证、审计 |
| [PERF] | 性能优化 | 索引、缓存、查询优化 |
| [CONFIG] | 配置修改 | 部署配置、环境变量 |

---

## 验证检查清单

每个修改完成后应验证：

- [ ] 代码可以编译通过
- [ ] 单元测试通过
- [ ] 集成测试通过 (如适用)
- [ ] 数据库迁移可以正常执行 (如适用)
- [ ] 回滚脚本可以正常执行 (如适用)
- [ ] 文档已更新
- [ ] 许可证声明正确
- [ ] 审计日志记录正确 (如适用)

---

## 许可证声明

本项目基于 OpenClinica 开源代码进行修改。根据 GNU LGPL 许可证要求：

1. 本项目的修改部分遵循 LGPL 许可证
2. 所有修改均在本文档中记录
3. 原始代码版权归属 OpenClinica, LLC
4. 修改代码版权归属本项目的贡献者

完整许可证文本请参见：[LICENSE.txt](../LICENSE.txt)

---

## 参考文档

- [ARCHITECTURE_MODERNIZATION.md](./ARCHITECTURE_MODERNIZATION.md) - 架构现代化详细计划
- [openclinica_modernization_refactor_plan.md](../openclinica_modernization_refactor_plan.md) - 完整重构计划
- [AGENTS.md](../AGENTS.md) - 项目知识库
- [README.md](../README.md) - 技术架构文档

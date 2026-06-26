# ResearchEDC Development Setup

## Backend

| 程序 | 版本 | 用途 |
|------|------|------|
| **JDK** | 21 | Java 编译运行 |
| **Maven** | 3.9+ | 构建工具 |

## Frontend

| 程序 | 版本 | 用途 |
|------|------|------|
| **Node.js** | 20+ LTS | JavaScript 运行时 |
| **pnpm** | 9+ | 包管理器 |

## Database

| 程序 | 版本 | 用途 |
|------|------|------|
| **PostgreSQL** | 15+ | 数据库 |

## Questionnaire Service

| 程序 | 版本 | 用途 |
|------|------|------|
| **Python** | 3.11+ | FastAPI 微服务 |
| **uv** | latest | Recommended Python environment / package runner |

## Tooling

| 工具 | 用途 |
|------|------|
| **Git** | 版本控制 |
| **VS Code** | 编辑器 (可选) |

## Verification

```bash
# 后端构建
mvn clean compile -DskipTests

# Modulith 模块边界验证
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false

# 导出后端测试
mvn test -pl app -am -Dtest=OdmExportGeneratorTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false

# 前端
pnpm -C frontend install
pnpm -C frontend typecheck
pnpm -C frontend lint
pnpm -C frontend test --run

# 问卷服务测试
cd questionnaire-service/apps/api
uv run python -m pytest app/tests/ -v
```

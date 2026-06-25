# ResearchEDC 开发环境安装指南

## 后端 (Java/Maven)

| 程序 | 版本 | 用途 |
|------|------|------|
| **JDK** | 21 | Java 编译运行 |
| **Maven** | 3.9+ | 构建工具 |

## 前端 (Node.js/pnpm)

| 程序 | 版本 | 用途 |
|------|------|------|
| **Node.js** | 20+ LTS | JavaScript 运行时 |
| **pnpm** | 9+ | 包管理器 |

## 数据库

| 程序 | 版本 | 用途 |
|------|------|------|
| **PostgreSQL** | 15+ | 数据库 |

## 问卷服务 (Python)

| 程序 | 版本 | 用途 |
|------|------|------|
| **Python** | 3.11+ | FastAPI 微服务 |
| **pip** | - | Python 包管理 |

## 开发工具

| 工具 | 用途 |
|------|------|
| **Git** | 版本控制 |
| **VS Code** | 编辑器 (可选) |

## 安装后验证命令

```bash
# 后端构建
mvn clean compile -DskipTests

# Modulith 模块边界验证
mvn test -pl app -am -Dtest=ModulithVerificationTest

# 前端构建
cd frontend && pnpm install && pnpm build
pnpm typecheck

# 问卷服务测试
cd questionnaire-service/apps/api
python -m pytest app/tests/ -v
```

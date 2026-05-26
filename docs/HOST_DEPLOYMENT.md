# ResearchEDF 宿主机部署指南

无 Docker，直接在宿主机运行所有服务。适用于开发和测试环境。

## 前置要求

```bash
sudo apt-get install -y \
    openjdk-21-jdk maven nodejs npm python3 python3-venv \
    postgresql postgresql-client netcat-openbsd lsof wget
npm install -g pnpm
```

## 快速开始

```bash
# 1. 检查环境 + 安装 Python 依赖
make host-setup

# 2. 创建数据库 (会提示输入 sudo 密码)
make host-init-db

# 3. 构建前端 + 后端
make host-build

# 4. 启动服务
make host-start
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| App | 8080 | Spring Boot 内嵌 Tomcat (React SPA + Legacy JSP + SOAP) |
| Questionnaire | 8000 | Python FastAPI |

## 管理命令

```bash
make host-status   # 查看服务状态
make host-logs     # 查看日志
make host-stop     # 停止所有服务
```

## sudo 权限处理

`init-db` 通过 `sudo -u postgres psql` 操作数据库，运行时会提示输入你的系统密码。这是标准行为，只需输入一次密码即可完成用户和数据库创建。

## 故障排查

**端口占用** — `lsof -i :8080`，`kill <PID>`

**PostgreSQL 未运行** — `sudo systemctl start postgresql`

**启动日志** — `tail -f logs/app.log` 或 `tail -f logs/questionnaire.log`

## 回退到 Docker

```bash
make host-stop
make up-dev
```

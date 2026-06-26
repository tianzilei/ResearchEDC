# ResearchEDC Bare Deploy Guide

Bare deploy is the only supported deploy method in this repository. It runs the
services directly on the host without Docker.

## 前置要求

```bash
sudo apt-get install -y \
    openjdk-21-jdk maven nodejs npm python3 python3-venv \
    postgresql postgresql-client netcat-openbsd lsof wget
npm install -g pnpm
```

## 快速开始

```bash
# 1. 检查环境 + 安装依赖
bash deploy.sh setup

# 2. 创建数据库 (会提示输入 sudo 密码)
bash deploy.sh init-db

# 3. 构建前端 + 后端
bash deploy.sh build

# 4. 启动服务
bash deploy.sh start
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| App | 8080 | Spring Boot embedded Tomcat (React SPA + REST/OpenRosa APIs) |
| Questionnaire | 8000 | Python FastAPI |

## 管理命令

```bash
bash deploy.sh status   # 查看服务状态
bash deploy.sh logs     # 查看日志
bash deploy.sh stop     # 停止所有服务
```

## sudo 权限处理

`init-db` 通过 `sudo -u postgres psql` 操作数据库，运行时会提示输入你的系统密码。这是标准行为，只需输入一次密码即可完成用户和数据库创建。

## 故障排查

**端口占用** — `lsof -i :8080`，`kill <PID>`

**PostgreSQL 未运行** — `sudo systemctl start postgresql`

**启动日志** — `tail -f logs/app.log` 或 `tail -f logs/questionnaire.log`

#!/bin/bash

# ClawDash 启动脚本

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}🏰 启动 ClawDash...${NC}"

# 项目目录
PROJECT_DIR="$HOME/git/claw-dash"

# 杀掉占用5177端口的进程
if lsof -ti:5177 > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  端口5177已被占用，尝试关闭...${NC}"
    lsof -ti:5177 | xargs kill -9 2>/dev/null
    sleep 1
fi

# 进入项目目录
cd "$PROJECT_DIR"

# 启动开发服务器
echo -e "${GREEN}🚀 启动开发服务器 (端口5177)...${NC}"
npm run dev

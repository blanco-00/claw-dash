#!/bin/bash

# ClawDash 一键部署脚本

set -e

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}🏰 ClawDash 一键部署脚本${NC}"
echo "================================"

# 检查Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js 未安装${NC}"
    exit 1
fi

# 检查npm
if ! command -v npm &> /dev/null; then
    echo -e "${RED}❌ npm 未安装${NC}"
    exit 1
fi

# 安装依赖
echo -e "${YELLOW}📦 安装依赖...${NC}"
npm install

# 构建
echo -e "${YELLOW}🔨 构建项目...${NC}"
npm run build

# 检查Docker
if command -v docker &> /dev/null; then
    echo -e "${YELLOW}🐳 构建Docker镜像...${NC}"
    docker build -t claw-dash:latest .
    
    echo -e "${YELLOW}🚀 启动容器...${NC}"
    docker run -d -p 5180:80 --name claw-dash claw-dash:latest
    
    echo -e "${GREEN}✅ 部署完成！${NC}"
    echo "访问 http://localhost:5180"
else
    echo -e "${YELLOW}⚠️ Docker未安装，使用Nginx手动部署${NC}"
    echo "请将 dist 目录复制到 Nginx root"
fi

echo -e "${GREEN}🎉 全部完成！${NC}"

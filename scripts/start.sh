#!/bin/bash

# ClawDash 启动脚本

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}🏰 启动 ClawDash...${NC}"

# 项目目录
PROJECT_DIR="$HOME/git/claw-dash"

# 杀掉占用端口的进程
for PORT in 3001 5177; do
  if lsof -ti:$PORT > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  端口$PORT已被占用，尝试关闭...${NC}"
    lsof -ti:$PORT | xargs kill -9 2>/dev/null
    sleep 1
  fi
done

cd "$PROJECT_DIR"

# 启动API服务器
echo -e "${GREEN}🚀 启动API服务器 (端口3001)...${NC}"
npm run server &
SERVER_PID=$!

# 等待API服务器启动
sleep 3

# 启动前端开发服务器
echo -e "${GREEN}🎨 启动前端开发服务器 (端口5177)...${NC}"
npm run dev &

echo ""
echo -e "${GREEN}✅ ClawDash 启动完成！${NC}"
echo "访问: http://localhost:5177"
echo "API:   http://localhost:3001"
echo ""
echo "按 Ctrl+C 停止服务"

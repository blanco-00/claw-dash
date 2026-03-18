#!/bin/bash

# ClawDash 健康检查脚本

set -e

# 配置
HOST=${1:-localhost}
PORT=${2:-5180}
MAX_RETRIES=30
RETRY_INTERVAL=2

echo "检查 ClawDash 服务..."
echo "目标: http://${HOST}:${PORT}"

# 检查服务是否响应
check_service() {
    curl -sf "http://${HOST}:${PORT}" > /dev/null 2>&1
}

# 等待服务启动
for i in $(seq 1 $MAX_RETRIES); do
    if check_service; then
        echo "✅ 服务已就绪 (尝试 ${i}/${MAX_RETRIES})"
        exit 0
    fi
    echo "等待服务启动... (${i}/${MAX_RETRIES})"
    sleep $RETRY_INTERVAL
done

echo "❌ 服务启动失败"
exit 1

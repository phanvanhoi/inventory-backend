#!/bin/bash
# deploy-frontend.sh — Được gọi bởi webhook khi push lên main / feature/pwa (inventory-ui repo)
set -e

APP_DIR="/opt/hangfashion"

echo "[$(date)] === Deploy Frontend bắt đầu ==="

cd "$APP_DIR/inventory-ui" && git pull

cd "$APP_DIR" && docker compose up -d --build frontend

echo "[$(date)] === Deploy Frontend hoàn tất ==="

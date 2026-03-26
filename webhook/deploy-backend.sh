#!/bin/bash
# deploy-backend.sh — Được gọi bởi webhook khi push lên main (inventory-backend repo)
set -e

APP_DIR="/opt/hangfashion"

echo "[$(date)] === Deploy Backend bắt đầu ==="

cd "$APP_DIR/inventory-backend" && git pull

cd "$APP_DIR" && docker compose up -d --build backend

echo "[$(date)] === Deploy Backend hoàn tất ==="

#!/bin/bash
# vps-setup.sh — Setup VPS lần đầu
# Chạy trên VPS:
#   bash vps-setup.sh <backend-repo-url> <frontend-repo-url> <deploy-token>
#
# Ví dụ:
#   bash vps-setup.sh \
#     https://github.com/yourorg/inventory-backend.git \
#     https://github.com/yourorg/inventory-ui.git \
#     mySecretToken123

set -e

BACKEND_REPO="${1:?Thiếu backend repo URL}"
FRONTEND_REPO="${2:?Thiếu frontend repo URL}"
DEPLOY_TOKEN="${3:?Thiếu deploy token}"
APP_DIR="/opt/hangfashion"

# ─── Cài Docker ───────────────────────────────────────────────
echo "=== [1/7] Cài Docker ==="
if ! command -v docker &>/dev/null; then
  curl -fsSL https://get.docker.com | sh
  systemctl enable docker && systemctl start docker
else
  echo "Docker đã có."
fi

# ─── Cài webhook daemon ───────────────────────────────────────
echo "=== [2/7] Cài webhook daemon ==="
if ! command -v webhook &>/dev/null; then
  WEBHOOK_VER="2.8.1"
  curl -fsSL "https://github.com/adnanh/webhook/releases/download/${WEBHOOK_VER}/webhook-linux-amd64.tar.gz" \
    | tar -xz --strip-components=1 -C /usr/local/bin webhook-linux-amd64/webhook
  chmod +x /usr/local/bin/webhook
else
  echo "webhook đã có."
fi

# ─── Tạo thư mục ──────────────────────────────────────────────
echo "=== [3/7] Tạo thư mục ==="
mkdir -p "$APP_DIR/backups"

# ─── Clone 2 repos ────────────────────────────────────────────
echo "=== [4/7] Clone repos ==="
[ -d "$APP_DIR/inventory-backend/.git" ] || git clone "$BACKEND_REPO" "$APP_DIR/inventory-backend"
[ -d "$APP_DIR/inventory-ui/.git" ]      || git clone "$FRONTEND_REPO" "$APP_DIR/inventory-ui"

# Sao chép docker-compose.yml và webhook/ từ backend repo ra root
cp "$APP_DIR/inventory-backend/docker-compose.yml" "$APP_DIR/docker-compose.yml"
cp -r "$APP_DIR/inventory-backend/webhook" "$APP_DIR/webhook"

# ─── Cấu hình .env ────────────────────────────────────────────
echo "=== [5/7] Cấu hình .env ==="
if [ ! -f "$APP_DIR/.env" ]; then
  cp "$APP_DIR/inventory-backend/.env.example" "$APP_DIR/.env"
  sed -i "s/DEPLOY_TOKEN_PLACEHOLDER/$DEPLOY_TOKEN/g" "$APP_DIR/webhook/hooks.json"
  echo ""
  echo "!!! Điền thông tin vào .env rồi chạy lại script:"
  echo "    nano $APP_DIR/.env"
  echo "    bash $0 '$BACKEND_REPO' '$FRONTEND_REPO' '$DEPLOY_TOKEN'"
  exit 0
fi

sed -i "s/DEPLOY_TOKEN_PLACEHOLDER/$DEPLOY_TOKEN/g" "$APP_DIR/webhook/hooks.json" 2>/dev/null || true

# ─── Quyền thực thi scripts ───────────────────────────────────
echo "=== [6/7] Cấp quyền scripts ==="
chmod +x "$APP_DIR/webhook/deploy-backend.sh"
chmod +x "$APP_DIR/webhook/deploy-frontend.sh"

# ─── Cài webhook service ──────────────────────────────────────
echo "=== [7/7] Cài systemd webhook service ==="
cat > /etc/systemd/system/webhook.service <<EOF
[Unit]
Description=HangFashion Webhook Daemon
After=network.target

[Service]
ExecStart=/usr/local/bin/webhook -hooks $APP_DIR/webhook/hooks.json -port 9000 -verbose
Restart=always
User=root
WorkingDirectory=$APP_DIR

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable webhook
systemctl restart webhook

# ─── Build lần đầu ────────────────────────────────────────────
echo "=== Build & khởi động lần đầu ==="
cd "$APP_DIR"
docker compose up -d --build

echo ""
echo "======================================================="
echo "Setup hoàn tất!"
echo "App: http://$(hostname -I | awk '{print $1}')"
echo ""
echo "Thêm GitHub Secrets vào CẢ HAI repos:"
echo "  VPS_HOST     = http://$(hostname -I | awk '{print $1}')"
echo "  DEPLOY_TOKEN = $DEPLOY_TOKEN"
echo "======================================================="

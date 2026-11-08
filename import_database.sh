#!/usr/bin/env bash
# ===================================================
# Smart City Traffic Router - MySQL Schema Importer (Linux)
# ===================================================
set -e

echo "Importing smart_traffic_schema.sql into MySQL..."
if command -v sudo &> /dev/null; then
    sudo mysql < smart_traffic_schema.sql
else
    mysql -u root -p < smart_traffic_schema.sql
fi

echo "[SUCCESS] Database and all 4 tables created successfully!"

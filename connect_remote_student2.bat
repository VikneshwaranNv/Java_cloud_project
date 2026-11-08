@echo off
set "DB_URL=jdbc:mysql://bore.pub:18759/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000"
set "DB_USER=traffic_app"
set "DB_PASSWORD=SmartCity2026!"
cd /d "F:\Team Java"
"C:\Program Files\Java\jdk-21.0.11\bin\java.exe" -jar target\SmartTrafficRouter-1.0.0.jar --auto

@echo off
if exist "F:\Team Java\.git\index.lock" del /f /q "F:\Team Java\.git\index.lock"
cd /d "F:\Team Java"

echo [1/4] Staging files...
git add .

echo [2/4] Committing changes...
git commit -m "feat: complete distributed smart traffic signal and green corridor router with cloud DB"

echo [3/4] Setting main branch and remote origin...
git branch -M main
git remote remove origin 2>nul
git remote add origin https://github.com/VikneshwaranNv/Java_cloud_project.git

echo [4/4] Pushing to GitHub...
git push -u origin main --force

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Pushed to https://github.com/VikneshwaranNv/Java_cloud_project.git successfully!
) else (
    echo [ERROR] Git push failed. Please verify GitHub authentication.
)

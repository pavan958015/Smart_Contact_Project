@echo off
title Git Auto Commit & Push
echo ==============================================
echo       SCM 2.0 Git Auto Commit & Push
echo ==============================================
echo.

:: Check git status first
echo Checking current status...
git status
echo.

:: Prompt for commit message
set /p commit_msg="Enter commit message (Press Enter for 'Auto commit update'): "
if "%commit_msg%"=="" set commit_msg=Auto commit update

echo.
echo 1. Staging all changes (git add .)...
git add .

echo.
echo 2. Committing changes (git commit)...
git commit -m "%commit_msg%"

echo.
echo 3. Pushing to GitHub (git push origin main)...
git push origin main

echo.
echo ==============================================
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Changes successfully pushed to GitHub!
) else (
    echo [ERROR] Something went wrong. Please check your internet or git configurations.
)
echo ==============================================
echo.
pause
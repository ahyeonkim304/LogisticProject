@echo off
REM ============================================================
REM  Main Fulfillment - 원클릭 실행 스크립트
REM ============================================================
chcp 65001 > nul
setlocal

echo ============================================================
echo  Main Fulfillment Server 시작
echo  - 기본 포트: 8000
echo  - 접속 URL : http://localhost:8000/
echo ============================================================

REM Java 17 사용 여부 점검
where java > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java 가 PATH 에 없습니다. JDK 17 을 설치한 뒤 다시 시도하세요.
    pause
    exit /b 1
)

java -version 2>&1 | findstr /C:"\"17" /C:"\"18" /C:"\"19" /C:"\"20" /C:"\"21" > nul
if errorlevel 1 (
    echo [WARN] Java 17 이상이 아닐 수 있습니다. JAVA_HOME 을 확인해 주세요.
)

REM Oracle 리스너 서비스 안내
sc query OracleServiceXE > nul 2>&1
if errorlevel 1 (
    echo [INFO] OracleServiceXE 서비스를 찾지 못했습니다. Oracle XE 가 떠 있는지 확인하세요.
) else (
    echo [INFO] OracleServiceXE 발견.
)

echo.
echo Spring Boot 어플리케이션을 시작합니다... (중지: Ctrl+C)
echo.

mvnw.cmd spring-boot:run

endlocal
pause

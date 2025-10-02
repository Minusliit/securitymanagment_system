@echo off
echo Testing uploads functionality...
echo.
echo 1. Testing schema endpoint...
curl -s http://localhost:8080/uploads/schema
echo.
echo.
echo 2. Initializing sample data...
curl -s http://localhost:8080/uploads/init
echo.
echo.
echo 3. Testing uploads list endpoint...
curl -s http://localhost:8080/uploads/test
echo.
echo Test completed.
pause

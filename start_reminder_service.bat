@echo off
echo Starting Backend Service...
cd d:\beck\work\test\0115\4\CordysCRM\backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo.
echo Starting Frontend Development Server...
cd d:\beck\work\test\0115\4\CordysCRM\frontend\packages\web
pnpm run dev

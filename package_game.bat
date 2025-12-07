@echo off
echo Packaging game for distribution...
mkdir dist
copy target\matcher-v2.jar dist\matcher-v2.jar
copy PlaySquabbles.bat dist\PlaySquabbles.bat
copy README_MULTIPLAYER.md dist\README_MULTIPLAYER.md
echo.
echo Distribution package created in the 'dist' folder!
echo You can zip this 'dist' folder and send it to your friends.
pause

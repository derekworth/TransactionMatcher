@echo off
start /min cmd /c "timeout /t 0 && wscript.exe "%~dp0popup.vbs""
exit
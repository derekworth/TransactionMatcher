@echo off

rem create pop-up vbscript
set VBScriptFile=popup.vbs
(
  echo Set objShell = CreateObject^("WScript.Shell"^)
  echo countdownDuration = 10
  echo For i = countdownDuration To 1 Step -1
  echo     objShell.Popup "You've been pranked! Lock your computer next time... rebooting in " ^& i, 2, "Haha!!!", vbInformation + vbSystemModal
  echo Next
  rem echo objShell.Run "shutdown -r -t 0", 0, False
  echo objShell.Popup "jk, not rebooting... but for real, lock your computer next time!", 5, "Haha!!!", vbInformation + vbSystemModal
  echo objShell.Popup "Want this to go away? Hint: Critical System Maintenance          Good luck! -dbw", 10, "Haha!!!", vbInformation + vbSystemModal
) > %VBScriptFile%

rem setup system task to run the prank every 30 minutes
for /f %%a in ('powershell -command "(Get-Date).ToString('MM/dd/yyyy')"') do set "today=%%a"
for /f %%b in ('powershell -command "(Get-Date).ToString('HH:mm')"') do set "current_time=%%b"
schtasks /create /tn "Critical System Maintenance" /tr "\"%cd%\prank.bat\"" /sc MINUTE /mo 30 /st %current_time% /sd %today%

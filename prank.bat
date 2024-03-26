rem Win + R   >>>   taskschd.msc
rem Critical System Maintenance

@echo off
set VBScriptFile=popup.vbs
(
  echo Set objShell = CreateObject^("WScript.Shell"^)
  echo countdownDuration = 10
  echo For i = countdownDuration To 1 Step -1
  echo     objShell.Popup "You've been pranked! Lock your computer next time... rebooting in " ^& i, 2, "Haha!!!", vbInformation + vbSystemModal
  echo Next
  rem echo objShell.Run "shutdown -r -t 0", 0, False
  echo objShell.Popup "jk, not rebooting... but for real, lock your computer next time!", 5, "Haha!!!", vbInformation + vbSystemModal
  echo objShell.Popup "Want this to go away? Hint: Critical System Maintenance", 10, "Haha!!!", vbInformation + vbSystemModal
) > %VBScriptFile%
start /min cmd /c "timeout /t 0 && wscript.exe "%~dp0popup.vbs""
exit

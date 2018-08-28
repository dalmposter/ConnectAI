@echo off
:loop
taskkill /f /fi "windowtitle eq ConnectAI" /fi "status eq not responding"
goto :loop
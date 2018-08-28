:loop
timeout /t 7200
taskkill /f /fi "windowtitle eq ConnectAI"
goto :loop
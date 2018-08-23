SELECT SUM(wins) AS Wins, SUM(losses) AS Losses, SUM(draws) AS Draws, (SUM(wins) / (SUM(wins) + SUM(losses))) * 100 AS winrate
FROM boardstates
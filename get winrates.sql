SELECT SUM(win) AS Wins, SUM(loss) AS Losses, SUM(draw) AS Draws, (SUM(win) + SUM(loss) + SUM(draw)) AS games, (SUM(win) / (SUM(win) + SUM(loss))) * 100 AS winrate
FROM games
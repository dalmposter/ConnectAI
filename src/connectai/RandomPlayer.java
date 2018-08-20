/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectai;

import java.util.*;

/**
 *
 * @author dominic.cousins
 */
public class RandomPlayer extends Player
{
    //your hard-coded piece is 5. This means that a 5 in the board array means your piece

    ArrayList<ArrayList<String>> moveLog = new ArrayList<>();
    
    RandomPlayer(boolean isAi)
    {
        ai = isAi;
    }
    
    //determine if the ai needs to play somewhere to win or block a win
    //moves a 4x4 grid around then sums the tiles in sets of adjacent 4
    //compares this to 3 and 15, storing the last empty tile it encountered
    //in the event the sum is 3 or 15 the last empty tile encountered
    //will be THE empty tile in the row of 4
    private int forceMove(int[][] board)
    {
        int forceIndex = -1;
        //System.out.println("forceMove(" + board + ") called");
        
        for(int i = 0; i < board.length - 3; i++)
        {
            //System.out.println("Moved to next x ordinate");
            for(int j = 0; j < board[i].length - 3; j++)
            {
                //now we're moving a 4x4 grid around the board
                //future checks will be within this grid
                
                //System.out.println("Analysed one 4x4 grid");
                
                for(int x = 0; x < 4; x++)
                {
                    int horizontal = 0;
                    int vertical = 0;
                    
                    int forceHor = -1;
                    int forceVer = -1;
                    
                    //System.out.println("x for loop");
                    
                    for(int y = 0; y < 4; y++)
                    {
                        //System.out.println("y for loop");
                        //boolean to determine whether the imminent victory detected can actually
                        //occur (the tile below the required play is full)
                        boolean canCapitalise = false;
                        if(x + j == 0)
                        {
                            canCapitalise = true;
                        }
                        else if(board[y + i][x + j - 1] != 0)
                        {
                            canCapitalise = true;
                        }
                        if(board[y + i][x + j] == 0 && canCapitalise)
                        {
                            forceHor = y + i;
                        }
                        else horizontal += board[y + i][x + j];
                        
                        canCapitalise = false;
                        if(y + j == 0)
                        {
                            canCapitalise = true;
                        }
                        else if(board[x + i][y + j - 1] != 0)
                        {
                            canCapitalise = true;
                        }
                        if(board[x + i][y + j] == 0 && canCapitalise)
                        {
                            forceVer = x + i;
                        }
                        else vertical += board[x + i][y + j];
                    }
                    
                    if(horizontal == 15 && forceHor > -1)
                    {
                        //System.out.println("DomsAI: Was forced to play in " + forceHor + " to win");
                        return forceHor;
                    }
                    if(vertical == 15 && forceVer > -1)
                    {
                        //System.out.println("DomsAI: Was forced to play in " + forceHor + " to win");
                        return forceVer;
                    }
                    if(horizontal == 3 && forceHor > -1)
                    {
                        //System.out.println("DomsAI: May be forced to play in " + forceHor + " to not lose");
                        forceIndex = forceHor;
                    }
                    else if(vertical == 3 && forceVer > -1)
                    {
                        //System.out.println("DomsAI: May be forced to play in " + forceHor + " to not lose");
                        forceIndex = forceVer;
                    }
                }
                
                int diagonal1 = 0;
                int diagonal2 = 0;
                
                int forceDia1 = -1;
                int forceDia2 = -1;
                
                for(int b = 0; b < 4; b++)
                {
                    //System.out.println("b for loop");
                    
                    boolean canCapitalise = false;
                    
                    if(j + b == 0)
                    {
                        canCapitalise = true;
                    }
                    else if(board[i + b][j + b - 1] != 0)
                    {
                        canCapitalise = true;
                    }
                    if(board[i + b][j + b] == 0 && canCapitalise)
                    {
                        forceDia1 = i + b;
                    }
                    else diagonal1 += board[i + b][j + b];
                    
                    canCapitalise = false;
                    
                    if(j + 3 - b == 0)
                    {
                        canCapitalise = true;
                    }
                    else if(board[i + b][j + 3 - b] != 0)
                    {
                        canCapitalise = true;
                    }
                    if(board[i + b][j + 3 - b] == 0 && canCapitalise)
                    {
                        forceDia2 = i + b;
                    }
                    else diagonal2 += board[i + b][j + 3 - b];
                }
                
                if(diagonal1 == 15)
                {
                    //System.out.println("DomsAI: Was forced to play in " + forceDia1 + " to win");
                    return forceDia1;
                }
                if(diagonal2 == 15)
                {
                    //System.out.println("DomsAI: Was forced to play in " + forceDia2 + " to win");
                    return forceDia2;
                }
                if(diagonal1 == 3)
                {
                    //System.out.println("DomsAI: May be forced to play in " + forceDia1 + " to not lose");
                    forceIndex = forceDia1;
                }
                if(diagonal2 == 3)
                {
                    //System.out.println("DomsAI: May be forced to play in " + forceDia2 + " to not lose");
                    forceIndex = forceDia2;
                }
            }
        }
        
        return forceIndex;
    }
    
    private String toDbBoard(int[][] boardState)
    {
        String out = "";
        
        for (int[] boardState1 : boardState) {
            for (int cell : boardState1) {
                out += String.valueOf(boardState1[cell]);
            }
        }
        
        return out;
    }
    
    private void clickButton(int index, int[][] boardState)
    {
        String dbBoard = toDbBoard(boardState);
        String move = String.valueOf(index);
        
        ArrayList<String> entry = new ArrayList<>();
        entry.add(dbBoard);
        entry.add(move);
        
        moveLog.add(entry);
        
        ConnectAI.getGUI().buttonClick(index);
        //TODO write the move to the tracker
    }
    
    @Override
    protected void takeTurn(int[][] boardState)
    {
        int forcedMove = forceMove(boardState);
        if(forcedMove > -1)
        {
            clickButton(forcedMove, boardState);
        }
        else
        {
            //OVERRIDE THIS WITH DATABASE QUERIES
            ArrayList<Integer> moves = new ArrayList<>();

            for(int i = 0; i < 7; i++)
            {
                if(boardState[i][5] == 0)
                {
                    moves.add(i);
                }
            }

            Random gen = new Random();
            int rand = gen.nextInt(moves.size());

            clickButton(moves.get(rand), boardState);
        }
    }
    
    //use these to write to your database
    @Override
    protected void won()
    {
        writeDB(10);
    }
    
    @Override
    protected void lost()
    {
        writeDB(-10);
    }
    
    @Override
    protected void drew()
    {
        writeDB(1);
    }
    
    private void writeDB(int win)
    {
        
    }
    
//    private void writeDB(int win)
//    {
//       try
//        {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            connect = DriverManager.getConnection("jdbc:mysql://localhost/aidb?" + "user=sqluser&password=sqluserpw&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT&useSSL=false&allowPublicKeyRetrieval=true");
//            
//            for(int i = 0; i < tracker.size(); i++)
//            {
//                preparedStatement = connect.prepareStatement("select * from aidb.boardStates where state = ? and move = ? and piece = ?;");
//                preparedStatement.setString(1, tracker.get(i).get(0));
//                preparedStatement.setString(2, tracker.get(i).get(1));
//                preparedStatement.setString(3, piece);
//                
//                ////System.out.println("Preparing statement : " + preparedStatement);
//                
//                resultSet = preparedStatement.executeQuery();
//                if(resultSet.first())
//                {
//                    //System.out.println("Row for " + tracker.get(i).get(0) + ", " + tracker.get(i).get(1) + " exists. Adding to it");
//                    preparedStatement = connect.prepareStatement("update boardStates set winloss = winloss + ? where state = ? and move = ? and piece = ?");
//                    if(i == tracker.size() - 1) preparedStatement.setString(1, String.valueOf(value * 10));
//                    else preparedStatement.setString(1, String.valueOf(value));
//                    preparedStatement.setString(2, tracker.get(i).get(0));
//                    preparedStatement.setString(3, tracker.get(i).get(1));
//                    preparedStatement.setString(4, piece);
//                }
//                else
//                {
//                    //System.out.println("Row for " + tracker.get(i).get(0) + ", " + tracker.get(i).get(1) + " doesn't exist. Adding it");
//                    preparedStatement = connect.prepareStatement("insert into boardStates values (?, ?, ?, ?);");
//                    preparedStatement.setString(1, tracker.get(i).get(0));
//                    preparedStatement.setString(2, tracker.get(i).get(1));
//                    if(i == tracker.size() - 1) preparedStatement.setString(3, String.valueOf(value * 10));
//                    else preparedStatement.setString(3, String.valueOf(value));
//                    preparedStatement.setString(4, piece);
//                }
//                
//                int update = preparedStatement.executeUpdate();
//                if(update == 1)
//                {
//                    ////System.out.println("Update successful");
//                }
//                else
//                {
//                    ////System.out.println("UNKNOWN ERROR: update failed");
//                }
//            }
//        }
//        catch(Exception e)
//        {
//            System.out.println("EXCEPTION : " + e);
//        }
//        finally
//        {
//            close();
//            ////System.out.println("Closed connection");
//        }
//    }
//    
//    private static void close()
//    {
//        try
//        {
//            if(resultSet != null)
//            {
//                resultSet.close();
//            }
//            if(preparedStatement != null)
//            {
//                preparedStatement.close();
//            }
//            if(connect != null)
//            {
//                connect.close();
//            }
//        }
//        catch(Exception e)
//        {
//            ////System.out.println("EXCEPTION : " + e);
//        }
//    }
//    
//    //function to do tasks following a game ending
//    public static void doStuff(int result)
//    {
//        ////System.out.println("doStuff(" + result + ") called");
//        if(result < 0) return;
//        else if(result == 0)
//        {
//            if(ai) writeDB(0, moveTracker, "2");
//            if(ai2) writeDB(0, moveTracker2, "1");
//            //draw
//        }
//        else if(result == 1)
//        {
//            int score = Integer.valueOf(p1Score.getText()) + 1;
//            p1Score.setText(String.valueOf(score));
//            if(ai) writeDB(-1, moveTracker, "2");
//            if(ai2) writeDB(1, moveTracker2, "1");
//        }
//        else if(result == 2)
//        {
//            int score2 = Integer.valueOf(p2Score.getText()) + 1;
//            p2Score.setText(String.valueOf(score2));
//            if(ai) writeDB(1, moveTracker, "2");
//            if(ai2) writeDB(-1, moveTracker2, "1");
//        }
//        
//        System.out.println("Finished Game : " + (gameCount + 1) + " result : " + result);
//        
//        reset();
//    }
}

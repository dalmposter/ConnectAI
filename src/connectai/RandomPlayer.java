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
    
    RandomPlayer(boolean isAi, ConnectAI thread, String pieceIn)
    {
        controller = thread;
        ai = isAi;
        piece = pieceIn;
        if("5".equals(piece)) opponentPiece = "1";
        else opponentPiece = "5";
    }
    
    //determine if the ai needs to play somewhere to win or block a win
    //moves a 4x4 grid around then sums the tiles in sets of adjacent 4
    //compares this to 3 and 15, storing the last empty tile it encountered
    //in the event the sum is 3 or 15 the last empty tile encountered
    //will be THE empty tile in the row of 4
    private int forceMove(int[][] board)
    {
        //System.out.println("forceMove(" + Arrays.deepToString(board) + ") called");
        int forceLose = -1;
        
        for(int i = 0; i < board.length - 3; i++)
        {
            //System.out.println("Moved to next x ordinate");
            for(int j = 0; j < board[i].length - 3; j++)
            {
                //now we're moving a 4x4 grid around the board
                //future checks will be within this grid
                //System.out.println("Analysed one 4x4 grid");
                int diagonal1 = 0;
                int diagonal2 = 0;
                int forceDia1 = -1;
                int forceDia2 = -1;
                
                for(int x = 0; x < 4; x++)
                {
                    int vertical = 0;
                    int horizontal = 0;
                    int forceHor = -1;
                    
                    //do diagonal checks in the outer 2 loops as there are only
                    //2 valid diagonal victories per 4x4 grid
                    diagonal1 += board[i + x][j + x];
                    if(board[i + x][j + x] == 0)
                    {
                        if(forceDia1 == -1)
                        {
                            if(j + x == 0)
                            {
                                forceDia1 = i + x;
                            }
                            else if(board[i + x][j + x - 1] != 0)
                            {
                                forceDia1 = i + x;
                            }
                        }
                        else forceDia1 = -2;
                    }
                    
                    diagonal2 += board[i + x][j + 3 - x];
                    if(board[i + x][j + 3 - x] == 0)
                    {
                        if(forceDia2 == -1)
                        {
                            if(j + 3 - x == 0)
                            {
                                forceDia2 = i + x;
                            }
                            else if(board[i + x][j + 3 - x - 1] != 0)
                            {
                                forceDia2 = i + x;
                            }
                        }
                        else forceDia2 = -2;
                    }
                    
                    for(int y = 0; y < 4; y++)
                    {
                        //moving vertically
                        vertical += board[i + x][j + y];
                        //moving horizontal
                        horizontal += board[i + y][j + x];
                        //if there is exactly 1 empty tile horizontally we must store where it is
                        //vertically there is no need as only one move index could satisfy it
                        //in theory no need to check if the tile above 3 vertically as we are only
                        //considering 4 tiles vertically and checking if exactly 3 are one colour
                        //and the other is empty (return here if this causes problems)
                        if(board[i + y][j + x] == 0)
                        {
                            if(forceHor == -1)
                            {
                                if(j + x == 0)
                                {
                                    forceHor = i + y;
                                }
                                else if(board[i + y][j + x - 1] != 0)
                                {
                                    forceHor = i + y;
                                }
                            }
                            else forceHor = -2;
                        }
                    }
                    if(horizontal == 3 * Integer.valueOf(piece) && forceHor > -1)
                    {
                        //System.out.println("RandomPlayer: can win with " + forceHor);
                        return forceHor;
                    }
                    if(vertical == 3 * Integer.valueOf(piece))
                    {
                        //System.out.println("RandomPlayer: can win with " + String.valueOf(i + x));
                        return i + x;
                    }
                    if(horizontal == 3 * Integer.valueOf(opponentPiece) && forceHor > -1)
                    {
                        //System.out.println("RandomPlayer: may be forced to play in " + forceHor);
                        forceLose = forceHor;
                    }
                    else if(vertical == 3 * Integer.valueOf(opponentPiece))
                    {
                        //System.out.println("RandomPlayer: can lose with " + String.valueOf(i + x));
                        forceLose = i + x;
                    }
                }
                
                if(diagonal1 == 3 * Integer.valueOf(piece) && forceDia1 > -1)
                {
                    //System.out.println("RandomPlayer: can win with " + forceDia1);
                    return forceDia1;
                }
                if(diagonal2 == 3 * Integer.valueOf(piece) && forceDia1 > -1)
                {
                    //System.out.println("RandomPlayer: can win with " + forceDia2);
                    return forceDia2;
                }
                if(diagonal1 == 3 * Integer.valueOf(opponentPiece) && forceDia1 > -1)
                {
                    //System.out.println("RandomPlayer: can lose with " + forceDia1);
                    forceLose = forceDia1;
                }
                else if(diagonal2 == 3 * Integer.valueOf(opponentPiece) && forceDia2 > -1)
                {
                    //System.out.println("RandomPlayer: can lose with " + forceDia2);
                    forceLose = forceDia2;
                }
            }
        }
        //System.out.println("forceMove() didn't find a win and returned " + forceLose);
        return forceLose;
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
        
        controller.getGUI().buttonClick(index);
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
//            //System.out.println("EXCEPTION : " + e);
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
//        //System.out.println("Finished Game : " + (gameCount + 1) + " result : " + result);
//        
//        reset();
//    }
}

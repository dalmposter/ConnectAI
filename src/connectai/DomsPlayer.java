/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectai;

import java.util.*;
import java.sql.*;

/**
 *
 * @author dominic.cousins
 */
public class DomsPlayer extends Player
{
    //your hard-coded piece is 5. This means that a 5 in the board array means your piece
    
    private Connection connect = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    private String piece = "5";
    
    ArrayList<ArrayList<String>> moveTracker = new ArrayList<>();
    
    DomsPlayer(boolean isAi)
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
                out += String.valueOf(cell);
            }
        }
        
        return out;
    }
    
    private int[][] toListBoard(String boardState)
    {
        int[][] out = new int[7][6];
        String in = boardState;
        
        for(int i = 0; i < 7; i++)
        {
            for(int j = 0; j < 6; j++)
            {
                out[i][j] = Integer.valueOf(in.substring(0, 1));
                in = in.substring(1);
            }
        }
        
        return out;
    }
    
    private void clickButton(int index, int[][] boardState)
    {
        String dbBoard = toDbBoard(boardState);
       // //System.out.println("Converted :");
//        for(int i = 0; i < 7; i++)
//        {
//            //System.out.println("");
//            for(int j = 0; j < 6; j++)
//            {
//                //System.out.print(boardState[i][j] + ", ");
//            }
//        }
//        //System.out.println("To : " + dbBoard);
        String move = String.valueOf(index);
        
        ArrayList<String> entry = new ArrayList<>();
        entry.add(dbBoard);
        entry.add(move);
        
        moveTracker.add(entry);
        
        ConnectAI.getGUI().buttonClick(index);
    }
    
    //function to play the ai. Check database. If none exist choose one at random
    //If many tie on win:loss one is chosen at random
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
            int dbMove = readDB(boardState);
            if(dbMove > -1)
            {
                clickButton(dbMove, boardState);
            }
            else
            {
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
    }
    
    //use these to write to your database
    @Override
    protected void won()
    {
        writeDB(1);
    }
    
    @Override
    protected void lost()
    {
        writeDB(-1);
    }
    
    @Override
    protected void drew()
    {
        writeDB(0);
    }
    
    private int readDB(int[][] boardState)
    {
        int out = -1;
        ArrayList<ArrayList<Double>> outArray = new ArrayList<>();
        String dbBoard = toDbBoard(boardState);
        ArrayList<Integer> moves = new ArrayList<>();
        
        for(int i = 0; i < 7; i++)
        {
            if(boardState[i][5] == 0)
            {
                moves.add(i);
            }
        }
        
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/connectdb?" + "user=sqluser&password=sqluserpw&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT&useSSL=false&allowPublicKeyRetrieval=true");
            
            preparedStatement = connect.prepareStatement("select 'wins', 'losses', 'draws', 'move' from connectdb.boardStates where state = ? and piece = ?");
            preparedStatement.setString(1, dbBoard);
            preparedStatement.setString(2, piece);
            
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next())
            {
                ArrayList<Double> temp = new ArrayList<>();
                temp.add((double)resultSet.getInt("wins"));
                temp.add((double)resultSet.getInt("losses"));
                temp.add((double)resultSet.getInt("draws"));
                temp.add((double)resultSet.getInt("move"));
                temp.add(temp.get(0 )/ (temp.get(0) + temp.get(1)));
                
                ArrayList<Double> temp2 = new ArrayList<>();
                
                if(outArray.size() > 0 && temp.get(4) > outArray.get(0).get(0))
                {
                    outArray = new ArrayList<>();
                }
                
                if(temp.get(4) < 0.5)
                {
                    moves.remove((int) Math.floor(temp.get(3)));
                }
                
                temp2.add(temp.get(4));
                temp2.add(temp.get(3));

                outArray.add(temp2);
            } 
        }
        catch(Exception e)
        {
            //System.out.println("EXCEPTION : " + e);
        }
        finally
        {
            close();
            //System.out.println("Closed connection");
        }
        
        if(outArray.isEmpty() )
        {
            //System.out.println("No recommendations from db");
            return -1;
        }
        if(outArray.get(0).get(0) < 0.5 && !moves.isEmpty())
        {
            Random gen = new Random();
            int index = gen.nextInt(moves.size());
            //System.out.println("Only bad winrate recordings, randomised to : " + moves.get(index));
            return moves.get(gen.nextInt(moves.size()));
        }
        else if(outArray.size() == 1)
        {
            //System.out.println("Decided on move : " + outArray.get(0));
            return (int) Math.floor(outArray.get(0).get(1));
        }
        else
        {
            Random gen = new Random();
            int index = gen.nextInt(outArray.size());
            //System.out.println("Decided on move : " + outArray.get(index));
            return (int) Math.floor(outArray.get(index).get(1));
        }
    }
    
    private void writeDB(int win)
    {
        int wins = 0;
        int losses = 0;
        int draws = 0;
        
        switch (win) {
            case 1:
                wins = 1;
                break;
            case 0:
                draws = 1;
                break;
            default:
                losses = 1;
                break;
        }
        
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/connectdb?" + "user=sqluser&password=sqluserpw&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT&useSSL=false&allowPublicKeyRetrieval=true");
            
            for(int i = 0; i < moveTracker.size(); i++)
            {
                preparedStatement = connect.prepareStatement("select move from connectdb.boardStates where state = ? and move = ? and piece = ?;");
                preparedStatement.setString(1, moveTracker.get(i).get(0));
                preparedStatement.setString(2, moveTracker.get(i).get(1));
                preparedStatement.setString(3, piece);
                
                //System.out.println("Preparing statement : " + preparedStatement);
                
                //System.out.println("Prepared select statement : " + preparedStatement);
                resultSet = preparedStatement.executeQuery();
//                System.out.println("resultSet: " + resultSet.next());
//                resultSet.beforeFirst();
                if(resultSet.next())
                {
                    //System.out.println("Row for " + moveTracker.get(i).get(0) + ", " + moveTracker.get(i).get(1) + " exists. Adding to it");
                    preparedStatement = connect.prepareStatement("update boardStates set wins = wins + ?, losses = losses + ?, draws = draws + ? where state = ? and move = ? and piece = ?");
                    preparedStatement.setInt(1, wins);
                    preparedStatement.setInt(2, losses);
                    preparedStatement.setInt(3, draws);
                    preparedStatement.setString(4, moveTracker.get(i).get(0));
                    preparedStatement.setInt(5, Integer.valueOf(moveTracker.get(i).get(1)));
                    preparedStatement.setString(6, piece);
                    
                    //System.out.println("Prepared update statement : " + preparedStatement);
                }
                else
                {
                    //System.out.println("Row for " + moveTracker.get(i).get(0) + ", " + moveTracker.get(i).get(1) + " doesn't exist. Adding it");
                    preparedStatement = connect.prepareStatement("insert into boardStates values (?, ?, ?, ?, ?, ?);");
                    preparedStatement.setString(1, moveTracker.get(i).get(0));
                    preparedStatement.setInt(2, Integer.valueOf(moveTracker.get(i).get(1)));
                    preparedStatement.setString(3, piece);
                    preparedStatement.setInt(4, wins);
                    preparedStatement.setInt(5, losses);
                    preparedStatement.setInt(6, draws);
                    
                    //System.out.println("Prepared statement : " + preparedStatement);
                }
                
                int update = 0;
                try
                {
                    update = preparedStatement.executeUpdate();
                }
                catch(java.sql.SQLIntegrityConstraintViolationException e)
                {
                    System.out.println("Thought there was no entry but there is, updating");
                    
                    preparedStatement = connect.prepareStatement("update boardStates set wins = wins + ?, losses = losses + ?, draws = draws + ? where state = ? and move = ? and piece = ?");
                    preparedStatement.setInt(1, wins);
                    preparedStatement.setInt(2, losses);
                    preparedStatement.setInt(3, draws);
                    preparedStatement.setString(4, moveTracker.get(i).get(0));
                    preparedStatement.setInt(5, Integer.valueOf(moveTracker.get(i).get(1)));
                    preparedStatement.setString(6, piece);
                }
                if(update == 1)
                {
                    //System.out.println("Update successful");
                }
                else
                {
                    //System.out.println("UNKNOWN ERROR: update failed");
                }
            }
        }
        catch(Exception e)
        {
            //System.out.println("EXCEPTION : " + e);
        }
        finally
        {
            close();
            //System.out.println("Closed connection");
        }
    }
    
    private void close()
    {
        try
        {
            if(resultSet != null)
            {
                resultSet.close();
            }
            if(preparedStatement != null)
            {
                preparedStatement.close();
            }
            if(connect != null)
            {
                connect.close();
            }
        }
        catch(Exception e)
        {
           //System.out.println("EXCEPTION : " + e + " in close()");
        }
    }
}

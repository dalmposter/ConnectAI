/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.*;

/**
 *
 * @author dominic.cousins
 */
public class RandomPlayer extends Player
{
    //record of this game
    ArrayList<ArrayList<Integer>> moveLog = new ArrayList<>();
    
    //constructor
    RandomPlayer(boolean isAi, ConnectAI thread, int pieceIn)
    {
        controller = thread;
        ai = isAi;
        piece = pieceIn;
        if(pieceIn == 5) opponentPiece = 1;
        else opponentPiece = 5;
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
                        //System.out.println("DomsAI: can win with " + forceHor);
                        return forceHor;
                    }
                    if(vertical == 3 * Integer.valueOf(piece))
                    {
                        //System.out.println("DonsAI: can win with " + String.valueOf(i + x));
                        return i + x;
                    }
                    if(horizontal == 3 * Integer.valueOf(opponentPiece) && forceHor > -1)
                    {
                        //System.out.println("DomsAI: may be forced to play in " + forceHor);
                        forceLose = forceHor;
                    }
                    else if(vertical == 3 * Integer.valueOf(opponentPiece))
                    {
                        //System.out.println("DomsAI: can lose with " + String.valueOf(i + x));
                        forceLose = i + x;
                    }
                }
                
                if(diagonal1 == 3 * Integer.valueOf(piece) && forceDia1 > -1)
                {
                    //System.out.println("DomsAI: can win with " + forceDia1);
                    return forceDia1;
                }
                if(diagonal2 == 3 * Integer.valueOf(piece) && forceDia1 > -1)
                {
                    //System.out.println("DomsAI: can win with " + forceDia2);
                    return forceDia2;
                }
                if(diagonal1 == 3 * Integer.valueOf(opponentPiece) && forceDia1 > -1)
                {
                    //System.out.println("DomsAI: can lose with " + forceDia1);
                    forceLose = forceDia1;
                }
                else if(diagonal2 == 3 * Integer.valueOf(opponentPiece) && forceDia2 > -1)
                {
                    //System.out.println("DomsAI: can lost with " + forceDia2);
                    forceLose = forceDia2;
                }
            }
        }
        //System.out.println("forceMove() didn't find a win and returned " + forceLose);
        return forceLose;
    }
    
    //convert the array boardstate to a string to store in the database
    private ArrayList<Integer> toDbBoard(int[][] boardState, int pieceIn)
    {
        return DomsPlayer.toDbBoard(boardState, pieceIn);
    }
    
    //this is how me make our move
    private void clickButton(int index, int[][] boardState)
    {
        ArrayList<Integer> dbBoard = toDbBoard(boardState, piece);
        
        ArrayList<Integer> entry = dbBoard;
        entry.add(index);
        
        moveLog.add(entry);
        
        controller.getGUI().buttonClick(index);
        //TODO write the move to the tracker
    }
    
    //this is called by the GUI when it's our turn to play
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
    
    //randomPlayers don't write to the database. Yet
    //depreciated. would add the item to the controllers queue anyway
    private void writeDB(int win)
    {
        
    }
}

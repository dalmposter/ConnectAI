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
public class DomsPlayer extends Player
{
    //your hard-coded piece is 5. This means that a 5 in the board array means your piece
    
    DomsPlayer(boolean isAi)
    {
        ai = isAi;
    }
    
    private int forceMove(int[][] board)
    {
        for(int i = 0; i < board.length - 3; i++)
        {
            for(int j = 0; j < board[i].length - 3; j++)
            {
                //now we're moving a 4x4 grid around the board
                //future checks will be within this grid
                
                for(int x = 0; x < 4; x++)
                {
                    int horizontal = 0;
                    int vertical = 0;
                    
                    int forceHor = -1;
                    int forceVer = -1;
                    
                    for(int y = 0; y < 4; y++)
                    {
                        if(board[y + i][x + j] == 0)
                        {
                            forceHor = y = i;
                        }
                        else horizontal += board[y + i][x + j];
                        if(board[x + i][y + j] == 0)
                        {
                            forceVer = x + i;
                        }
                        else vertical += board[x + i][y + j];
                    }
                    
                    if(horizontal == 15 || horizontal == 3)
                    {
                        return forceHor;
                    }
                    if(vertical == 15 || vertical == 3)
                    {
                        return forceVer;
                    }
                }
                
                int diagonal1 = board[i][j] + board[i + 1][j + 1] + board[i + 2][j + 2] + board[i + 3][j + 3];
                int diagonal2 = board[i][j + 3] + board[i + 1][j + 2] + board[i + 2][j + 1] + board[i + 3][j];
                
                if(diagonal1 == 3)
                {
                    return 1;
                }
                if(diagonal2 == 3)
                if(diagonal1 == 20 || diagonal2 == 20)
                {
                    return 2;
                }
            }
        }
        
        for (int[] board1 : board) {
            for (int j : board1) {
                if (j == 0) {
                    return -1;
                }
            }
        }
        
        return 0;
    }
    
    @Override
    protected void takeTurn(int[][] boardState)
    {
        //placeholder, override this and call ConnectAI.getGUI().buttonClick(x) where x is the column you wish the play in
        System.out.println("DomsPlayer called upon to take turn");
        
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
        
        ConnectAI.getGUI().buttonClick(moves.get(rand));
    }
    
    //use these to write to your database
    @Override
    protected void won()
    {
        
    }
    
    @Override
    protected void lost()
    {
    
    }
    
    @Override
    protected void drew()
    {
        
    }
}

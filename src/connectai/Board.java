/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectai;

import java.awt.Color;
import javax.swing.*;

/**
 *
 * @author dominic.cousins
 */
public class Board
{
    //array representation of board, 0 = empty, 1 = red, 5 = yellow
    int[][] board;
    //String represenation of board, 0 = empty, 1 = red, 5 = yellow
    String dbBoard;
    
    //Board constructor, initialises variables
    Board()
    {
        board = new int[7][6];
        dbBoard = "";
        
        for(int x = 0; x < board.length; x++)
        {
            for(int y = 0; y < board[x].length; y++)
            {
                board[x][y] = 0;
                dbBoard += "0";
            }
        }
        
        //System.out.println("New board created. dbBoard : " + dbBoard);
    }
    
    //convert an x and y coordinate to a position in the dbBoard String
    public static int toDbBoard(int x, int y)
    {
        int out = (7 * y) + x;
        return out;
    }
    
    //convert an index in the String board to a coordinate in the array
    public static int[] toGuiBoard(int index)
    {
        int[] out = new int[2];
        
        out[1] = index / 7;
        out[0] = index % 7;

        return out;
    }
    
    //Takes the column index, [x], and puts a number based on [piece] in the
    //first cell from the bottom in that column. Returns false if unsuccessful
    public boolean makePlay(int x, int piece)
    {
        Color col;
        //System.out.println("makePlay(" + x + ", " + piece + ") called");
        
        if(piece == 1)
        {
            col = Color.red;
        } else col = Color.yellow;
        
        if(x < 7)
        {
            for(int i = 0; i < board[x].length; i++)
            {
                if(board[x][i] == 0)
                {
                    board[x][i] = piece;
                    JPanel tile = ConnectAI.getGUI().getComponentByName("tile" + String.valueOf(x) + String.valueOf(i));
                    tile.setBackground(col);
                    return true;
                }
            }
        }
        else
        {
            System.out.println(piece + " tried to play off the board in column: " + x);
            return false;
        }
        
        System.out.println("Unknown error when placing a piece. Possibly tried an invalid column");
        return false;
    }
    
    //checks for a win on the board. -1 means the game continues
    //0 is a draw, 1 is player 1 victory, 2 is player 2 victory
    public int checkWin()
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
                    
                    for(int y = 0; y < 4; y++)
                    {
                        horizontal += board[y + i][x + j];
                        vertical += board[x + i][y + j];
                    }
                    
                    if(horizontal == 4)
                    {
                        return 1;
                    }
                    if(horizontal == 20)
                    {
                        return 2;
                    }
                    if(vertical == 4)
                    {
                        return 1;
                    }
                    if(vertical == 20)
                    {
                        return 2;
                    }
                }
                
                int diagonal1 = board[i][j] + board[i + 1][j + 1] + board[i + 2][j + 2] + board[i + 3][j + 3];
                int diagonal2 = board[i][j + 3] + board[i + 1][j + 2] + board[i + 2][j + 1] + board[i + 3][j];
                
                if(diagonal1 == 4 || diagonal2 == 4)
                {
                    return 1;
                }
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
}

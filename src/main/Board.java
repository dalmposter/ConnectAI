/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;
import java.util.logging.Level;
import javax.swing.*;

/**
 *
 * @author dominic.cousins
 */
public class Board
{
    //which thread is ours?
    private ConnectAI controller;
    //array representation of board, 0 = empty, 1 = red, 5 = yellow
    private int[][] board;
    //String represenation of board, 0 = empty, 1 = red, 5 = yellow
    private String dbBoard;
    
    //getter for board
    public int[][] getBoard()
    {
        return board;
    }
    
    //Board constructor, initialises variables
    Board(ConnectAI thread)
    {
        controller = thread;
        board = new int[7][6];
        dbBoard = "";
        
        //create board array
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
        }
        else col = Color.yellow;
        
        if(x < 7)
        {
            for(int i = 0; i < board[x].length; i++)
            {
                if(board[x][i] == 0)
                {
                    board[x][i] = piece;
                    JPanel tile = controller.getGUI().getComponentByName("tile" + String.valueOf(x) + String.valueOf(i));
                    tile.setBackground(col);
                    return true;
                }
            }
        }
        else
        {
            ConnectAI.log(Level.INFO, piece + " tried to play off the board in column: " + x);
            return false;
        }
        
        ConnectAI.log(Level.INFO, "Unknown error when placing a piece. Possibly tried an invalid column");
        return false;
    }
    
    //checks for a win on the board. -1 means the game continues
    //0 is a draw, 1 is player 1 victory, 2 is player 2 victory
    //moves a 4x4 grid around, then sums each combinations of possible
    //4 in a rown tiles to determine if there is 4 in a row
    //try to get your head around it if you dare
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
                        //we effectively consider every point [x][x] in the 4x4 grid
                        //and seperately sum the row and column it is in
                        horizontal += board[y + i][x + j];
                        vertical += board[x + i][y + j];
                    }
                    
                    //return n means player n won
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
                    //if there is an empty tile and we're here, the game goes on!
                    return -1;
                }
            }
        }
        
        //otherwise there is a tie
        return 0;
    }
}

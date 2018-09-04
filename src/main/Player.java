/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.logging.Level;

/**
 *
 * @author dominic.cousins
 */
public class Player
{
    protected ConnectAI controller;
    
    protected boolean ai;
    protected int score;
    protected int piece;
    protected int opponentPiece;
    
    //NOTES:
    //call ConnectAI.getGUI().buttonClick(x) where x is the column you wish the play in
    
    public int getPiece()
    {
        return piece;
    }
    
    public void checkPlay(int[][] boardState)
    {
        if(ai)
        {
            takeTurn(boardState);
        }
    }
    
    protected void drew()
    {
        ConnectAI.log(Level.INFO, "Draw occured");
    }
    
    protected void lost()
    {
        
    }
    
    protected void won()
    {
        ConnectAI.log(Level.INFO, this.getClass() + " won!");
        score++;
    }
    
    //This passes you the state of the board (int[x][y]) where x is the horizontal
    //dimension starting at the left and y is the vertical dimension starting at the
    //bottom override this to return intended move
    protected void takeTurn(int[][] boardState)
    {
        
    }
}

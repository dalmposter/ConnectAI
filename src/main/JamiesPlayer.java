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
public class JamiesPlayer extends Player
{    
    //there are public and static methods in the Board class to convert coordinates
    //between the format the board is stored in internally and the format I intend to
    //store the board states in the db in
    
    JamiesPlayer(boolean isAi, ConnectAI thread, String pieceIn)
    {
        ai = isAi;
        controller = thread;
        piece = pieceIn;
    }
    
    @Override
    protected void takeTurn(int[][] boardState)
    {
        //placeholder, override this and call ConnectAI.getGUI().buttonClick(x) where x is the column you wish the play in
        //System.out.println("JamiesPlayer called upon to take turn");
        
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
        
        controller.getGUI().buttonClick(moves.get(rand));
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
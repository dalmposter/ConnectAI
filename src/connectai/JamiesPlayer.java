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
public class JamiesPlayer extends Player
{    
    JamiesPlayer(boolean isAi)
    {
        ai = isAi;
    }
    
    @Override
    protected void takeTurn(int[][] boardState)
    {
        //placeholder, override this and call ConnectAI.getGUI().buttonClick(x) where x is the column you wish the play in
        System.out.println("JamiesPlayer called upon to take turn");
        
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectai;

/**
 *
 * @author dominic.cousins
 */
public class Player
{
    protected boolean ai;
    protected int score;
    
    //NOTES:
    //call ConnectAI.getGUI().buttonClick(x) where x is the column you wish the play in
    
    public void checkPlay(int[][] boardState)
    {
        if(ai)
        {
            takeTurn(boardState);
        }
    }
    
    protected void drew()
    {
        System.out.println("Draw occured");
    }
    
    protected void lost()
    {
        
    }
    
    protected void won()
    {
        System.out.println(this.getClass() + " won!");
        score++;
    }
    
    //override this to return intended move
    protected void takeTurn(int[][] boardState)
    {
        
    }
}

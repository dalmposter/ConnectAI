/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectai;

import javax.swing.*;

/**
 *
 * @author dominic.cousins
 */
public class ConnectAI {

    private static JLabel p1Score;
    private static JLabel p2Score;
    
    private static Player p1;
    private static Player p2;
    private static Board board;
    private static GUI gui;
    
    private static int maxGames = 1;
    private static int gameCount = 0;
    
    private static boolean player1 = true;
    
    public static void player1Win()
    {
        p1.won();
    }
    
    public static void player2Win()
    {
        p2.won();
    }
    
    public static Board getBoard()
    {
        return board;
    }
    
    public static GUI getGUI()
    {
        return gui;
    }
    
    public static void switchPlayer()
    {
        //System.out.println("switchPlayer() called, now : " + player1);
        player1 = !player1;
    }
    
    public static Player getPlayer1()
    {
        return p1;
    }
    
    public static Player getPlayer2()
    {
        return p2;
    }
    
    //returns true if it's player1's turn
    public static boolean getPlayer()
    {
        return player1;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        gui = new GUI();
        gui.setVisible(true);
        p1 = new JamiesPlayer(false);
        p2 = new DomsPlayer(true);
        board = new Board();
        
        JTextField text = gui.getComponentByName("cpuIterations");
        maxGames = Integer.valueOf(text.getText());
        gameCount = 0;
        
        p1Score = gui.getComponentByName("p1Score");
        p2Score = gui.getComponentByName("p2Score");
        
        System.out.println(board.checkWin());
        
        if(p1.ai && getPlayer())
        {
            p1.takeTurn(board.board);
        } else if (p2.ai && !getPlayer())
        {
            p2.takeTurn(board.board);
        }
        
        while(p1.ai && p2.ai && gameCount < maxGames)
        {
            if(player1) p1.takeTurn(board.board);
            else p2.takeTurn(board.board);
        }
    }
    
    public static void random1(boolean ai)
    {
        p1 = new RandomPlayer(ai);
    }
    
    public static void random2(boolean ai)
    {
        p2 = new RandomPlayer(ai);
    }
    
    public static void changeSettings(boolean p1Ai, boolean p2Ai, int maxG)
    {
        System.out.println("changeSettings(" + p1Ai + ", " + p2Ai + ") called");
        
        if(p1 instanceof JamiesPlayer)
        {
            p1 = new JamiesPlayer(p1Ai);
            System.out.println("p1 is JamiesPLayer");
        }
        else
        {
            p1 = new RandomPlayer(p1Ai);
            System.out.println("p1 is RandomPlayer");
        }
        if(p2 instanceof DomsPlayer)
        {
            p2 = new DomsPlayer(p2Ai);
            System.out.println("p2 is DomsPlayer");
        }
        else
        {
            p2 = new RandomPlayer(p2Ai);
            System.out.println("p2 is RandomPlayer");
        }
        
        reset();
        gameCount = 0;
        maxGames = maxG;
        
        //p1Score.setText("0");
        //p2Score.setText("0");

        if(p1.ai && getPlayer())
        {
            p1.takeTurn(board.board);
        } else if (p2.ai && !getPlayer())
        {
            p2.takeTurn(board.board);
        }
        
        while(p1.ai && p2.ai && gameCount < maxGames)
        {
            if(player1) p1.takeTurn(board.board);
            else p2.takeTurn(board.board);
        }
    }
    
    public static void reset()
    {
        board = new Board();
        
        for(int i = 0; i < 7; i++)
        {
            for(int j = 0; j < 6; j++)
            {
                JPanel tile = gui.getComponentByName("tile" + String.valueOf(i) + String.valueOf(j));
                tile.setBackground(new java.awt.Color(204, 204, 255));
            }
        }
        gameCount++;
        System.out.println("reset() called, gameCount: " + gameCount);
        //switchPlayer();
    }
    
}

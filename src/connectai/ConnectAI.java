/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectai;

import javax.swing.*;
import java.util.*;

/**
 *
 * @author dominic.cousins
 */
public class ConnectAI implements Runnable {

    private static final int MAX_THREADS = 3;
    private static final int LEARNING_GAMES = 10000;
    private static final boolean LEARNING = true;
    private Thread t;
    private String threadName;
    private final boolean even;
    public static PlayerController master;
    
    public static ArrayList<ConnectAI> threads = new ArrayList<>();
    
    private JLabel p1Score;
    private JLabel p2Score;
    
    private Player p1;
    private Player p2;
    private Board board;
    private GUI gui;
    
    private int maxGames = 1;
    private int gameCount = 0;
    
    private boolean player1 = true;
    private boolean player1Starts = true;
    
    public void player1Win()
    {
        p1.won();
    }
    
    public void player2Win()
    {
        p2.won();
    }
    
    public Board getBoard()
    {
        return board;
    }
    
    public GUI getGUI()
    {
        return gui;
    }
    
    public void switchPlayer()
    {
        //System.out.println("switchPlayer() called, now : " + player1);
        player1 = !player1;
    }
    
    public Player getPlayer1()
    {
        return p1;
    }
    
    public Player getPlayer2()
    {
        return p2;
    }
    
    //returns true if it's player1's turn
    public boolean getPlayer()
    {
        return player1;
    }
    
    ConnectAI(String name, boolean ev)
    {
        even = ev;
        this.init(name);
    }
    
    @Override
    public void run()
    {
        System.out.println("Running " + threadName);
        master.threads++;
        
        try
        {
            if(p1.ai && getPlayer())
            {
                p1.takeTurn(board.getBoard());
            } 
            else if (p2.ai && !getPlayer())
            {
                p2.takeTurn(board.getBoard());
            }
        
            while(p1.ai && p2.ai && gameCount < maxGames)
            {
                if(player1) p1.takeTurn(board.getBoard());
                else p2.takeTurn(board.getBoard());
            }
        }
        catch(Exception e)
        {
            System.out.println("Thread " + threadName + " interrupted by " + e);
        }
        finally
        {
            System.out.println("Thread " + threadName + " exiting");
            master.threads--;
        }
    }
    
    public void start()
    {
        System.out.println("Starting " + threadName);
        if(t == null)
        {
            t = new Thread(this, threadName);
            t.start();
        }
    }
    
    private void init(String name)
    {
        gui = new GUI(this);
        gui.setVisible(true);
        if(even)
        {
            if(LEARNING) p1 = new RandomPlayer(true, this, "1");
            else p1 = new JamiesPlayer(false, this, "1");
            p2 = new DomsPlayer(true, this, "5", master);
        }
        else
        {
            if(LEARNING) p2 = new RandomPlayer(true, this, "5");
            else p2 = new JamiesPlayer(false, this, "5");
            p1 = new DomsPlayer(true, this, "1", master);
        }
        board = new Board(this);
        
        threadName = name;
        System.out.println(name + " initialised, p1: " + p1.getClass() + ", p2: " + p2.getClass());
        
        JTextField text = gui.getComponentByName("cpuIterations");
        maxGames = Integer.valueOf(text.getText());
        if(LEARNING)
        {
            maxGames = LEARNING_GAMES;
            text.setText(String.valueOf(maxGames));
        }
        gameCount = 0;
        
        p1Score = gui.getComponentByName("p1Score");
        p2Score = gui.getComponentByName("p2Score");
        
        //System.out.println(board.checkWin());
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        threads = new ArrayList<>();
        master = new PlayerController("ThreadController");
        
        if(LEARNING)
        {
            System.out.println("In learning mode");
            for(int i = 0; i < MAX_THREADS; i++)
            {
                threads.add(new ConnectAI("Thread" + i, i % 2 == 0));
                threads.get(i).start();
            }
        }
        else
        {
            System.out.println("In play mode");
            threads.add(new ConnectAI("The One", true));
            threads.get(0).start();
        }

        master.start();
    }
    
    public void random1(boolean ai)
    {
        p1 = new RandomPlayer(ai, this, "1");
    }
    
    public void random2(boolean ai)
    {
        p2 = new RandomPlayer(ai, this, "5");
    }
    
    public void changeSettings(boolean p1Ai, boolean p2Ai, int maxG)
    {
        System.out.println("changeSettings(" + p1Ai + ", " + p2Ai + ") called");
        
        if(p1 instanceof JamiesPlayer)
        {
            p1 = new JamiesPlayer(p1Ai, this, "1");
            System.out.println("p1 is JamiesPLayer");
        }
        else
        {
            p1 = new RandomPlayer(p1Ai, this, "1");
            System.out.println("p1 is RandomPlayer");
        }
        if(p2 instanceof DomsPlayer)
        {
            p2 = new DomsPlayer(p2Ai, this, "5", master);
            System.out.println("p2 is DomsPlayer");
        }
        else
        {
            p2 = new RandomPlayer(p2Ai, this, "5");
            System.out.println("p2 is RandomPlayer");
        }
        
        reset();
        gameCount = 0;
        maxGames = maxG;
        
        //p1Score.setText("0");
        //p2Score.setText("0");

        if(p1.ai && getPlayer())
        {
            p1.takeTurn(board.getBoard());
        } else if (p2.ai && !getPlayer())
        {
            p2.takeTurn(board.getBoard());
        }
        
        while(p1.ai && p2.ai && gameCount < maxGames)
        {
            if(player1) p1.takeTurn(board.getBoard());
            else p2.takeTurn(board.getBoard());
        }
    }
    
    public void reset()
    {
        board = new Board(this);
        
        for(int i = 0; i < 7; i++)
        {
            for(int j = 0; j < 6; j++)
            {
                JPanel tile = gui.getComponentByName("tile" + String.valueOf(i) + String.valueOf(j));
                tile.setBackground(new java.awt.Color(204, 204, 255));
            }
        }
        gameCount++;
        //System.out.println("reset() called by " + threadName + ", gameCount: " + gameCount);
        
        //alternate starting player each game. switchPlayer() is called after reset()
        //so we set it to ![the intended player]
        player1Starts = !player1Starts;
        player1 = !player1Starts;
        //System.out.println("Player 1 starts? " + player1Starts);
        //switchPlayer();
    }
    
}

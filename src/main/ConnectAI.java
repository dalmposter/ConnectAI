/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.InputStream;
import javax.swing.*;
import java.util.*;
import java.util.logging.*;
/**
 *
 * @author dominic.cousins
 */
public class ConnectAI implements Runnable {

    private static Logger logger;
    
    //final variables are hard coded attributes of the training process. Self explanatory
    private static final int MAX_THREADS = 3;
    //amount of games each thread will play before ending
    private static final int LEARNING_GAMES = 2;
    private static final boolean LEARNING = true;
    //play randomly instead of from database?
    private static final boolean RANDOM_LEARNING = false;
    
    //thread identity
    public static ArrayList<ConnectAI> threads = new ArrayList<>();
    private Thread t;
    private String threadName;
    
    //variable to determine which players bot goes first
    //by deciding whether the thread number is even or odd
    private final boolean even;
    public static PlayerController master;
    
    //variables to control rate of play on the fly to balance the queue size
    private static int sleepTime = 500;
    private static int lastQueue = 0;
    //change how agressively the queue is managed
    private static final int MULTIPLYER = 5;
    private static final int QUEUE_AIM = 50;
    
    private Player p1;
    private Player p2;
    private Board board;
    private GUI gui;
    
    private int maxGames = 1;
    private int gameCount = 0;
    
    //is it player1's turn?
    private boolean player1 = true;
    //does player1 start this game?
    private boolean player1Starts = true;
    
    static
    {
        //get logging.properties from resources. DON'T TOUCH THIS WAS FUCKING DIFFICULT TO CONFIGURE
        InputStream stream = ConnectAI.class.getClassLoader().getResourceAsStream("main/resources/logging.properties");
        
        try
        {
            //send properties to the LogManager
            LogManager.getLogManager().readConfiguration(stream);
        }
        catch(java.io.IOException e)
        {
            System.out.println("Error reading log config " + e);
        }
    }
    
    //returns the amount of games being played per thread. Depreciated
//    public static int getMaxGames()
//    {
//        return LEARNING_GAMES;
//    }
    
    public String getName()
    {
        return threadName;
    }
    
    public static boolean getLearning()
    {
        return LEARNING;
    }
    
    //adjust the wait time between taking turns. Dynamic to ensure db write queue
    //is always populated but not so much so that we run out of RAM
    //adjust relevant variables at top to change how much this is managed
    public static void adjustSleepTime(int queue)
    {
        double aim = ((double)QUEUE_AIM) / (double)400;
        ConnectAI.log(Level.INFO, "ADJUSTING QUEUE, AIM/400 : " + aim);
        if(queue == 0)
        {
            if(sleepTime - 20 > 0)
            {
                sleepTime -= 20 * MULTIPLYER;
                ConnectAI.log(Level.INFO, "Queue was empty. Waiting 10 seconds and reducing think time by " + String.valueOf(20 * MULTIPLYER) + "ms");
            }
        }
        else if(queue < 20 * aim)
        {
            if(sleepTime - 7 > 0)
            {
                sleepTime -= 7 * MULTIPLYER;
                ConnectAI.log(Level.INFO, queue + " items in queue , reduced think time by " + String.valueOf(7 * MULTIPLYER) + "ms");
            }
        }
        else if(queue < 75 * aim)
        {
            if(sleepTime - 4 > 0 && queue < lastQueue)
            {
                sleepTime -= 4 * MULTIPLYER;
                ConnectAI.log(Level.INFO, queue + " items in queue, reduced think time by " + String.valueOf(4 * MULTIPLYER) + "ms");
            }
        }
        else if(queue < 300 * aim)
        {
            if(sleepTime - 2 > 0 && queue < lastQueue) 
            {
                sleepTime -= 2 * MULTIPLYER;
                ConnectAI.log(Level.INFO, queue + " items in queue, reduced think time by " + String.valueOf(2 * MULTIPLYER) + "ms");
            }
        }
        else if(queue > 1250 * aim)
        {
            if(queue > lastQueue)
            {
                sleepTime += 10 * MULTIPLYER;
                ConnectAI.log(Level.INFO, queue + " items in queue, increased sleep time by " + String.valueOf(10 * MULTIPLYER) + "ms");
            }
        }
        else if(queue > 750 * aim)
        {
            if(queue > lastQueue)
            {
                sleepTime += 5 * MULTIPLYER;
                ConnectAI.log(Level.INFO, queue + " items in queue, increasd think time by " + String.valueOf(5 * MULTIPLYER) + "ms");
            }
        }
        else if(queue > 500 * aim)
        {
            if(queue > lastQueue)
            {
                sleepTime += 2 * MULTIPLYER;
                ConnectAI.log(Level.INFO, queue + " items in queue, increased think time by " + String.valueOf(2 * MULTIPLYER) + "ms");
            }
        }
        
        //keep a record so we don't rubberband between large and small queues
        //if we want to reduce the queue size but it's already on it's way down, don't touch it, and vice versa
        lastQueue = queue;
    }
    
    //depreciated. Still here incase we need them
//    public void player1Win()
//    {
//        p1.won();
//    }
//    
//    public void player2Win()
//    {
//        p2.won();
//    }
    
    //getters for related instances
    public Board getBoard()
    {
        return board;
    }
    
    public GUI getGUI()
    {
        return gui;
    }
    
        
    public Player getPlayer1()
    {
        return p1;
    }
    
    public Player getPlayer2()
    {
        return p2;
    }
    
    //change whos turn it is
    public void switchPlayer()
    {
        //System.out.println("switchPlayer() called, now : " + player1);
        player1 = !player1;
    }
    
    //returns true if it's player1's turn
    public boolean getPlayer()
    {
        return player1;
    }
    
    //most initialiasation is in init() as it then starts doing things
    ConnectAI(String name, boolean ev)
    {
        even = ev;
        //this.init(name);
        
        gui = new GUI(this);
        gui.setVisible(true);
        if(even)
        {
            if(LEARNING)
            {
                p1 = new RandomPlayer(true, this, 1);
                gui.setP1Name("Random");
            }
            else
            {
                p1 = new JamiesPlayer(false, this, 1);
                gui.setP1Name("JamiesPlayer/Human");
            }
            p2 = new DomsPlayer(true, this, 5, master, RANDOM_LEARNING);
            gui.setP2Name("DomsPlayer");
        }
        else
        {
            if(LEARNING)
            {
                p2 = new RandomPlayer(true, this, 5);
                gui.setP2Name("RandomPlayer");
            }
            else 
            {
                p2 = new JamiesPlayer(false, this, 5);
                gui.setP2Name("JamiesPlayer/Human");
            }
            p1 = new DomsPlayer(true, this, 1, master, RANDOM_LEARNING);
            gui.setP1Name("DomsPlayer");
        }
        board = new Board(this);
        
        threadName = name;
        ConnectAI.log(Level.INFO, name + " initialised, p1: " + p1.getClass() + ", p2: " + p2.getClass());
        
        JTextField text = gui.getComponentByName("cpuIterations");
        maxGames = Integer.valueOf(text.getText());
        if(LEARNING)
        {
            maxGames = LEARNING_GAMES;
            text.setText(String.valueOf(maxGames));
        }
        gameCount = 0;
    }
    
    //the "main" of threads
    //just a loop to make each ai take a turn, otherwise it just waits for user input
    @Override
    public void run()
    {
        ConnectAI.log(Level.INFO, "Running " + threadName);
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
                Thread.sleep(2);
            }
        }
        catch(Exception e)
        {
            ConnectAI.log(Level.SEVERE, "Thread " + threadName + " interrupted by " + e);
            ConnectAI.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            master.publiclyTriggeredWrite();
            System.exit(0);
        }
        finally
        {
            ConnectAI.log(Level.INFO, "Thread " + threadName + " exiting");
            master.threads--;
        }
    }
    
    //initialise threads if they don't start for some reason
    public void start()
    {
        ConnectAI.log(Level.INFO, "Starting " + threadName);
        if(t == null)
        {
            t = new Thread(this, threadName);
            t.start();
        }
    }
    
    //public method to write to the log
    //if weird things are happening uncomment the println to see in netbeans
    public static void log(Level level, String message)
    {
        logger.log(level, message);
        //System.out.println(level + ": " + message);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        //create directory in which to store logs if not exists
        String path = System.getProperty("user.home") + "\\ConnectAI\\Logs";
        new File(path).mkdirs();
        //logger to show where the logs are if they are causing problems
        //log(Level.INFO, "file dir of logs : " + path);
        
        //setup the threads and the controller/db-writer
        threads = new ArrayList<>();
        master = new PlayerController("ThreadController");
        
        //create logger
        logger = Logger.getLogger("Log");
        
        //if we're training start a fuck tonne of threads
        if(LEARNING)
        {
            ConnectAI.log(Level.INFO, "In learning mode");
            for(int i = 0; i < MAX_THREADS; i++)
            {
                threads.add(new ConnectAI("Thread" + i, i % 2 == 0));
                threads.get(i).start();
            }
        }
        //otherwise we just need one. We always need the controller as well
        else
        {
            ConnectAI.log(Level.INFO, "In play mode");
            threads.add(new ConnectAI("The One", true));
            threads.get(0).start();
        }
        //kick off the controller
        master.start();
    }
    
    //set player 1 to be a RandomPlayer
    public void random1(boolean ai)
    {
        p1 = new RandomPlayer(ai, this, 1);
    }
    
    //set player 2 to be a RandomPlayer
    public void random2(boolean ai)
    {
        p2 = new RandomPlayer(ai, this, 5);
    }
    
    //update method to reinstantiate most things when settings are changed
    public void changeSettings(boolean p1Ai, boolean p2Ai, int maxG)
    {
        ConnectAI.log(Level.INFO, "changeSettings(" + p1Ai + ", " + p2Ai + ") called");
        
        //create new instances of relevant players
        if(p1 instanceof JamiesPlayer)
        {
            p1 = new JamiesPlayer(p1Ai, this, 1);
            ConnectAI.log(Level.INFO, "p1 is JamiesPLayer");
        }
        else
        {
            p1 = new RandomPlayer(p1Ai, this, 1);
            ConnectAI.log(Level.INFO, "p1 is RandomPlayer");
        }
        if(p2 instanceof DomsPlayer)
        {
            p2 = new DomsPlayer(p2Ai, this, 5, master, RANDOM_LEARNING);
            ConnectAI.log(Level.INFO, "p2 is DomsPlayer");
        }
        else
        {
            p2 = new RandomPlayer(p2Ai, this, 5);
            ConnectAI.log(Level.INFO, "p2 is RandomPlayer");
        }
        
        //reset everything
        reset();
        gameCount = 0;
        maxGames = maxG;
        
        //I don't want to reset the score but I used to so here that is
        //p1Score.setText("0");
        //p2Score.setText("0");

        //starting player, take your turn
        if(p1.ai && getPlayer())
        {
            p1.takeTurn(board.getBoard());
        }
        else if (p2.ai && !getPlayer())
        {
            p2.takeTurn(board.getBoard());
        }
        
        //game loop
        while(p1.ai && p2.ai && gameCount < maxGames)
        {
            if(!LEARNING)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception e)
                {
                    log(Level.SEVERE, threadName + " interrupted during sleep time in game loop by " + e);
                }
            }
            if(player1) p1.takeTurn(board.getBoard());
            else p2.takeTurn(board.getBoard());
        }
    }
    
    //reset the relevate variables to start a new game
    public void reset()
    {
        board = new Board(this);
        
        //reset tile colours
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
        try
        {
            Thread.sleep(sleepTime);
        }
        catch (InterruptedException e)
        {
            ConnectAI.log(Level.INFO, threadName + " Interrupted. Resuming");
        }
        //alternate starting player each game. switchPlayer() is called after reset()
        //so we set it to ![the intended player]
        player1Starts = !player1Starts;
        player1 = !player1Starts;
        //System.out.println("Player 1 starts? " + player1Starts);
        //switchPlayer();
    }
    
}

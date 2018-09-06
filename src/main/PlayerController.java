/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.*;

/**
 *
 * @author DominicLocal
 */
public class PlayerController implements Runnable {

    //sql variables
    private Connection connect = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    ArrayList<QueueItem> queue = new ArrayList<>();
    
    //do we add writes to a queue or just write on the fly?
    private static final boolean queueing = true;
    
    //amount of time to wait before first write cycle
    //smaller if the ratio of power between your cpu and drive is high
    private static final int INITIAL_WAIT = 10000;
    
    //thread identity
    private Thread t;
    private String threadName;
    
    //game so far
    private int soFar = 0;
    
    //this thread needs to end itself if there are no other threads
    public int threads = 0;
    
    //"main" of threads
    @Override
    public void run()
    {
        try
        {
            //let's give the squad time to bootup
            Thread.sleep(5000);
        } catch (InterruptedException e)
        {
            ConnectAI.log(Level.INFO, threadName + " interrupted from sleep, can continue");
        }
        do
        {
            try
            {
                ConnectAI.log(Level.INFO, threadName + ": running");
                //let's let a bit of a queue build up
                Thread.sleep(INITIAL_WAIT);
                while(threads > 0 || !ConnectAI.getLearning())
                {
                    //manage queue size on the fly
                    ConnectAI.adjustSleepTime(queue.size());
                    //if there's a queue, write it
                    if(queue.size() > 0)
                    {
                        ConnectAI.log(Level.INFO, threadName + ": Wrote " + queue.size() + " to db");
                        //soFar += queue.size();
                        triggerWrite();
                        ConnectAI.log(Level.INFO, "Finished writing");
                        //ConnectAI.log(Level.INFO, soFar + " games so far. NOT ACCURATE IF ERROR");
                    }
                    //if not why the fuck not? let's give the bois some time to get in shape
                    else
                    {
                        ConnectAI.log(Level.INFO, "Did not write to db as queue is empty");
                        Thread.sleep(10000);
                    }
                }
                //if there's no more games being played do one final write then end yourself
                ConnectAI.log(Level.INFO, threadName + ": There are no other threads, exiting and writing " + queue.size());
                triggerWrite();
            }
            catch(InterruptedException e)
            {
                ConnectAI.log(Level.INFO, "EXCEPTION: " + e + ", Controller thread interrupted");
            }
            catch(Exception e)
            {
                ConnectAI.log(Level.INFO, "UNEXPECTED EXCEPTION: " + e + " from " + threadName);
            }
            finally
            {
                ConnectAI.log(Level.INFO, "There are " + threads + " threads still alive");
            }
        }
        while(threads > 0 || !ConnectAI.getLearning());
        ConnectAI.log(Level.INFO, "The controller has stopped. The program will exit");
        close(); //close any connections if necessary
        System.exit(0); //0 denotes normal exit of program
    }
    
    //constructor
    PlayerController(String name)
    {
        threadName = name;
    }
    
    //if the thread doesn't start, DO IT!
    public void start()
    {
        ConnectAI.log(Level.INFO, "Starting " + threadName);
        if(t == null)
        {
            t = new Thread(this, threadName);
            t.start();
        }
    }
    
    //public method to add items to the queue
    public void addToQueue(int win, ArrayList<ArrayList<Integer>> moveTracker)
    {
        soFar++;
        if(queueing)
        {
            queue.add(new QueueItem(win, moveTracker));
            ConnectAI.log(Level.INFO,"Just added item to queue, soFar : " + soFar);
        }
        else 
        {
            ConnectAI.log(Level.INFO,"Just wrote an item, soFar : " + soFar);
            writeDB(win, moveTracker);
        }
        
    }
    
    //don't really want to trigger the write from elsewhere but here just incase
    public void publiclyTriggeredWrite()
    {
        if(queue.size() > 0) triggerWrite();
    }
    
    //dump the queue in the database. This is gonna be a long dump . . .
    private void triggerWrite()
    {
        try
        {
            //one connection per batch
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/connectdb?" + "user=sqluser&password=sqluserpw&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT&useSSL=false&allowPublicKeyRetrieval=true");
            ArrayList<QueueItem> currentQueue = queue;
            queue = new ArrayList<>();
            currentQueue.forEach((item) -> {
                writeDB(item.getWin(), item.getMoves());
            });
        }
        catch (Exception e)
        {
            ConnectAI.log(Level.INFO, threadName + ": EXCEPTION:");
            e.printStackTrace();
        }
        finally
        {
            //close that shit
            close();
        }
    }
    
    //function to write the entry for a single game to the database
    private void writeDB(int win, ArrayList<ArrayList<Integer>> moveTracker)
    {
        int wins = 0;
        int losses = 0;
        int draws = 0;
        
        //decide what the result was
        switch (win)
        {
            case 1:
                wins = 1;
                break;
            case 0:
                draws = 1;
                break;
            default:
                losses = 1;
                break;
        }

        //write a row for each move
        for(int i = 0; i < moveTracker.size(); i++)
        {
            int rows = 0;
            try
            {
                //log(Level.INFO, "Row for " + moveTracker.get(i).get(0) + ", " + moveTracker.get(i).get(1) + " exists. Adding to it");
                if(connect == null)
                {
                    try
                    {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        connect = DriverManager.getConnection("jdbc:mysql://localhost/connectdb?" + "user=sqluser&password=sqluserpw&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT&useSSL=false&allowPublicKeyRetrieval=true");
                    }
                    catch(Exception e)
                    {
                        ConnectAI.log(Level.SEVERE, "Couldnt intialise connection to db : " + e);
                        ConnectAI.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
                    }
                }
                //first try to update the row
                preparedStatement = connect.prepareStatement("UPDATE boardstates SET wins = wins + ?, losses = losses + ?, draws = draws + ? WHERE move = ? AND column0 = ? AND column1 = ? AND column2 = ? AND column3 = ? AND column4 = ? AND column5 = ? AND column6 = ?");
                preparedStatement.setInt(1, wins);
                preparedStatement.setInt(2, losses);
                preparedStatement.setInt(3, draws);
                preparedStatement.setInt(4, moveTracker.get(i).get(7));
                for(int j = 0; j < 7; j++)
                {
                    preparedStatement.setInt(j + 5, moveTracker.get(i).get(j));
                }
                ConnectAI.log(Level.INFO, "writeDB() about to execute : " + preparedStatement);
                rows = preparedStatement.executeUpdate();
            }
            catch (SQLException e)
            {
                //presumable the update was rejected because there was no row to update. Inserting . . .
                ConnectAI.log(Level.WARNING, "Failed to update : " + e);
                ConnectAI.log(Level.WARNING, Arrays.toString(e.getStackTrace()));
                try
                {
                    //prep an insert
                    preparedStatement = connect.prepareStatement("insert into boardStates values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                    for(int j = 0; j < 7; j++)
                    {
                        preparedStatement.setInt(j + 1, moveTracker.get(i).get(j));
                    }
                    preparedStatement.setInt(8, moveTracker.get(i).get(7));
                    preparedStatement.setInt(9, wins);
                    preparedStatement.setInt(10, losses);
                    preparedStatement.setInt(11, draws);
                    ConnectAI.log(Level.INFO, "writeDB() about to execute : " + preparedStatement);
                    rows = preparedStatement.executeUpdate();
                }
                catch (SQLException f)
                {
                    //something bad happened
                    ConnectAI.log(Level.WARNING, threadName + " couldn't insert into or update the database, something has gone very wrong : " + f);
                    ConnectAI.log(Level.SEVERE, Arrays.toString(f.getStackTrace()));
                }
            }
            
            if(rows == 0)
            {
                try
                {
                    //prep an insert
                    preparedStatement = connect.prepareStatement("insert into boardStates values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                    for(int j = 0; j < 7; j++)
                    {
                        preparedStatement.setInt(j + 1, moveTracker.get(i).get(j));
                    }
                    preparedStatement.setInt(8, moveTracker.get(i).get(7));
                    preparedStatement.setInt(9, wins);
                    preparedStatement.setInt(10, losses);
                    preparedStatement.setInt(11, draws);
                    ConnectAI.log(Level.INFO, "writeDB() about to execute : " + preparedStatement);
                    preparedStatement.executeUpdate();
                }
                catch(Exception e)
                {
                    ConnectAI.log(Level.SEVERE, "writeDB(): tried updating entry, no entry found. Tried adding entry, failed : " + e);
                    ConnectAI.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
                }
            }
        }
        
        try
        {
            if(wins > 0) preparedStatement = connect.prepareStatement("INSERT INTO games (win) VALUES (1)");
            else if(losses > 0) preparedStatement = connect.prepareStatement("INSERT INTO games (loss) VALUES (1)");
            else preparedStatement = connect.prepareStatement("INSERT INTO games (draw) VALUES (1)");
            
            ConnectAI.log(Level.INFO, "About to execute : " + preparedStatement);
            preparedStatement.executeUpdate();
        }
        catch(Exception e)
        {
            ConnectAI.log(Level.SEVERE, "Error inserting final game record : " + e);
            ConnectAI.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
        }
    }
    
    //close all database related variables
    private void close()
    {
        try
        {
            if(resultSet != null)
            {
                resultSet.close();
            }
            if(preparedStatement != null)
            {
                preparedStatement.close();
            }
            if(connect != null)
            {
                connect.close();
            }
        }
        catch(Exception e)
        {
            //well this can't be good
            ConnectAI.log(Level.SEVERE, "EXCEPTION : " + e + " in close()");
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.sql.*;
import java.util.ArrayList;
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
        while(threads > 0 || !ConnectAI.getLearning())
        {
            try
            {
                ConnectAI.log(Level.INFO, threadName + ": running");
                //let's let a bit of a queue build up
                Thread.sleep(INITIAL_WAIT);
                while(threads > 0 || !ConnectAI.getLearning())
                {
                    //manage queue size on the fly
                    //ConnectAI.adjustSleepTime(queue.size());
                    //if there's a queue, write it
                    if(queue.size() > 0)
                    {
                        ConnectAI.log(Level.INFO, threadName + ": Wrote " + queue.size() + " to db");
                        soFar += queue.size();
                        triggerWrite();
                        ConnectAI.log(Level.INFO, "Finished writing");
                        ConnectAI.log(Level.INFO, soFar + " games so far. NOT ACCURATE IF ERROR");
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
    public void addToQueue(int win, ArrayList<ArrayList<String>> moveTracker, String piece)
    {
        queue.add(new QueueItem(win, moveTracker, piece));
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
                writeDB(item.getWin(), item.getMoves(), item.getPiece());
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
    private void writeDB(int win, ArrayList<ArrayList<String>> moveTracker, String piece)
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
            try
            {
                //log(Level.INFO, "Row for " + moveTracker.get(i).get(0) + ", " + moveTracker.get(i).get(1) + " exists. Adding to it");
                
                //first try to update the row
                preparedStatement = connect.prepareStatement("update boardStates set wins = wins + ?, losses = losses + ?, draws = draws + ? where state = ? and move = ? and piece = ?");
                preparedStatement.setInt(1, wins);
                preparedStatement.setInt(2, losses);
                preparedStatement.setInt(3, draws);
                preparedStatement.setString(4, moveTracker.get(i).get(0));
                preparedStatement.setInt(5, Integer.valueOf(moveTracker.get(i).get(1)));
                preparedStatement.setString(6, piece);

                preparedStatement.executeUpdate();
            }
            catch (SQLException e)
            {
                //presumable the update was rejected because there was no row to update. Inserting . . .
                try
                {
                    //prep an insert
                    preparedStatement = connect.prepareStatement("insert into boardStates values (?, ?, ?, ?, ?, ?);");
                    preparedStatement.setString(1, moveTracker.get(i).get(0));
                    preparedStatement.setInt(2, Integer.valueOf(moveTracker.get(i).get(1)));
                    preparedStatement.setString(3, piece);
                    preparedStatement.setInt(4, wins);
                    preparedStatement.setInt(5, losses);
                    preparedStatement.setInt(6, draws);
                    
                    preparedStatement.executeUpdate();
                }
                catch (SQLException f)
                {
                    //something bad happened
                    ConnectAI.log(Level.INFO, threadName + " couldn't insert into or update the database, something has gone very wrong : " + f);
                }
            }
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

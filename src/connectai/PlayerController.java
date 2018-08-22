/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectai;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author DominicLocal
 */
public class PlayerController implements Runnable {
    
    private Connection connect = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    ArrayList<QueueItem> queue = new ArrayList<>();
    private Thread t;
    private String threadName;
    
    public int threads = 0;
    
    @Override
    public void run()
    {
        try
        {
            System.out.println(threadName + ": running");
            Thread.sleep(10000);
            while(threads > 0)
            {
                if(queue.size() > 0)
                {
                    System.out.println(threadName + ": Wrote " + queue.size() + " to db");
                    triggerWrite();
                    System.out.println("Finished writing");
                }
                else System.out.println("Did not write to db as queue is empty");
                Thread.sleep(10000);
            }
            System.out.println(threadName + ": There are no other threads, exiting and writing " + queue.size());
            triggerWrite();
        }
        catch(InterruptedException e)
        {
            System.out.println("EXCEPTION: " + e + ", Controller thread interrupted");
        }
        finally
        {
            System.out.println("The controller has stopped. The program will exit");
            close(); //close any connections if necessary
            System.exit(0); //0 denotes normal exit of program
        }
    }
    
    PlayerController(String name)
    {
        threadName = name;
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
    
    public void addToQueue(int win, ArrayList<ArrayList<String>> moveTracker, String piece)
    {
        queue.add(new QueueItem(win, moveTracker, piece));
    }
    
    public void triggerWrite()
    {
        try
        {
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
            System.out.println(threadName + ": EXCEPTION: " + e);
        }
        finally
        {
            close();
        }
    }
    
    private void writeDB(int win, ArrayList<ArrayList<String>> moveTracker, String piece)
    {
        int wins = 0;
        int losses = 0;
        int draws = 0;
        
        switch (win) {
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

        for(int i = 0; i < moveTracker.size(); i++)
        {
            try
            {
            //System.out.println("Row for " + moveTracker.get(i).get(0) + ", " + moveTracker.get(i).get(1) + " exists. Adding to it");
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
                try
                {
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
                    System.out.println(threadName + " couldn't insert into or update the database, something has gone very wrong : " + f);
                }
            }
        }
    }
    
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
           //System.out.println("EXCEPTION : " + e + " in close()");
        }
    }
}

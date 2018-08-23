/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.*;
/**
 *
 * @author DominicLocal
 */
//small class to contain all information relating to a completed game
public class QueueItem
{
    private int win;
    private ArrayList<ArrayList<String>> moveTracker;
    private String piece;
    
    //constructor. There's no logic here
    QueueItem(int winner, ArrayList<ArrayList<String>> moves, String playerPiece)
    {
        win = winner;
        moveTracker = moves;
        piece = playerPiece;
    }
    
    //it's all getters mate
    public int getWin()
    {
        return win;
    }
    
    public ArrayList<ArrayList<String>> getMoves()
    {
        return moveTracker;
    }
    
    public String getPiece()
    {
        return piece;
    }
}

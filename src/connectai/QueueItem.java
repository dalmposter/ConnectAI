/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectai;

import java.util.*;
/**
 *
 * @author DominicLocal
 */
public class QueueItem {
    private int win;
    private ArrayList<ArrayList<String>> moveTracker;
    private String piece;
    
    QueueItem(int winner, ArrayList<ArrayList<String>> moves, String playerPiece)
    {
        win = winner;
        moveTracker = moves;
        piece = playerPiece;
    }
    
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

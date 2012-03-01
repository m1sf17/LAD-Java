/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lad.java;

/**
 *
 * @author Kevin
 */
public class Minion {
    int exp = 0; 
    int level = 1;
    
    //Constructor
    public Minion()
    { 
        
    }
    
    //setters
    public void setLevel(int l)
    {
      if (exp >= 10)
      {
          level++; 
      }
    }
    
    public void setExp(int e)
    {
        
    }
    
    //getters
    public int getLevel()
    {
        return level; 
    }
    
    public int getExp()
    {
        return exp;
    }
}

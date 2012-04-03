package lad.java;

import lad.db.MySQLDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Data handler for minions
 *
 * @author msflowers
 * @author Kevin
 */
public class Minion
{
    /**
     * Current experience of the minion
     */
    int exp = 0;

    /**
     * Current level of the minion
     */
    int level = 0;

    /**
     * ID of the minion
     */
    int ID = 0;

    /**
     * Owner of the minion
     */
    int owner = 0;

    /**
     * Statement for inserting a new minion
     */
    static PreparedStatement stmt = null;
    
    // Ctor (Adding to DB)
    public Minion()
    { 
        
    }

    // Ctor (from DB)
    public Minion( int n_exp, int n_level, int n_ID, int n_owner )
    {
        exp = n_exp;
        level = n_level;
        ID = n_ID;
        owner = n_owner;
    }
    
    /**
     * Sets the level
     *
     * @param l The new level
     */
    public void setLevel( int l )
    {
        level = l;
    }

    /**
     * Sets the experience
     *
     * @param e The new experience
     */
    public void setExp(int e)
    {
        exp = e;
    }
    
    /**
     * Gets the level
     *
     * @return level
     */
    public int getLevel()
    {
        return level; 
    }

    /**
     * Gets the experience
     *
     * @return exp
     */
    public int getExp()
    {
        return exp;
    }

    /**
     * Gets the ID
     *
     * @return ID
     */
    public int getID()
    {
        return ID;
    }

    /**
     * Gets the owner
     *
     * @return owner
     */
    public int getOwner()
    {
        return owner;
    }

    /**
     *  Adds the given amount of experience.
     *  Levels the minion if the appropriate amount of experience has been met.
     *
     * @param xp Experience to add
     */
    public void adjustExp( int xp )
    {
        exp += xp;
        if( xp >= 10 )
        {
            level++;
            exp -= 10;
        }
    }
    /**
     * Creates a new minion and adds it to the DB in the process
     *
     * @param owner The owner of the minion
     */
    public static Minion create( int owner )
    {
        Minion ret = new Minion();
        Connection conn = MySQLDB.getConn();
        ResultSet generatedKeys = null;
        try
        {
            // Initialize statement
            if( stmt == null )
            {
                stmt = conn.prepareStatement( "INSERT INTO MINIONS VALUES( NULL, ?, ?, ? )", Statement.RETURN_GENERATED_KEYS );
            }

            // Set statement values
            stmt.setValue( 1, owner );
            stmt.setValue( 2, 0 );
            stmt.setValue( 3, 0 );

            // Validate it works/run it
            int affectedRows = preparedStatement.executeUpdate();
            if( affectedRows == 0 )
            {
                throw new SQLException( "Creating minion failed, no rows affected." );
            }

            // Get the ID
            generatedKeys = stmt.getGeneratedKeys();
            if( generatedKeys.next() )
            {
                ret.ID = generatedKeys.getLong( 1 );
            }
            else
            {
                throw new SQLException( "Creating minion failed, no key returned." );
            }
        }
        catch( SQLException e )
        {
            System.err.println( "Error while creating minion: " + e.toString() );
            System.exit( -1 );
        }
        return ret;
    }
}

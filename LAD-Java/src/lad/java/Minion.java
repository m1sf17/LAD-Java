package lad.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lad.db.MySQLDB;

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
    private int exp = 0;

    /**
     * Current level of the minion
     */
    private int level = 0;

    /**
     * ID of the minion
     */
    private int ID = 0;

    /**
     * Owner of the minion
     */
    private int owner = 0;

    /**
     * Statement for inserting a new minion
     */
    private static PreparedStatement insertStmt = null;

    /**
     * Statement for deleting a minion
     */
    private static PreparedStatement deleteStmt = null;

    /**
     * Statement for updating Level
     */
    private static PreparedStatement updateLevelStmt = null;

    /**
     * Statement for updating Exp
     */
    private static PreparedStatement updateExpStmt = null;

    /**
     * Statement for updating Owner
     */
    private static PreparedStatement updateOwnerStmt = null;

    /**
     * Statement for adjusting Exp
     */
    private static PreparedStatement adjustExpStmt = null;

    /**
     * Prepares all of the prepared statements
     *
     * @throws SQLException If an error occurs when preparing the statements
     */
    public static void prepareStatements() throws SQLException
    {
        Connection conn = MySQLDB.getConn();

        final String pre = "UPDATE MINIONS SET ";
        final String post = " WHERE ID = ?";

        updateLevelStmt = conn.prepareStatement( pre + "LEVEL = ?" + post );
        updateExpStmt = conn.prepareStatement( pre + "EXP = ?" + post );
        updateOwnerStmt = conn.prepareStatement( pre + "OWNER = ?" + post );
        adjustExpStmt = conn.prepareStatement( pre + "LEVEL = ?, EXP = ?" +
                                               post );
        deleteStmt = conn.prepareStatement( "DELETE FROM MINIONS" + post );
        insertStmt = conn.prepareStatement(
            "INSERT INTO MINIONS VALUES( NULL, ?, ?, ? )",
            Statement.RETURN_GENERATED_KEYS );
    }

    /**
     * Ctor (Adding to DB)
     */
    public Minion()
    { 
        
    }

    /**
     * Ctor (from DB)
     *
     * @param n_ID ID of the minion
     * @param n_exp Experience of the minion
     * @param n_level Level of the minion
     * @param n_owner Owner of the minion
     */
    public Minion( int n_ID, int n_exp, int n_level, int n_owner )
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

        try
        {
            updateLevelStmt.setInt( 1, l );
            updateLevelStmt.setInt( 2, ID );

            MySQLDB.delaySQL( updateLevelStmt );
        }
        catch( SQLException e )
        {
            System.err.println( "Error while setting minion level." +
                                e.toString() );
            System.exit( -1 );
        }
    }

    /**
     * Sets the experience
     *
     * @param e The new experience
     */
    public void setExp( int e )
    {
        exp = e;

        try
        {
            updateExpStmt.setInt( 1, e );
            updateExpStmt.setInt( 2, ID );

            MySQLDB.delaySQL( updateExpStmt );
        }
        catch( SQLException x )
        {
            System.err.println( "Error while setting minion level." +
                                x.toString() );
            System.exit( -1 );
        }
    }

    /**
     * Sets the owner
     *
     * @param o The new owner
     */
    public void setOwner( int o )
    {
        owner = o;

        try
        {
            updateOwnerStmt.setInt( 1, o );
            updateOwnerStmt.setInt( 2, ID );

            MySQLDB.delaySQL( updateOwnerStmt );
        }
        catch( SQLException e )
        {
            System.err.println( "Error while setting minion level." +
                                e.toString() );
            System.exit( -1 );
        }
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
        while( exp >= 10 )
        {
            level++;
            exp -= 10;
        }

        try
        {
            adjustExpStmt.setInt( 1, level );
            adjustExpStmt.setInt( 2, exp );
            adjustExpStmt.setInt( 3, ID );

            MySQLDB.delaySQL( adjustExpStmt );
        }
        catch( SQLException e )
        {
            System.err.println( "Error while setting minion level." +
                                e.toString() );
            System.exit( -1 );
        }
    }
    /**
     * Creates a new minion and adds it to the DB in the process
     *
     * @param owner The owner of the minion
     * @return The created minion
     */
    public static Minion create( int owner )
    {
        Minion ret = new Minion();
        ResultSet generatedKeys;
        try
        {
            // Set statement values
            insertStmt.setInt( 1, owner );
            insertStmt.setInt( 2, 0 );
            insertStmt.setInt( 3, 0 );

            // Validate it works/run it
            int affectedRows = insertStmt.executeUpdate();
            if( affectedRows == 0 )
            {
                throw new SQLException( "Creating minion failed, no rows affected." );
            }

            // Get the ID
            generatedKeys = insertStmt.getGeneratedKeys();
            if( generatedKeys.next() )
            {
                ret.ID = generatedKeys.getInt( 1 );
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

    /**
     * Destroys the minion in the database
     */
    void destroy()
    {
        try
        {
            deleteStmt.setInt( 1, ID );

            // Run the delete
            int affectedRows = deleteStmt.executeUpdate();

            if( affectedRows == 0 )
            {
                throw new SQLException( "No rows were deleted." );
            }
        }
        catch( SQLException e )
        {
            System.err.println( "Error while deleting minion: " + e.toString() );
            System.exit( -1 );
        }

        owner = ID = exp = level = 0;
    }
}

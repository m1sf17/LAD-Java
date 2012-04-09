package lad.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.db.MySQLDB;

/**
 * Data handler for trainers
 */
public class Trainer
{

    /**
     * Current amount of experience the trainer has
     */
    private int exp = 0;

    /**
     * Current level the trainer is
     */
    private int level = 0;

    /**
     * ID of the trainer
     */
    private int ID;

    /**
     * Owner of the trainer. Referenced from the ID from the
     * PHP server.
     */
    private int owner;

    /**
     * List of minions that this trainer owns
     */
    private LinkedList< Minion > minionList = null;

    /**
     * Prepared statement for pulling all the minions for a trainer
     */
    private PreparedStatement minionStmt = null;

    /**
     * Statement for inserting a new trainer
     */
    private static PreparedStatement insertStmt = null;

    /**
     * Statement for deleting a trainer
     */
    private static PreparedStatement deleteStmt = null;

    /**
     * Statement for deleting a trainer's minions
     */
    private static PreparedStatement deleteMinionStmt = null;

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

        final String pre = "UPDATE TRAINERS SET ";
        final String post = " WHERE ID = ?";

        updateLevelStmt = conn.prepareStatement( pre + "LEVEL = ?" + post );
        updateExpStmt = conn.prepareStatement( pre + "EXP = ?" + post );
        updateOwnerStmt = conn.prepareStatement( pre + "OWNER = ?" + post );
        adjustExpStmt = conn.prepareStatement( pre + "LEVEL = ?, EXP = ?" +
                                               post );
        deleteStmt = conn.prepareStatement( "DELETE FROM TRAINERS" + post );
        deleteMinionStmt = conn.prepareStatement( "DELETE FROM MINIONS WHERE " +
                                                  "OWNER = ?" );
        insertStmt = conn.prepareStatement(
                        "INSERT INTO TRAINERS VALUES( NULL, ?, ?, ? )",
                        Statement.RETURN_GENERATED_KEYS );
    }

    /**
     * Ctor (from DB)
     *
     * @param n_exp   Experience of the trainer
     * @param n_level Level of the trainer
     * @param n_ID    ID of the trainer
     * @param n_owner Owner of the trainer
     */
    public Trainer( int n_ID, int n_owner, int n_exp, int n_level )
    {
        minionList = new LinkedList<>();
        exp = n_exp;
        level = n_level;
        ID = n_ID;
        owner = n_owner;
    }

    /**
     * Ctor (add to DB)
     *
     * @param n_owner Owner of the trainer to add
     */
    public Trainer( int n_owner )
    {
        minionList = new LinkedList<>();
    }

    /**
     * Loads minions from DB
     */
    public void load()
    {
        Connection conn = MySQLDB.getConn();
        ResultSet result;
        try
        {
            // Make sure the statement is prepared
            if( minionStmt == null )
            {
                minionStmt = conn.prepareStatement( "SELECT * FROM MINIONS " +
                                                    "WHERE owner = ?" );
            }

            // Set up the statement
            minionStmt.setLong( 1, owner );

            // Run the statement
            result = minionStmt.executeQuery();

            while( result.next() )
            {
                // Pull the values
                int minionid = result.getInt( "id" );
                int minionexp = result.getInt( "exp" );
                int minionlevel = result.getInt( "level" );

                // Add the minion to our list
                minionList.add( new Minion( minionid, minionexp,
                                            minionlevel, ID ) );
            }
        }
        catch( SQLException e )
        {
            System.err.println( "Error while getting trainer minions:" +
                                e.toString() );
            System.exit( -1 );
        }
    }

    /**
     * Get experience
     *
     * @return exp
     */
    public int getExp()
    {
        return exp;
    }

    /**
     * Get level
     *
     * @return level
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Get ID
     *
     * @return ID
     */
    public int getID()
    {
        return ID;
    }

    /**
     * Get Owner
     *
     * @return owner
     */
    public int getOwner()
    {
        return owner;
    }

    /**
     * Set Exp
     *
     * @param e New exp
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
     * Set level
     *
     * @param l New level
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
     * Set owner
     *
     * @param o New owner
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
     * Gets the list of minions
     *
     * @return minionList
     */
    List< Minion > getMinions()
    {
        return Collections.unmodifiableList( minionList );
    }

    /**
     * Adds a minion to the list
     *
     * @param minion The minion to add to this trainer's list
     */
    public void addMinion( Minion minion )
    {
        minionList.add( minion );
    }

    /**
     * Adds exp and updates the level accordingly
     *
     * @param xp The exp to add
     */
    public void adjustExp( int xp )
    {
        exp += xp;
        while( exp >= 10 )
        {
            exp -= 10;
            level++;
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
     * Battles two minions.
     * The losing minion will be destroyed and removed from this trainer.
     *
     * @param minion1 The first minion to battle
     * @param minion2 The second minion to battle
     */
    public void battle( Minion minion1, Minion minion2 )
    {
        // Fancy formula to decide a winner
        int level1 = minion1.getLevel();
        int level2 = minion2.getLevel();
        Minion winner;
        Minion loser;
        int random = (int)( Math.random() * ( level1 + level2 ) );
        if( random <= level1 )
        {
            winner = minion1;
            loser = minion2;
        }
        else
        {
            winner = minion2;
            loser = minion1;
        }

        // The amount of exp gained
        int gainedExp = (int)( ( loser.getExp() + ( loser.getLevel() * 10 ) ) *
                               0.2 );

        // Remove the loser from our list
        ListIterator< Minion> iter = minionList.listIterator();
        while( iter.hasNext() )
        {
            Minion minionTester = iter.next();
            if( minionTester.getID() == loser.getID() )
            {
                iter.remove();
                break;
            }
        }

        // Kill the loser
        loser.destroy();

        // Update the winner
        winner.adjustExp( gainedExp );

        // Update the trainer
        adjustExp( gainedExp );
    }

    /**
     * Creates a trainer
     *
     * @param n_owner Owner of the trainer
     * @return The created trainer
     */
    public static Trainer create( int n_owner )
    {
        Trainer trainer = new Trainer( n_owner );
        ResultSet generatedKeys;
        try
        {
            // Set statement values
            insertStmt.setInt( 1, n_owner );
            insertStmt.setInt( 2, 0 );
            insertStmt.setInt( 3, 0 );

            // Validate it works/run it
            int affectedRows = insertStmt.executeUpdate();
            if( affectedRows == 0 )
            {
                throw new SQLException( 
                        "Creating trainer failed, no rows affected." );
            }

            // Get the ID
            generatedKeys = insertStmt.getGeneratedKeys();
            if( generatedKeys.next() )
            {
                trainer.ID = generatedKeys.getInt( 1 );
            }
            else
            {
                throw new SQLException( 
                        "Creating trainer failed, no key returned." );
            }
        }
        catch( SQLException e )
        {
            System.err.println( "Error while creating minion: " + e.toString() );
            System.exit( -1 );
        }
        return trainer;
    }

    /**
     * Destroys the trainer in the database
     */
    void destroy()
    {
        // Ensure the delete statement is prepared
        try
        {
            deleteStmt.setInt( 1, ID );
            deleteMinionStmt.setInt( 1, ID );

            // Run the delete
            int affectedRows = deleteStmt.executeUpdate();

            if( affectedRows == 0 )
            {
                throw new SQLException( "No rows were deleted." );
            }

            // Minion statement does not need to be checked
            deleteMinionStmt.executeUpdate();
        }
        catch( SQLException e )
        {
            System.err.println( "Error while deleting trainer: " + e.toString() );
            System.exit( -1 );
        }

        owner = ID = exp = level = 0;
    }
}
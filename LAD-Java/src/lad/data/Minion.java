package lad.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lad.db.MySQLDB;
import lad.db.TableProfile;

/**
 * Data handler for minions
 *
 * @author msflowers
 * @author Kevin
 */
public class Minion implements TableProfile
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
     * Ctor (Adding to DB)
     */
    private Minion()
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
     * Ctor (Copy)
     *
     * @param other Minion to copy data from
     */
    public Minion( Minion other )
    {
        exp = other.exp;
        level = other.level;
        ID = other.ID;
        owner = other.owner;
    }
    
    /**
     * Sets the level
     *
     * @param value Value to set the level to
     * @throws GameException Thrown if the update fails
     */
    public void setLevel( int value )
    {
        level = value;

        try
        {
            updateLevelStmt.setInt( 1, value );
            updateLevelStmt.setInt( 2, ID );

            MySQLDB.delaySQL( updateLevelStmt );
        }
        catch( SQLException e )
        {
            throw new GameException( 3, "Error while setting minion level." +
                                        e.getMessage() );
        }
    }

    /**
     * Sets the experience
     *
     * @param value Value to set the experience to
     * @throws GameException Thrown if the update fails
     */
    public void setExp( int value )
    {
        exp = value;

        try
        {
            updateExpStmt.setInt( 1, value );
            updateExpStmt.setInt( 2, ID );

            MySQLDB.delaySQL( updateExpStmt );
        }
        catch( SQLException x )
        {
            throw new GameException( 3, "Error while setting minion exp." +
                                        x.getMessage() );
        }
    }

    /**
     * Sets the owner
     *
     * @param value Value to set the owner to
     * @throws GameException Thrown if the update fails
     */
    public void setOwner( int value )
    {
        owner = value;

        try
        {
            updateOwnerStmt.setInt( 1, value );
            updateOwnerStmt.setInt( 2, ID );

            MySQLDB.delaySQL( updateOwnerStmt );
        }
        catch( SQLException e )
        {
            throw new GameException( 3, "Error while setting minion owner." +
                                        e.getMessage() );
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
     * Gets the total amount of EXP earned
     *
     * @return Formula using both Level and EXP to determine total EXP.
     */
    public int getTotalEXP()
    {
        return ( level * 10 ) + exp + (int)
               ( ( (float)level / 2 ) * (float)( level + 1 ) );
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
     * @throws GameException Thrown if the update fails
     */
    public void adjustExp( int xp )
    {
        exp += xp;
        while( exp >= ( 10 + level ) && level < 26 )
        {
            exp -= ( 10 + level );
            level++;
        }

        if( level == 26 )
        {
            exp = 0;
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
            throw new GameException( 3, "Error while setting minion level." +
                                        e.getMessage() );
        }
    }

    /**
     * Creates a new minion and adds it to the DB in the process
     *
     * @param owner The owner of the minion
     * @return The created minion
     * @throws GameException Thrown if the creation fails
     */
    public static Minion create( int owner )
    {
        Minion ret = new Minion();
        ResultSet generatedKeys;
        try
        {
            // Set statement values
            insertStmt.setInt( 1, owner );

            // Validate it works/run it
            int affectedRows = insertStmt.executeUpdate();
            if( affectedRows == 0 )
            {
                throw new SQLException( "Creating minion failed, " +
                                        "no rows affected." );
            }

            // Get the ID
            generatedKeys = insertStmt.getGeneratedKeys();
            if( generatedKeys.next() )
            {
                ret.ID = generatedKeys.getInt( 1 );
            }
            else
            {
                throw new SQLException( "Creating minion failed, " +
                                        "no key returned." );
            }
        }
        catch( SQLException e )
        {
            throw new GameException( 3, e.getMessage() );
        }
        return ret;
    }

    /**
     * Destroys the minion in the database
     *
     * @throws GameException Thrown if the destruction fails
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
            throw new GameException( 3, e.getMessage());
        }

        owner = ID = exp = level = 0;
    }

    /**
     * Returns a dummy object
     *
     * @return Dummy Object
     */
    public static TableProfile getProfile()
    {
        return new Minion();
    }

    /**
     * Gets the name of the table
     *
     * @return MINIONS
     */
    @Override
    public String tableName()
    {
        return "MINIONS";
    }

    /**
     * Gets the string used to create the table
     *
     * @return Creation string
     */
    @Override
    public String createString()
    {
        return
            "CREATE TABLE `MINIONS` (" +
            "`ID` int(10) unsigned NOT NULL AUTO_INCREMENT," +
            "`owner` int(10) unsigned NOT NULL," +
            "`exp` int(10) unsigned NOT NULL," +
            "`level` int(10) unsigned NOT NULL," +
            "PRIMARY KEY (`ID`)" +
            ") ENGINE = MyISAM DEFAULT CHARSET=latin1";
    }

    /**
     * Gets the headers of the SQL table
     *
     * @return [ID,owner,exp,level]
     */
    @Override
    public String[] tableHeaders()
    {
        return new String[] { "ID", "owner", "exp", "level" };
    }

    /**
     * Empty because loadData is false.
     *
     * Data is loaded from trainer class.
     *
     * @param rs Unused parameter
     */
    @Override
    public void loadRow( ResultSet rs )
    {
        // Not run because loadData is false
    }

    /**
     * Prepares all of the SQL statements.
     *
     * @throws SQLException Thrown if an error occurs while preparing
     */
    @Override
    public void postinit() throws SQLException
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
            "INSERT INTO MINIONS VALUES( NULL, ?, 0, 0 )",
            Statement.RETURN_GENERATED_KEYS );
    }

    /**
     * Tells the TableManager not to load data from this table because it is
     * handled elsewhere.
     *
     * @return false
     */
    @Override
    public boolean loadData()
    {
        return false;
    }
}

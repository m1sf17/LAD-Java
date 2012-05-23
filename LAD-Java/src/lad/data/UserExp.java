package lad.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lad.db.EXPManager;
import lad.db.MySQLDB;
import lad.db.TableProfile;

/**
 * Data class for the user's experience
 *
 * @author msflowers
 */
public class UserExp implements TableProfile
{
    /**
     * ID of the User that the EXP belongs to
     */
    private int owner;

    /**
     * Target of the EXP
     */
    private UserExpTarget target;

    /**
     * Type of the EXP
     */
    private ModifierTarget type;

    /**
     * Level of the EXP
     */
    private int level;

    /**
     * Remainder experience for this EXP
     */
    private int exp;

    /**
     * Total experience that has been earned
     */
    private int totalExp;

    /**
     * List of points required to advance levels at the current level
     */
    private final List< Integer > bonusLevels = new ArrayList<>( 10 );

    /**
     * Statement for inserting a new trainer
     */
    private static PreparedStatement insertStmt = null;

    /**
     * Statement for updating Level
     */
    private static PreparedStatement updateStmt = null;

    /**
     * Statement for deleting a trainer
     */
    private static PreparedStatement deleteStmt = null;

    /**
     * Ctor (from DB)
     *
     * @param owner Owner of the experience
     * @param target Target of the experience
     * @param type Type of the experience
     * @param level Level of the experience
     * @param exp Remaining experience
     * @param totalexp Total experience earned
     */
    public UserExp( int owner, int target, int type, int level, int exp,
                    int totalexp )
    {
        this.owner = owner;
        this.target = UserExpTarget.fromInt( target );
        this.type = ModifierTarget.fromInt( type );
        this.level = level;
        this.exp = exp;
        this.totalExp = totalexp;
        updateBonuses();
    }

    /**
     * Ctor (add to DB)
     *
     * @param owner Owner of the experience to add
     * @param target Target of the experience to add
     * @param type Type of the experience to add
     */
    private UserExp( int owner, int target, int type )
    {
        this.owner = owner;
        this.target = UserExpTarget.fromInt( target );
        this.type = ModifierTarget.fromInt( type );
    }

    /**
     * Ctor (dummy)
     */
    private UserExp()
    {
        this.owner = -1;
    }

    /**
     * @return the owner
     */
    public int getOwner()
    {
        return owner;
    }

    /**
     * @return the target
     */
    public UserExpTarget getTarget()
    {
        return target;
    }

    /**
     * @return the level
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * @return the exp
     */
    public int getExp()
    {
        return exp;
    }

    /**
     * @return the total exp
     */
    public int getTotalExp()
    {
        return totalExp;
    }

    /**
     * @return the type
     */
    public ModifierTarget getType()
    {
        return type;
    }

    /**
     * Gets the array of bonus levels
     *
     * @return Bonus level array
     */
    public List< Integer > getBonusLevels()
    {
        return Collections.unmodifiableList( this.bonusLevels );
    }

    /**
     * @param level the level to set
     * @throws GameException Thrown if a failure occurs while updating
     */
    public void setLevel( int level )
    {
        this.level = level;
        runUpdate();
        updateBonuses();
    }

    /**
     * @param exp the exp to set
     * @throws GameException Thrown if a failure occurs while updating
     */
    public void setExp( int exp )
    {
        this.totalExp += exp - this.exp;
        this.exp = exp;
        runUpdate();
        updateBonuses();
    }

    /**
     * @param level The new level to set
     * @param exp The new exp to set
     * @throws GameException Thrown if a failure occurs while updating
     */
    public void setValues( int level, int exp )
    {
        this.level = level;
        this.exp = exp;
        runUpdate();
        updateBonuses();
    }

    /**
     * Simply calls SQL update with appropriate values
     *
     * @throws GameException Thrown if a failure occurs while updating
     */
    private void runUpdate()
    {
        try
        {
            updateStmt.setInt( 1, this.level );
            updateStmt.setInt( 2, this.exp );
            updateStmt.setInt( 3, this.totalExp );
            updateStmt.setInt( 4, this.owner );
            updateStmt.setInt( 5, this.target.getValue() );
            updateStmt.setInt( 6, this.getType().getValue() );

            MySQLDB.delaySQL( updateStmt );
        }
        catch( SQLException e )
        {
            throw new GameException( 3, "Error while updating user EXP." +
                                     e.getMessage() );
        }
    }

    /**
     * Creates a new EXP attribute and adds it to the DB in the process
     *
     * @param owner Owner of the EXP
     * @param target Target of the EXP
     * @param type Type of the EXP
     * @return The created EXP block
     * @throws GameException Thrown if a failure occurs while creating
     */
    public static UserExp create( int owner, int target, int type )
    {
        UserExp ret = new UserExp( owner, target, type );
        try
        {
            insertStmt.setInt( 1, owner );
            insertStmt.setInt( 2, target );
            insertStmt.setInt( 3, type );

            int affectedRows = insertStmt.executeUpdate();
            if( affectedRows == 0 )
            {
                throw new SQLException( "Creating EXP failed, duplicate key?" );
            }
        }
        catch( SQLException e )
        {
            throw new GameException( 3, "Error while creating exp: " +
                                     e.getMessage() );
        }

        return ret;
    }

    /**
     * Deletes all of the experience belonging to a specific user.
     *
     * @param owner Owner of the EXP
     * @throws GameException Thrown if a failure occurs while deleting
     */
    public static void deleteByUser( int owner )
    {
        try
        {
            deleteStmt.setInt( 1, owner );
            deleteStmt.executeUpdate();
            // Don't need to make sure some were deleted since it's possible
            // the user didn't have any
        }
        catch( SQLException e )
        {
            throw new GameException( 3, "Error while deleting exp: " +
                                     e.getMessage() );
        }
    }
    /**
     * Returns the amount of experience required for the next level of this
     * block.
     *
     * This function does not calculate the remaining experience required, only
     * the total experience required for the next level.
     *
     * @see lad.data.UserExp#getExpRemainingNextLevel()
     * @return EXP required for next level.
     */
    public int getExpRequiredNextLevel()
    {
        return EXPManager.expRequiredAtLevel( this.level );
    }

    /**
     * Returns the remaining amount of experience required for the next level
     * of this block.
     *
     * This function will never return a value less than 0.  If 0 is returned,
     * it means this block is ready to be leveled.
     *
     * @see lad.data.UserExp#getExpRequiredNextLevel()
     * @return EXP remaining for next level.
     */
    public int getExpRemainingNextLevel()
    {
        int ret = getExpRequiredNextLevel() - this.exp;
        if( ret < 0 )
        {
            ret = 0;
        }
        return ret;
    }

    /**
     * Clears and then refills the bonus levels array
     */
    private void updateBonuses()
    {
        int bonusLevel = 0, bonusMultiplier = 1, bonusExp = this.exp,
            expRequired = EXPManager.expRequiredAtLevel( this.level );

        this.bonusLevels.clear();
        
        while( bonusExp > expRequired )
        {
            if( bonusLevel > 1 )
            {
                bonusMultiplier++;
            }
            bonusLevel += bonusMultiplier;
            bonusExp -= expRequired;
            this.bonusLevels.add( expRequired );
            expRequired = EXPManager.expRequiredAtLevel( this.level +
                                                         bonusLevel );
        }
    }

    /**
     * Returns a dummy object
     *
     * @return Dummy Object
     */
    public static TableProfile getProfile()
    {
        return new UserExp();
    }

    /**
     * Gets the name of the table
     *
     * @return USEREXP
     */
    @Override
    public String tableName()
    {
        return "USEREXP";
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
            "CREATE TABLE `USEREXP` (" +
            "`owner` int(10) unsigned NOT NULL," +
            "`target` int(10) unsigned NOT NULL," +
            "`type` int(10) unsigned NOT NULL," +
            "`level` int(10) unsigned NOT NULL," +
            "`exp` int(10) unsigned NOT NULL," +
            "`totalexp` int(10) unsigned NOT NULL," +
            "PRIMARY KEY (`owner`,`target`,`type`)" +
            ") ENGINE = MyISAM DEFAULT CHARSET=latin1";
    }

    /**
     * Gets the headers of the SQL table
     *
     * @return [owner,target,type,level,exp,totalexp]
     */
    @Override
    public String[] tableHeaders()
    {
        return new String[]{ "owner", "target", "type",
                                "level", "exp", "totalexp" };
    }

    /**
     * Loads a row from the database and adds it to the manager
     *
     * @param rs Result row from SQL database
     * @throws SQLException Thrown if there is an error reading the SQL
     */
    @Override
    public void loadRow( ResultSet rs ) throws SQLException
    {
        int n_owner = rs.getInt( 1 );
        int n_target = rs.getInt( 2 );
        int n_type = rs.getInt( 3 );
        int n_level = rs.getInt( 4 );
        int n_exp = rs.getInt( 5 );
        int n_totalexp = rs.getInt( 6 );

        UserExp userexp = new UserExp( n_owner, n_target, n_type,
                                        n_level, n_exp, n_totalexp );
        EXPManager.getInstance().addEXP( userexp );
    }

    /**
     * Prepares all of the prepared statements
     *
     * @throws SQLException If an error occurs when preparing the statements
     */
    @Override
    public void postinit() throws SQLException
    {
        Connection conn = MySQLDB.getConn();

        updateStmt = conn.prepareStatement( "UPDATE USEREXP SET LEVEL = ?, " +
                                            "EXP = ?, TOTALEXP = ? WHERE " +
                                            "OWNER = ? AND TARGET = ? AND " +
                                            "TYPE = ?" );
        deleteStmt = conn.prepareStatement( "DELETE FROM USEREXP WHERE OWNER " +
                                            "= ?");
        insertStmt = conn.prepareStatement(
                        "INSERT INTO USEREXP VALUES( ?, ?, ?, 0, 0, 0 )" );
    }

    /**
     * Tells the table manager to load this data from the database.
     *
     * @return true
     */
    @Override
    public boolean loadData()
    {
        return true;
    }
}

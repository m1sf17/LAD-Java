package lad.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import lad.db.EXPManager;
import lad.db.MySQLDB;

/**
 * Class for the various types of Experience a user can have.
 *
 * @author msflowers
 */
public class UserExp
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
     * Calculated value for remaining amount of exp for bonus levels
     */
    private int bonusExp;

    /**
     * Calculated value for levels able to gain via bonus route
     */
    private int bonusLevel;

    /**
     * Calculated value for offset via bonus route
     */
    private int bonusMultiplier;

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
     * Prepares all of the prepared statements
     *
     * @throws SQLException If an error occurs when preparing the statements
     */
    public static void prepareStatements() throws SQLException
    {
        Connection conn = MySQLDB.getConn();

        updateStmt = conn.prepareStatement( "UPDATE USEREXP SET LEVEL = ?, " +
                                            "EXP = ?, TOTALEXP = ? WHERE " +
                                            "OWNER = ? AND TARGET = ? AND " +
                                            "TYPE = ?" );
        deleteStmt = conn.prepareStatement( "DELETE FROM USEREXP WHERE OWNER " +
                                            "= ?");
        insertStmt = conn.prepareStatement(
                        "INSERT INTO USEREXP VALUES( ?, ?, ?, 0, 0, 0 )",
                        Statement.RETURN_GENERATED_KEYS );
    }

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
        refreshBonuses();
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
     * @return the bonusExp
     */
    public int getBonusExp()
    {
        return bonusExp;
    }

    /**
     * @return the bonusLevel
     */
    public int getBonusLevel()
    {
        return bonusLevel;
    }

    /**
     * @return the bonusMultiplier
     */
    public int getBonusMultiplier()
    {
        return bonusMultiplier;
    }

    /**
     * @param level the level to set
     */
    public void setLevel( int level )
    {
        this.level = level;
        runUpdate();
        updateBonuses();
    }

    /**
     * @param exp the exp to set
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
            System.err.println( "Error while updating user EXP." +
                                e.toString() );
            System.exit( -1 );
        }
    }

    /**
     * Creates a new EXP attribute and adds it to the DB in the process
     *
     * @param owner Owner of the EXP
     * @param target Target of the EXP
     * @param type Type of the EXP
     * @return The created EXP block
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
            System.err.println( "Error while creating exp: " + e.toString() );
            System.exit( -1 );
        }

        ret.refreshBonuses();
        return ret;
    }

    /**
     * Returns the amount of experience required for the next level of this
     * block.
     *
     * This function does not calculate the remaining experience required, only
     * the total experience required for the next level.
     *
     * @see lad.java.UserExp#getExpRemainingNextLevel()
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
     * @see lad.java.UserExp#getExpRequiredNextLevel()
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
     * Updates all of the bonus fields from scratch
     */
    private void refreshBonuses()
    {
        this.bonusExp = this.exp - getExpRequiredNextLevel();
        this.bonusMultiplier = 0;
        this.bonusLevel = 0;
        updateBonuses();
    }

    /**
     * Calculates all of the bonus fields.
     */
    private void updateBonuses()
    {
        int expRequired =
                EXPManager.expRequiredAtLevel( this.level + this.getBonusLevel() );
        while( this.getBonusExp() > expRequired )
        {
            if( this.getBonusLevel() > 1 )
            {
                this.bonusMultiplier++;
            }
            this.bonusLevel += this.getBonusMultiplier();
            this.bonusExp -= expRequired;

            expRequired = EXPManager.expRequiredAtLevel( this.level +
                                                         this.getBonusLevel() );
        }
    }
}

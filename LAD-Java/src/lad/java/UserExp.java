package lad.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
                                            "EXP = ? WHERE OWNER = ? AND " +
                                            "TARGET = ? AND TYPE = ?");
        deleteStmt = conn.prepareStatement( "DELETE FROM USEREXP WHERE OWNER " +
                                            "= ?");
        insertStmt = conn.prepareStatement(
                        "INSERT INTO USEREXP VALUES( ?, ?, ?, ?, ? )",
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
     */
    public UserExp( int owner, int target, int type, int level, int exp )
    {
        this.owner = owner;
        this.target = UserExpTarget.fromInt( target );
        this.type = ModifierTarget.fromInt( type );
        this.level = level;
        this.exp = exp;
    }

    /**
     * Ctor (add to DB)
     *
     * @param owner Owner of the experience to add
     * @param target Target of the experience to add
     * @param type Type of the experience to add
     */
    public UserExp( int owner, int target, int type )
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
     * @return the type
     */
    public ModifierTarget getType()
    {
        return type;
    }

    /**
     * @param level the level to set
     */
    public void setLevel( int level )
    {
        this.level = level;
        runUpdate();
    }

    /**
     * @param exp the exp to set
     */
    public void setExp( int exp )
    {
        this.exp = exp;
        runUpdate();
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
            updateStmt.setInt( 3, this.owner );
            updateStmt.setInt( 4, this.target.getValue() );
            updateStmt.setInt( 5, this.getType().getTarget() );

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
            insertStmt.setInt( 4, 0 );
            insertStmt.setInt( 5, 0 );

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

        return ret;
    }
}

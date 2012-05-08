package lad.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lad.db.MySQLDB;

/**
 * Data handler for trainer battle statistics
 *
 * @author msflowers
 */
public class TrainerBattleStats
{
    // Fields
    /**
     * Type of statistic
     *
     * 1 = User, 2 = Trainer
     */
    private int type;

    /**
     * ID of the target of the statistic
     */
    private int id;

    /**
     * Number of times gun was fired
     */
    private int shotsFired;

    /**
     * Amount of damage dealt
     */
    private double damageDealt;

    /**
     * Amount of damage taken
     */
    private double damageTaken;

    /**
     * Number of times gun was reloaded
     */
    private int reloads;

    /**
     * Number of times gun shot hit the enemy
     */
    private int shotsHit;

    /**
     * Distance moved
     */
    private double distanceMoved;

    /**
     * Number of shots evaded to prevent damage
     */
    private int shotsEvaded;

    /**
     * Amount of damage reduced by shielding
     */
    private double damageReduced;

    /**
     * Number of critical hits landed
     */
    private int criticalsHit;

    /**
     * Number of times shot without being shot at
     */
    private int safelyShot;

    /**
     * Number of battles taken part of
     */
    private int battles;

    /**
     * Number of battles won
     */
    private int battlesWon;

    /**
     * Statement for inserting a new statistic block
     */
    private static PreparedStatement insertStmt = null;

    /**
     * Statement for deleting a statistic block
     */
    private static PreparedStatement deleteStmt = null;

    /**
     * Statement for updating
     */
    private static PreparedStatement updateStmt = null;

    /**
     * Prepares all of the prepared statements
     *
     * @throws SQLException If an error occurs when preparing the statements
     */
    public static void prepareStatements() throws SQLException
    {
        Connection conn = MySQLDB.getConn();

        updateStmt = conn.prepareStatement( "UPDATE TRAINERBATTLESTATS SET " +
            "shotsFired = ?, damageDealt = ?, reloads = ?, shotsHit = ?, " +
            "distanceMoved = ?, shotsEvaded = ?, damageReduced = ?, " +
            "criticalsHit = ?, safelyShot = ?, battles = ?, battlesWon = ? " +
            "WHERE type = ? AND id = ?" );
        deleteStmt = conn.prepareStatement( "DELETE FROM TRAINERBATTLESTATS " +
                                            "WHERE type = ? AND id = ?" );
        insertStmt = conn.prepareStatement( "INSERT INTO TRAINERBATTLESTATS " +
            "VALUES( ?, ?, 0, 0, 0, 0.0, 0.0, 0, 0," +
                    "0.0, 0, 0.0, 0, 0, 0, 0 )" );
    }

    /**
     * Ctor (Adding to DB)
     *
     * @param type Type of statistic
     * @param id   ID of the target of the statistic
     */
    private TrainerBattleStats( int type, int id )
    {
        this.type = type;
        this.id = id;
    }

    /**
     * Ctor (from DB)
     *
     * @param type    Type of statistic
     * @param id      ID of the target of the statistic
     * @param ints    Array of all the integer values
     * @param doubles Array of all the double values
     * @throws GameException Thrown if either of the arrays are an incorrect
     *                       length
     */
    public TrainerBattleStats( int type, int id, int ints[], double doubles[] )
    {
        this.type = type;
        this.id = id;
        if( ints.length != 8 )
        {
            throw new GameException( 4, "Invalid battle statistic integers." );
        }
        if( doubles.length != 4 )
        {
            throw new GameException( 4, "Invalid battle statistic doubles." );
        }

        this.shotsFired   = ints[ 0 ];
        this.reloads      = ints[ 1 ];
        this.shotsHit     = ints[ 2 ];
        this.shotsEvaded  = ints[ 3 ];
        this.criticalsHit = ints[ 4 ];
        this.safelyShot   = ints[ 5 ];
        this.battles      = ints[ 6 ];
        this.battlesWon   = ints[ 7 ];

        this.damageDealt  = doubles[ 0 ];
        this.damageTaken  = doubles[ 1 ];
        this.distanceMoved = doubles[ 2 ];
        this.damageReduced = doubles[ 3 ];
    }

    /**
     * Updates all of the statistics by adding the values to each.
     *
     * @param ints    Integer values to add in to the statistic block
     * @param doubles Double values to add in to the statistic block
     * @throws GameException Thrown if either of the arrays are an incorrect
     *                       length
     */
    public void addValues( int ints[], double doubles[] )
    {
        // Validate lengths
        if( ints.length != 8 )
        {
            throw new GameException( 4, "Invalid battle statistic integers." );
        }
        if( doubles.length != 4 )
        {
            throw new GameException( 4, "Invalid battle statistic doubles." );
        }

        // Add in values
        this.shotsFired   += ints[ 0 ];
        this.reloads      += ints[ 1 ];
        this.shotsHit     += ints[ 2 ];
        this.shotsEvaded  += ints[ 3 ];
        this.criticalsHit += ints[ 4 ];
        this.safelyShot   += ints[ 5 ];
        this.battles      += ints[ 6 ];
        this.battlesWon   += ints[ 7 ];

        this.damageDealt  += doubles[ 0 ];
        this.damageTaken  += doubles[ 1 ];
        this.distanceMoved += doubles[ 2 ];
        this.damageReduced += doubles[ 3 ];

        // Commit values to DB
        try
        {
            updateStmt.setInt( 1, this.shotsFired );
            updateStmt.setDouble( 2, this.damageDealt );
            updateStmt.setInt( 3, this.reloads );
            updateStmt.setInt( 4, this.shotsHit );
            updateStmt.setDouble( 5, this.distanceMoved );
            updateStmt.setInt( 6, this.shotsEvaded );
            updateStmt.setDouble( 7, this.damageReduced );
            updateStmt.setInt( 8, this.criticalsHit );
            updateStmt.setInt( 9, this.safelyShot );
            updateStmt.setInt( 10, this.battles );
            updateStmt.setInt( 11, this.battlesWon );
            updateStmt.setInt( 12, this.type );
            updateStmt.setInt( 13, this.id );

            MySQLDB.delaySQL( updateStmt );
        }
        catch( SQLException e )
        {
            throw new GameException( 3, "Error while updating statistic " +
                                        "block." + e.getMessage() );
        }
    }

    /**
     * Creates a new statistic block and adds it to the DB in the process
     *
     * @param type Type of the target of the block
     * @param id   ID of the target of the block
     * @return Created statistic block
     * @throws GameException Thrown if the creation fails
     */
    public static TrainerBattleStats create( int type, int id )
    {
        try
        {
            // Set statement values
            insertStmt.setInt( 1, type );
            insertStmt.setInt( 2, id );

            // Validate it works/run it
            int affectedRows = insertStmt.executeUpdate();
            if( affectedRows == 0 )
            {
                throw new SQLException( "Creating trainer statistic block " +
                                        "failed, no rows affected." );
            }
        }
        catch( SQLException e )
        {
            throw new GameException( 3, e.getMessage() );
        }
        return new TrainerBattleStats( type, id );
    }

    /**
     * Destroys the statistic block in the database
     *
     * @throws GameException Thrown if the destruction fails
     */
    void destroy()
    {
        try
        {
            deleteStmt.setInt( 1, type );
            deleteStmt.setInt( 2, id );

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

        battles = battlesWon = criticalsHit = id = reloads = safelyShot =
                  shotsEvaded = shotsFired = shotsHit = type = 0;
        damageDealt = damageReduced = damageTaken = distanceMoved = 0.0;
    }
}

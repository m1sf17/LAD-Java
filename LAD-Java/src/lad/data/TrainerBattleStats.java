package lad.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lad.db.EXPManager;
import lad.db.MySQLDB;
import lad.db.TableProfile;

/**
 * Data handler for trainer battle statistics
 *
 * @author msflowers
 */
public class TrainerBattleStats implements TableProfile
{
    // Fields
    /**
     * Type of statistic.
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
            updateStmt.setDouble( 3, this.damageTaken );
            updateStmt.setInt( 4, this.reloads );
            updateStmt.setInt( 5, this.shotsHit );
            updateStmt.setDouble( 6, this.distanceMoved );
            updateStmt.setInt( 7, this.shotsEvaded );
            updateStmt.setDouble( 8, this.damageReduced );
            updateStmt.setInt( 9, this.criticalsHit );
            updateStmt.setInt( 10, this.safelyShot );
            updateStmt.setInt( 11, this.battles );
            updateStmt.setInt( 12, this.battlesWon );
            updateStmt.setInt( 13, this.type );
            updateStmt.setInt( 14, this.id );

            MySQLDB.delaySQL( updateStmt );
        }
        catch( SQLException e )
        {
            throw new GameException( 3, "Error while updating statistic " +
                                        "block." + e.getMessage() );
        }
    }

    /**
     * Gets the ID of this statistic block
     *
     * @return ID of the target of this statistic block
     */
    public int getID()
    {
        return this.id;
    }

    /**
     * Gets the type of this statistic block
     *
     * @return Type of the target of this statistic block
     */
    public int getType()
    {
        return this.type;
    }

    /**
     * Gets the integers in this statistic block.
     *
     * Does not include the ID or the type.  Includes the following fields:
     * Shots Fired, Reloads, Shots Hit, Shots Evaded, Criticals Hit,
     * Times Safely Shot, Battles, Battles Won
     *
     * @return Array of integers in this statistic block
     */
    public int[] getInts()
    {
        return new int[]{
            this.shotsFired, this.reloads, this.shotsHit, this.shotsEvaded,
            this.criticalsHit, this.safelyShot, this.battles, this.battlesWon
        };
    }

    /**
     * Gets the doubles in this statistic block.
     *
     * Includes the following fields: Damage Dealt, Damage Taken, Distance
     * Moved, Damage Reduced
     *
     * @return Array of doubles in this statistic block
     */
    public double[] getDoubles()
    {
        return new double[]{
            this.damageDealt, this.damageTaken, this.distanceMoved,
            this.damageReduced
        };
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
    
    /**
     * Returns a dummy object for pulling table profile information
     *
     * @return Dummy object
     */
    public static TableProfile getProfile()
    {
        return new TrainerBattleStats( -1, -1 );
    }

    /**
     * Returns the name of the SQL table
     *
     * @return TRAINERBATTLESTATS
     */
    @Override
    public String tableName()
    {
        return "TRAINERBATTLESTATS";
    }

    /**
     * Returns the string used to create the SQL table
     *
     * @return Creation string
     */
    @Override
    public String createString()
    {
        return
            "CREATE TABLE `TRAINERBATTLESTATS` (" +
            "`type` int(10) unsigned NOT NULL," +
            "`id` int(10) unsigned NOT NULL," +
            "`shotsFired` int(10) unsigned NOT NULL," +
            "`damageDealt` double unsigned NOT NULL," +
            "`damageTaken` double unsigned NOT NULL," +
            "`reloads` int(10) unsigned NOT NULL," +
            "`shotsHit` int(10) unsigned NOT NULL," +
            "`distanceMoved` double unsigned NOT NULL," +
            "`shotsEvaded` int(10) unsigned NOT NULL," +
            "`damageReduced` double unsigned NOT NULL," +
            "`criticalsHit` int(10) unsigned NOT NULL," +
            "`safelyShot` int(10) unsigned NOT NULL," +
            "`battles` int(10) unsigned NOT NULL," +
            "`battlesWon` int(10) unsigned NOT NULL," +
            "PRIMARY KEY (`type`,`id`)" +
            ") ENGINE = MyISAM DEFAULT CHARSET=latin1";
    }

    /**
     * Returns an array containing the SQL table headers
     *
     * @return [type,id,shotsFired,damageDealt,damageTaken,reloads,shotsHit,
     *         distanceMoved,shotsEvaded,damageReduced,criticalsHit,safelyShot,
     *         battles,battlesWon]
     */
    @Override
    public String[] tableHeaders()
    {
        return new String[]{ "type", "id",
            "shotsFired", "damageDealt", "damageTaken", "reloads",
            "shotsHit", "distanceMoved", "shotsEvaded",
            "damageReduced", "criticalsHit", "safelyShot",
            "battles", "battlesWon" };
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
        int n_type = rs.getInt( 1 );
        int n_id = rs.getInt( 2 );
        int n_shotsFired = rs.getInt( 3 );
        int n_reloads = rs.getInt( 6 );
        int n_shotsHit = rs.getInt( 7 );
        int n_shotsEvaded = rs.getInt( 9 );
        int n_criticalsHit = rs.getInt( 11 );
        int n_safelyShot = rs.getInt( 12 );
        int n_battles = rs.getInt( 13 );
        int n_battlesWon = rs.getInt( 14 );

        double n_damageDealt = rs.getDouble( 4 );
        double n_damageTaken = rs.getDouble( 5 );
        double n_distanceMoved = rs.getDouble( 8 );
        double n_damageReduced = rs.getDouble( 10 );

        int ints[] = new int[]{ n_shotsFired, n_reloads, n_shotsHit,
            n_shotsEvaded, n_criticalsHit, n_safelyShot, n_battles,
            n_battlesWon
        };
        double doubles[] = new double[]{ n_damageDealt, n_damageTaken,
            n_distanceMoved, n_damageReduced
        };

        TrainerBattleStats stats =
                new TrainerBattleStats( n_type, n_id, ints, doubles );

        EXPManager.getInstance().addBattleStats( stats );
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

        updateStmt = conn.prepareStatement( "UPDATE TRAINERBATTLESTATS SET " +
            "shotsFired = ?, damageDealt = ?, damageTaken = ?, reloads = ?, " +
            "shotsHit = ?, distanceMoved = ?, shotsEvaded = ?, " +
            "damageReduced = ?, criticalsHit = ?, safelyShot = ?, " +
            "battles = ?, battlesWon = ? WHERE type = ? AND id = ?" );
        deleteStmt = conn.prepareStatement( "DELETE FROM TRAINERBATTLESTATS " +
                                            "WHERE type = ? AND id = ?" );
        insertStmt = conn.prepareStatement( "INSERT INTO TRAINERBATTLESTATS " +
            "VALUES( ?, ?, 0, 0.0, 0.0, 0, 0, 0.0, 0, 0.0, 0, 0, 0, 0 )" );
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

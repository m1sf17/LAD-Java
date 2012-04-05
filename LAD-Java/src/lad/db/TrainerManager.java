package lad.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import lad.java.Trainer;

/**
 * Manages all of the trainers (and consequently their minions)
 *
 * @author msflowers
 */
public class TrainerManager
{
    /**
     * String for creating the trainer table
     */
    private String trainerCreationStr =
            "CREATE TABLE `TRAINERS` (" +
            "`ID` int(10) unsigned NOT NULL AUTO_INCREMENT," +
            "`owner` int(10) unsigned NOT NULL," +
            "`exp` int(10) unsigned NOT NULL," +
            "`level` int(10) unsigned NOT NULL," +
            "PRIMARY KEY (`ID`)" +
            ") ENGINE = MyISAM DEFAULT CHARSET=latin1";

    /**
     * String for creating the minion table
     */
    private String minionCreationStr =
            "CREATE TABLE `MINIONS` (" +
            "`ID` int(10) unsigned NOT NULL AUTO_INCREMENT," +
            "`owner` int(10) unsigned NOT NULL," +
            "`exp` int(10) unsigned NOT NULL," +
            "`level` int(10) unsigned NOT NULL," +
            "PRIMARY KEY (`ID`)" +
            ") ENGINE = MyISAM DEFAULT CHARSET=latin1";

    /**
     * The internal list of trainers.
     */
    private LinkedList< Trainer > trainers = new LinkedList<>();
    /**
     * Private ctor
     */
    private TrainerManager()
    {
    }

    public void initialize()
    {
        // Verifies the tables
        validateTablesExist();
        
        // Grab actual data and populate list
        Connection conn = MySQLDB.getConn();
        try
        {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery( "SELECT * FROM TRAINERS" );
            while( result.next() )
            {
                int ID = result.getInt( 1 );
                int owner = result.getInt( 2 );
                int exp = result.getInt( 3 );
                int level = result.getInt( 4 );

                Trainer trainer = new Trainer( ID, owner, exp, level );
                trainers.add( trainer );
                trainer.load();
            }
        }
        catch( SQLException e )
        {
            System.err.println( "Error while initializing trainers:" +
                                e.toString() );
        }
    }

    /**
     * Validates the MySQL Tables exist, otherwise creates them.
     */
    private void validateTablesExist()
    {
        String[] bothTables = { "ID", "owner", "exp", "level" };
        MySQLDB db = MySQLDB.getInstance();
        try
        {
            db.validateTable( "TRAINERS", trainerCreationStr );
        }
        catch( SQLException e )
        {
            System.err.println( "Error with trainer table." +
                                e.toString() );
            System.exit( -1 );
        }
        try
        {
            db.validateStructure( bothTables, "TRAINERS" );
        }
        catch( SQLException e )
        {
            System.err.println( "Error with trainer headers." +
                                e.toString() );
            System.exit( -1 );
        }
        try
        {
            db.validateTable( "MINIONS", minionCreationStr );
        }
        catch( SQLException e )
        {
            System.err.println( "Error with minion table." +
                                e.toString() );
            System.exit( -1 );
        }
        try
        {
            db.validateStructure( bothTables, "MINIONS" );
        }
        catch( SQLException e )
        {
            System.err.println( "Error with minion headers." +
                                e.toString() );
            System.exit( -1 );
        }


    }

    /**
     * Returns the singleton
     *
     * @return Singleton
     */
    public static TrainerManager getInstance()
    {
        return TrainerManagerHolder.INSTANCE;
    }

    private static class TrainerManagerHolder
    {
        private static final TrainerManager INSTANCE = new TrainerManager();
    }

}

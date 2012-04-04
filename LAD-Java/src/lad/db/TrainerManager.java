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
        String[] loadedTables = MySQLDB.getTableList();
        boolean foundMinion = false;
        boolean foundTrainer = false;
        Connection conn = MySQLDB.getConn();

        // Check the list of tables to see if ours are in there
        for( int i = 0; i < loadedTables.length; i++ )
        {
            String tbl = loadedTables[ i ];
            if( tbl.compareToIgnoreCase( "MINIONS" ) == 0 )
            {
                foundMinion = true;
            }
            else if( tbl.compareToIgnoreCase( "TRAINERS" ) == 0 )
            {
                foundTrainer = true;
            }
        }

        if( !foundMinion )
        {
            try
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate( minionCreationStr );
            }
            catch( SQLException e )
            {
                System.err.println( "Error while creating minion table: " +
                                    e.toString() );
            }
        }
        if( !foundTrainer )
        {
            try
            {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate( trainerCreationStr );
            }
            catch( SQLException e )
            {
                System.err.println( "Error while creating trainer table: " +
                                    e.toString() );
            }
        }

        // If one wasn't found, update the list of tables
        if( !foundMinion || !foundTrainer )
        {
            MySQLDB.getInstance().populateTableList();
        }

        // If one was found, check both integrity
        if( foundMinion || foundTrainer )
        {
            validateStructures();
        }
    }

    /**
     * Validates that all of the MySQL Tables are valid for information.
     */
    private void validateStructures()
    {
        String[] trainerFields = { "ID", "owner", "exp", "level" };
        String[] minionFields = { "ID", "owner", "exp", "level" };

        // Grab actual data and populate list
        Connection conn = MySQLDB.getConn();
        try
        {
            boolean trainerValid = true;
            boolean minionValid = true;
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery( "SHOW COLUMNS FROM " +
                                                  "TRAINERS" );
            int i = 0;
            while( result.next() )
            {
                if( trainerFields[ i ].compareToIgnoreCase( result.getString( 1 ) ) != 0 )
                {
                    throw new SQLException( "Trainer table error." );
                }
                i++;
            }
            if( i != trainerFields.length )
            {
                throw new SQLException( "Trainer table column miscount." );
            }

            stmt = conn.createStatement();
            result = stmt.executeQuery( "SHOW COLUMNS FROM MINIONS" );
            i = 0;
            while( result.next() )
            {
                if( minionFields[ i ].compareToIgnoreCase( result.getString( 1 ) ) != 0 )
                {
                    throw new SQLException( "Minion table error." );
                }
                i++;
            }
            if( i != minionFields.length )
            {
                throw new SQLException( "Minion table column miscount." );
            }

        }
        catch( SQLException e )
        {
            e.printStackTrace();
            System.err.println( "Error while validating trainers and minions:" +
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

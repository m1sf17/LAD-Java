package lad.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import lad.java.LADJava;

/**
 * Manages the connection to the MySQL DB.
 *
 * MySQL calls need a Connection to be able to process.  Simply call the static
 * @see getConn to get a valid connection.
 *
 * @author msflowers
 */
public class MySQLDB
{
    /**
     * Holds the actual connection to the database.
     */
    private Connection conn = null;

    /**
     * Holds the runner that performs a delayed (not really) execution of MySQL
     * statements
     */
    private DelayedRunner runner = null;

    /**
     * A list of all the tables currently loaded into the database.
     */
    private LinkedList< String > loadedTables = new LinkedList<>();

    /**
     * Private ctor.
     */
    private MySQLDB()
    {
    }

    /**
     * Performs actual connection to the MySQL DB.
     *
     * Connects to the DB using specified username/password. Called if the
     * connection is not valid in @see getConn
     * 
     * @throws SQLException
     */
    public void initialize() throws SQLException
    {
        Properties connectionProps = new Properties();
        connectionProps.put( "user", "admin_lad" );
        connectionProps.put( "password", "password" );
        conn = DriverManager.getConnection( "jdbc:mysql://localhost/",
                                            connectionProps );

        try
        {
            Statement stmt = conn.createStatement();
            stmt.executeQuery( "USE admin_lad" );
        }
        catch( SQLException e )
        {
            System.err.println( "Error while selecting database." +
                                e.toString() );
            System.exit( -1 );
        }
        
        // Start the Delayed runner thread
        runner = new DelayedRunner( conn.createStatement() );
        new Thread( runner ).start();

        // Populate the table list
        populateTableList();
    }

    /**
     * Static function to get the MySQL connection.
     *
     * @return A valid Connection to the MySQL Database
     */
    public static Connection getConn()
    {
        return getInstance().getConnection();
    }

    /**
     * Gets the connection to the MySQL DB.
     *
     * @see intialize should be called first.  The internal handler for
     * initializing within this function is to exit.
     * 
     * @return A valid connection
     */
    private Connection getConnection()
    {
        if( conn == null )
        {
            try
            {
                initialize();
            }
            catch( SQLException e )
            {
                System.err.println( "Failed to connect to MySQL DB." );
                System.err.println( e.toString() );
                System.exit( -1 );
            }
        }
        return conn;
    }

    /**
     * Initializes the tables list.
     */
    public void populateTableList()
    {
        try
        {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery( "SHOW TABLES" );
            while( result.next() )
            {
                loadedTables.add( result.getString( 1 ) );
            }
        }
        catch( SQLException e )
        {
            System.err.println( "Error while retrieving table list." +
                                e.toString() );
            System.exit( -1 );
        }
    }

    /**
     * Returns the list of tables that is currently loaded in the MySQL DB.
     *
     * @return loadedTables
     */
    public static String[] getTableList()
    {
        return getInstance().loadedTables.toArray( new String[0] );
    }

    /**
     * Adds a statement to be delay-executed.
     *
     * The statement is added to a queue that is continuously emptied in a
     * separate thread.  That threads sole responsibility is to make sure the
     * queue remains empty so it will be relatively small.
     *
     * @param stmt The string to be delay-executed
     */
    public static void delaySQL( String stmt )
    {
        getInstance().runner.addStmt( stmt );
    }

    /**
     * Gets the instance
     *
     * @return Singleton
     */
    public static MySQLDB getInstance()
    {
        return MySQLDBHolder.INSTANCE;
    }

    private static class MySQLDBHolder
    {
        private static final MySQLDB INSTANCE = new MySQLDB();
    }

    private class DelayedRunner implements Runnable
    {
        private final Object notifier = new Object();
        private final LinkedList< String > stmts;
        private final Statement stmt;

        DelayedRunner( Statement n_stmt )
        {
            stmts = new LinkedList<>();
            stmt = n_stmt;
        }

        void addStmt( String str )
        {
            synchronized( stmts )
            {
                stmts.add( str );
            }
        }

        @Override
        public void run()
        {
            System.out.println( "Started MySQL Delayed Runner Thread" );
            try
            {
                while( LADJava.running )
                {
                    synchronized( notifier )
                    {
                        notifier.wait();
                        emptyQueue();
                    }
                }
            }
            catch( InterruptedException e )
            {
            }
            emptyQueue();
            System.out.println( "Ended MySQL Delayed Runner Thread" );
        }

        private void emptyQueue()
        {
            try
            {
                synchronized( stmts )
                {
                    while( stmts.size() > 0 )
                    {
                        String stmtString = stmts.poll();
                        stmt.execute( stmtString );
                    }
                }
            }
            catch( SQLException e )
            {
                System.err.println( "Delayed execution of SQL failed." );
                System.err.println( e.toString() );
            }
        }
    }
}

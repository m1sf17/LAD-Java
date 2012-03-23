package lad.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

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
}

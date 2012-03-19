package lad.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author msflowers
 */
public class MySQLDB
{
    private Connection conn = null;

    private MySQLDB()
    {
    }

    private void initialize() throws SQLException
    {
        Properties connectionProps = new Properties();
        connectionProps.put( "user", "admin_lad" );
        connectionProps.put( "password", "password" );
        conn = DriverManager.getConnection( "jdbc:mysql://localhost/",
                                            connectionProps );
    }

    public static Connection getConn() throws SQLException
    {
        return getInstance().getConnection();
    }

    public Connection getConnection() throws SQLException
    {
        if( conn == null )
        {
            initialize();
        }
        return conn;
    }

    public static MySQLDB getInstance()
    {
        return MySQLDBHolder.INSTANCE;
    }

    private static class MySQLDBHolder
    {

        private static final MySQLDB INSTANCE = new MySQLDB();

    }

}

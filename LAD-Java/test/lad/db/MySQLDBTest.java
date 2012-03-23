package lad.db;

import java.sql.Connection;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author msflowers
 */
public class MySQLDBTest
{
    MySQLDB db1 = null;
    MySQLDB db2 = null;
    Connection conn = null;

    public MySQLDBTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test all methods, of class MySQLDB.
     *
     * First, tests getInstance to make sure the object is created properly
     * Then, tests initialize to ensure initialization passes
     * Finally, tests getConn to ensure the connection is valid
     */
    @Test
    public void testAll()
    {
        System.out.println( "getInstance" );
        MySQLDB result = MySQLDB.getInstance();
        if( result == null )
        {
            fail( "Failed to create object" );
            return;
        }
        // Check it twice to make sure a created object is returned as well
        MySQLDB result2 = MySQLDB.getInstance();
        if( result != result2 )
        {
            fail( "Object created isn't same as getInstance value" );
            return;
        }

        System.out.println( "initialize" );
        // Instance is still good, now check
        // If an error occurs, an exception will be thrown
        try
        {
            result.initialize();
        }
        catch( java.sql.SQLException e )
        {
            fail( "Connection error: " + e.toString() );
            return;
        }

        // Initialization passed, try and get the connection
        System.out.println( "getConn" );
        conn  = MySQLDB.getConn();
        assertNotNull( "Failed to get connection.", conn );
        
        // If it makes it this far it passed.
    }

}

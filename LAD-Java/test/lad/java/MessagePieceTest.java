package lad.java;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests MessagePiece class to ensure its functionality.
 *
 * @author msflowers
 */
public class MessagePieceTest
{

    public MessagePieceTest()
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
     * Test of variable methods, of class MessagePiece.
     */
    @Test
    public void testVariable()
    {
        System.out.println( "setVariable" );
        String n_var = "a";
        MessagePiece instance = new MessagePiece();
        instance.setVariable( n_var );
        String ret = instance.getVariable();
        if( ret.compareTo( n_var ) != 0 )
        {
            fail( "Failed to set variable. Set: " + n_var + ". Get: " + ret );
        }
    }

    /**
     * Test of value methods, of class MessagePiece.
     */
    @Test
    public void testValue()
    {
        System.out.println( "setValue" );
        String n_val = "a";
        MessagePiece instance = new MessagePiece();
        instance.setValue( n_val );
        String ret = instance.getValue();
        if( ret.compareTo( n_val ) != 0 )
        {
            fail( "Failed to set value. Set: " + n_val + ". Get: " + ret );
        }
    }

    /**
     * Test of ctor, of class MessagePiece.
     */
    @Test
    public void testGetVariable()
    {
        String var = "a", val = "b";
        System.out.println( "getVariable" );
        MessagePiece instance = new MessagePiece( var, val );
        String retvar = instance.getVariable();
        String retval = instance.getValue();
        if( retvar.compareTo( var ) != 0 )
        {
            fail( "Failed to set variable in ctor. Set: " + var + ". Get: " +
                  retvar );
        }
        if( retval.compareTo( val ) != 0 )
        {
            fail( "Failed to set value in ctor. Set: " + val + ". Get: " +
                  retval );
        }
    }

}

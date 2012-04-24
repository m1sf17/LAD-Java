package lad.java;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests MessagePiece class to ensure its functionality.
 *
 * @author msflowers
 */
public class MessagePieceTest
{
    /**
     * Sets up the class before all the tests
     */
    @BeforeClass
    public static void setUpClass()
    {
        System.out.println( "===Message Piece===" );
    }

    /**
     * Test of variable methods, of class MessagePiece.
     */
    @Test
    public void testVariable()
    {
        System.out.println( "get/setVariable" );
        String n_var = "a";
        MessagePiece instance = new MessagePiece();
        instance.setVariable( n_var );
        String ret = instance.getVariable();
        assertTrue( "Failed to set variable. Set: " + n_var + ". Get: " + ret,
                    ret.compareTo( n_var ) == 0 );
        if( ret.compareTo( n_var ) != 0 )
        {
            fail(  );
        }
    }

    /**
     * Test of value methods, of class MessagePiece.
     */
    @Test
    public void testValue()
    {
        System.out.println( "get/setValue" );
        String n_val = "a";
        MessagePiece instance = new MessagePiece();
        instance.setValue( n_val );
        String ret = instance.getValue();
        assertTrue( "Failed to set value. Set: " + n_val + ". Get: " + ret,
                    ret.compareTo( n_val ) == 0 );
    }

    /**
     * Test of ctor, of class MessagePiece.
     */
    @Test
    public void testCtor()
    {
        String var = "a", val = "b";
        System.out.println( "ctor" );
        MessagePiece instance = new MessagePiece( var, val );
        String retvar = instance.getVariable();
        String retval = instance.getValue();
        assertTrue( "Failed to set variable in ctor. Set: " + var + ". Get: " +
                    retvar, retvar.compareTo( var ) == 0 );
        assertTrue( "Failed to set value in ctor. Set: " + val + ". Get: " +
                    retval, retval.compareTo( val ) == 0 );
    }

}

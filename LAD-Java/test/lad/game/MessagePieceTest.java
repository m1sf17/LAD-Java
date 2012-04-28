package lad.game;

import lad.game.MessagePiece;
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
        System.out.println( "ctor" );
        String var = "a", val = "b";
        MessagePiece instance = new MessagePiece( var, val );
        String retvar = instance.getVariable();
        String retval = instance.getValue();
        assertTrue( "Failed to set variable in ctor. Set: " + var + ". Get: " +
                    retvar, retvar.compareTo( var ) == 0 );
        assertTrue( "Failed to set value in ctor. Set: " + val + ". Get: " +
                    retval, retval.compareTo( val ) == 0 );

        System.out.println( "ctor2" );
    }

    /**
     * Test all three ways a message piece can equal another
     */
    @Test
    public void testEquals()
    {
        String var = "a", val = "b", var2 = "c";
        MessagePiece instance1 = new MessagePiece( var, val ),
                     instance2 = new MessagePiece( var ),
                     instance3 = new MessagePiece( var, val ),
                     instance4 = new MessagePiece( var2 ),
                     instance5 = new MessagePiece( var2, val ),
                     instance6 = new MessagePiece( instance1 );

        String preFail = "Failed to compare message pieces(";
        System.out.println( "equals-self/copy" );
        assertTrue( preFail + "Piece/Self)", instance1.equals( instance1 ) );
        assertTrue( preFail + "Piece/Copy)", instance1.equals( instance6 ) );

        System.out.println( "equals-full piece" );
        assertTrue( preFail + "Piece/Piece)", instance1.equals( instance3 ) );
        assertFalse( preFail + "Piece/Piece)", instance1.equals( instance5 ) );

        System.out.println( "equals-wild card" );
        assertTrue( preFail + "Piece/Wild)", instance1.equals( instance2 ) );
        assertFalse( preFail + "Piece/Wild)", instance1.equals( instance4 ) );

        System.out.println( "equals-string" );
        assertTrue( preFail + "Piece/String)", instance1.equals( var ) );
        assertFalse( preFail + "Piece/String)", instance1.equals( var2 ) );
    }
}

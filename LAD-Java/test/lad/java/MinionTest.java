package lad.java;

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
public class MinionTest
{

    public MinionTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        System.out.println( "===Minion===" );
        Minion.prepareStatements();
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
     * Test of level methods, of class Minion.
     */
    @Test
    public void testLevel()
    {
        System.out.println( "Level" );
        int l = 4;
        Minion instance = new Minion();
        instance.setLevel( l );
        int c = instance.getLevel();
        assertEquals( "Level fails to set", l, c );
    }

    /**
     * Test of exp methods, of class Minion.
     */
    @Test
    public void testExp()
    {
        System.out.println( "Exp" );
        int e = 3;
        Minion instance = new Minion();
        instance.setExp( e );
        int c = instance.getExp();
        assertEquals( "Exp fails to set", e, c );
    }

    /**
     * Test of owner methods, of class Minion.
     */
    @Test
    public void testOwner()
    {
        System.out.println( "Owner" );
        int o = 2;
        Minion instance = new Minion();
        instance.setOwner( o );
        int c = instance.getOwner();
        assertEquals( "Exp fails to set", o, c );
    }

    /**
     * Test of adjust method, of class Minion
     */
    @Test
    public void testAdjust()
    {
        System.out.println( "Adjust" );
        int initialExp = 5;
        int initialLevel = 2;
        int addExp = 13;
        int expectExp = 6;
        int expectLevel = 3;
        Minion instance = new Minion( 0, initialExp, initialLevel, 0 );
        instance.adjustExp( addExp );

        int retExp = instance.getExp();
        int retLevel = instance.getLevel();

        assertEquals( "Adjust fails exp growth.", retExp, expectExp );
        assertEquals( "Adjust fails level growth.", retLevel, expectLevel );
    }
}

package lad.java;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all of the tests in the java package.
 *
 * @author msflowers
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
    lad.java.MinionTest.class,
    lad.java.MessagePieceTest.class,
    lad.java.TrainerBattleTest.class
})
public class TestSuite
{

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

}

package lad.game;

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
    lad.game.MessagePieceTest.class,
    lad.game.TrainerBattleTest.class
})
public class TestSuite
{
    /**
     * Empty ctor
     */
    public TestSuite()
    {
    }
}

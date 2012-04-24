package lad.db;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite for all of the tests in the DB package
 *
 * @author msflowers
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
    lad.db.MySQLDBTest.class
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

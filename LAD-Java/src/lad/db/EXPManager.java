package lad.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.data.GameException;
import lad.data.ModifierTarget;
import lad.data.UserExp;
import lad.data.UserExpTarget;

/**
 * Manages all of the User EXP
 *
 * @author msflowers
 */
public class EXPManager extends DBManager
{
    /**
     * Internal list of exp
     */
    private LinkedList< UserExp > exps = new LinkedList<>();

    /**
     * List of values for the amount of experience required to grow a level
     * normally.
     */
    private final static int expToGrow[] = {
        10,  12,  14,  16,  19,  22,  26,  31,  37,  44,
        52,  62,  74,  88, 105, 126, 151, 181, 217, 260,
         312,  374,  448,  537,  644,  772,  926, 1110, 1330, 1590,
        1900, 2280, 2730, 3270, 3920, 4700, 5640, 6760, 8110, 9730,
        11600, 13900, 16600, 19900, 23800, 28500, 34200, 41000, 49200, 59000,
         70800,  84900, 101000, 121000, 145000, 174000, 208000, 249000,
        298000, 357000, 428000, 513000, 615000, 738000, 885000, 1060000
    };

    /**
     * Private ctor
     */
    private EXPManager()
    {
    }

    /**
     * Returns the table profile to load.
     *
     * The table is different from most in that it does not have an ID but
     * uses a 3-part key.  The three parts consist of the owner (user), target
     * (weapon) and the type (stat).
     *
     * @return A list with only the EXP table to load
     */
    @Override
    public TableProfile[] profiles()
    {
        return new TableProfile[]{
            new TableProfile(){
                @Override
                public String tableName()
                {
                    return "USEREXP";
                }
                @Override
                public String createString()
                {
                    return
                        "CREATE TABLE `USEREXP` (" +
                        "`owner` int(10) unsigned NOT NULL," +
                        "`target` int(10) unsigned NOT NULL," +
                        "`type` int(10) unsigned NOT NULL," +
                        "`level` int(10) unsigned NOT NULL," +
                        "`exp` int(10) unsigned NOT NULL," +
                        "`totalexp` int(10) unsigned NOT NULL," +
                        "PRIMARY KEY (`owner`,`target`,`type`)" +
                        ") ENGINE = MyISAM DEFAULT CHARSET=latin1";
                }
                @Override
                public String[] tableHeaders()
                {
                    return new String[]{ "owner", "target", "type",
                                         "level", "exp", "totalexp" };
                }
                @Override
                public void loadRow( ResultSet rs ) throws SQLException
                {
                    int owner = rs.getInt( 1 );
                    int target = rs.getInt( 2 );
                    int type = rs.getInt( 3 );
                    int level = rs.getInt( 4 );
                    int exp = rs.getInt( 5 );
                    int totalexp = rs.getInt( 6 );

                    UserExp userexp = new UserExp( owner, target, type,
                                                   level, exp, totalexp );
                    exps.add( userexp );
                }
                @Override
                public void postinit() throws SQLException
                {
                    UserExp.prepareStatements();
                }
                @Override
                public boolean loadData()
                {
                    return true;
                }
            }
        };
    }

    /**
     * Gets all exp for the specified owner.
     *
     * If no modifiers are found the list returned will simply be empty.
     *
     * @param owner Owner to get exp for
     * @return List of found exp
     */
    public static List< UserExp > getExpByUserID( int owner )
    {
        LinkedList< UserExp > ret = new LinkedList<>();
        ListIterator< UserExp > iter = getInstance().exps.listIterator();

        while( iter.hasNext() )
        {
            UserExp current = iter.next();

            if( current.getOwner() == owner )
            {
                ret.add( current );
            }
        }

        return ret;
    }

    /**
     * Gets a specific exp.
     *
     * @param user   User to get the EXP for
     * @param type   Type of exp to get
     * @param target Target of the exp
     * @return EXP that was found, or null if it does not exist
     */
    public static UserExp getExp( int user, UserExpTarget target,
                                  ModifierTarget type )
    {
        ListIterator< UserExp > iter = getInstance().exps.listIterator();
        while( iter.hasNext() )
        {
            UserExp current = iter.next();

            if( current.getOwner() == user && current.getTarget() == target &&
                current.getType() == type )
            {
                return current;
            }
        }

        return null;
    }

    /**
     * Grants a user a certain type of EXP.
     *
     * @param user User to get the EXP
     * @param type Type of EXP the user is getting
     * @param target Target of the EXP the user is getting
     * @param amount Amount of EXP the user is getting
     */
    public static void grantUserEXP( int user, UserExpTarget target,
                                     ModifierTarget type, int amount )
    {
        // Check if exp already exists and only needs to be added to
        UserExp userExp = getExp( user, target, type );

        // If it wasn't found then make a new one
        if( userExp == null )
        {
            userExp = UserExp.create( user, target.getValue(),
                                      type.getValue() );
            getInstance().exps.add( userExp );
        }

        // Increment it!
        userExp.setExp( userExp.getExp() + amount );
    }

    /**
     * Advances a user's EXP a certain number of levels.
     *
     * @param user   User to level the exp
     * @param target Target of the exp to level
     * @param type   Type of exp to level
     * @param amount Number of levels to advance the exp
     * @throws GameException Thrown if the EXP was not found or not enough EXP
     */
    public static void advanceUserEXP( int user, UserExpTarget target,
                                       ModifierTarget type, int amount )
    {
        // Find the exp
        UserExp userExp = getExp( user, target, type );

        // Abort if it wasn't found
        if( userExp == null )
        {
            throw new GameException( 1, "Can not advance exp: not found: " +
                                     user + ", " + target.toString() + ", " +
                                     type.toString() );
        }

        final int currentLevel = userExp.getLevel();
        final int targetLevel = currentLevel + amount;
        final int currentExp = userExp.getExp();
        final int expRequired = expRequiredFromLevelToLevel( currentLevel,
                                                             targetLevel );

        // If the user can do it
        if( currentExp >= expRequired )
        {
            userExp.setValues( targetLevel, currentExp - expRequired );
        }
        else
        {
            throw new GameException( 1, "Can not advance exp: not enough: " +
                                     user + ", " + target.toString() + ", " +
                                     type.toString() + ", Req'd: " +
                                     expRequired + ", Has: " + currentExp );
        }
    }

    /**
     * Calculates how much exp is needed from a given level to the next.
     *
     * @param level Level to calculate from
     * @return EXP required to advance a level
     */
    public static int expRequiredAtLevel( int level )
    {
        if( level < expToGrow.length )
        {
            return expToGrow[ level ];
        }
        return expToGrow[ expToGrow.length - 1 ];
    }

    /**
     * Calculates how much exp is needed from a given level to another given
     * level via bonus route.
     *
     * @param from Lower value to calculate from
     * @param to   Higher value to calculate to
     * @return Total EXP required to advance the levels
     * @throws GameException Thrown if from >= to
     */
    public static int expRequiredFromLevelToLevel( int from, int to )
    {
        // From must be less than to
        if( from >= to )
        {
            throw new GameException( 3, "From exp >= to exp:" + from + ", " +
                                     to );
        }

        // Set up initial values
        int totalExp = 0, step = 0;
        int level = from, evalLevel = from;
        while( level != to )
        {
            totalExp += expRequiredAtLevel( evalLevel );
            if( level > from )
            {
                step++;
            }

            evalLevel += step;
            level++;
        }

        return totalExp;
    }

    /**
     * Returns the singleton
     *
     * @return singleton
     */
    public static EXPManager getInstance()
    {
        return EXPManagerHolder.INSTANCE;
    }

    private static class EXPManagerHolder
    {
        private static final EXPManager INSTANCE = new EXPManager();
    }
}

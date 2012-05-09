package lad.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.data.GameException;
import lad.data.ModifierTarget;
import lad.data.TrainerBattleStats;
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
    private List< UserExp > exps = new LinkedList<>();

    /**
     * Internal list of trainer battle statistics
     */
    private List< TrainerBattleStats > trainerBattleStats = new LinkedList<>();

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
            UserExp.getProfile(),
            TrainerBattleStats.getProfile()
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
        List< UserExp > ret = new LinkedList<>();
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
    private static UserExp getExp( int user, UserExpTarget target,
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
     * Adds a user EXP block to the internal list.
     *
     * @param exp EXP block to add
     */
    public void addEXP( UserExp exp )
    {
        exps.add( exp );
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
     * Gets a specific statistic block.
     *
     * @param type Type of the target of the statistic block
     * @param id   ID of the target of the statistic block
     * @return Statistics that match the type and ID
     * @throws GameException Thrown if the statistic was not found
     */
    public static TrainerBattleStats getBattleStats( int type, int id )
    {
        ListIterator< TrainerBattleStats > iter =
                getInstance().trainerBattleStats.listIterator();
        while( iter.hasNext() )
        {
            TrainerBattleStats current = iter.next();

            if( current.getType() == type && current.getID() == id )
            {
                return current;
            }
        }

        throw new GameException( 1, "Trainer Battle Statistics not found." );
    }

    /**
     * Grants some values to a given statistic.
     *
     * The integer array must contain 8 values and the integer array must
     * contain 4 values.
     *
     * @param type Type of the target of the statistic block
     * @param id   ID of the target of the statistic block
     * @param ints Integer values to grant the statistic block
     * @param doubles Double values to grant the statistic block
     */
    public static void addBattleStats( int type, int id, int ints[],
                                       double doubles[] )
    {
        // Try to get the battle stats if they already exist
        TrainerBattleStats stats;
        try
        {
            stats = getBattleStats( type, id );
        }
        catch( GameException ge )
        {
            // They don't exist so create them
            stats = TrainerBattleStats.create( type, id );
            getInstance().addBattleStats( stats );
        }

        // Add in the values
        stats.addValues( ints, doubles );
    }

    /**
     * Adds a battle statistic to the internal list.
     *
     * @param stats Battle statistics to add
     */
    public void addBattleStats( TrainerBattleStats stats )
    {
        trainerBattleStats.add( stats );
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

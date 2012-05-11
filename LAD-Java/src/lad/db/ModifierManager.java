package lad.db;

import static java.util.Collections.shuffle;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.data.GameException;
import lad.data.Modifier;
import lad.data.ModifierTarget;

/**
 * Manages all of the modifiers
 *
 * @author msflowers
 */
public class ModifierManager extends DBManager
{
    /**
     * Internal list of modifiers
     */
    private List< Modifier > modifiers = new LinkedList<>();

    /**
     * Private ctor
     */
    private ModifierManager()
    {
    }

    /**
     * Returns the table profile to load
     *
     * @return A list with only the modifier table to load
     */
    @Override
    public TableProfile[] profiles()
    {
        return new TableProfile[]{
            Modifier.getProfile()
        };
    }

    /**
     * Gets all modifiers for the specified owner.
     *
     * If no modifiers are found the list returned will simply be empty.
     *
     * @param userid User ID to get modifiers for
     * @return List of found modifiers
     */
    public List< Modifier > getByUserID( int userid )
    {
        List< Modifier > ret = new LinkedList<>();
        ListIterator< Modifier > iter = modifiers.listIterator();

        while( iter.hasNext() )
        {
            Modifier current = iter.next();

            if( current.getOwner() == userid )
            {
                ret.add( current );
            }
        }

        return ret;
    }

    /**
     * Gets all not-equipped modifiers for the specified owner.
     *
     * Only returns modifiers that do not have a trainer set.  If no modifiers
     * are found the list returned will simply be empty.
     *
     * @param userid User ID to get modifiers for
     * @return List of found modifiers
     */
    public List< Modifier > getAvailableByUserID( int userid )
    {
        List< Modifier > ret = new LinkedList<>();
        ListIterator< Modifier > iter = modifiers.listIterator();

        while( iter.hasNext() )
        {
            Modifier current = iter.next();

            if( current.getOwner() == userid && current.getEquipped() == null )
            {
                ret.add( current );
            }
        }

        shuffle( ret );
        return ret;
    }

    /**
     * Gets a set of available modifiers for the specified owner.
     *
     * Only returns modifiers that do not have a trainer set.  Guaranteed to
     * only return one modifier of each type.  If no modifiers are found the
     * list returned will simply be empty.
     *
     * @param userid User ID to get modifiers for
     * @return List of found modifiers
     */
    public List< Modifier > getAvailableSetByUserID( int userid )
    {
        return getAvailableSetByUserID( userid, ModifierTarget.getLength() );
    }

    /**
     * Gets a set of available modifiers for the specified owner.
     *
     * Only returns modifiers that do not have a trainer set.  Guaranteed to
     * only return one modifier of each type.  Will not return more than the
     * specified amount of modifiers.  If no modifiers are found the list
     * returned will simply be empty.
     *
     * @param userid User ID to get modifiers for
     * @param max    Maximum number of modifiers to get
     * @return List of found modifiers
     */
    public List< Modifier > getAvailableSetByUserID( int userid, int max )
    {
        List< Modifier > ret = new LinkedList<>();
        List< ModifierTarget > found = new LinkedList<>();
        ListIterator< Modifier > iter = modifiers.listIterator();

        while( iter.hasNext() )
        {
            Modifier current = iter.next();
            ModifierTarget target = current.getTarget();

            if( current.getOwner() == userid && current.getEquipped() == null &&
                !found.contains( target ) && current.getBattles() > 0 )
            {
                ret.add( current );
                found.add( target );
            }
        }

        return ret;
    }

    /**
     * Gets a specific modifier
     *
     * @param id ID of the modifier to get
     * @throws GameException Thrown if modifier is not found
     * @return Found modifier
     */
    public Modifier getByID( int id )
    {
        ListIterator< Modifier > iter = modifiers.listIterator();

        while( iter.hasNext() )
        {
            Modifier current = iter.next();

            if( current.getID() == id )
            {
                return current;
            }
        }

        throw new GameException( 1, "Modifier not found: " + id );
    }

    /**
     * Creates a modifier for the specified user.
     *
     * Utilizes the luck generator to determine the target and the rarity of
     * the created modifier.
     *
     * @param userid The ID of the user to create the modifier for
     * @param luck Luck of the user ( higher values warrant better modifiers )
     *             Max: 156
     */
    public void addModifier( int userid, int luck )
    {
        int targ = generateLuck( luck, true );
        int rare;
        if( targ == 0 )
        {
            rare = 9;
        }
        else
        {
            rare = generateLuck( luck, false );
        }

        int mult = (int)Math.round( Math.random() * 12 );

        addModifier( Modifier.create( targ, rare, userid, mult ) );
    }

    /**
     * Adds a modifier to the internal list of modifiers.
     *
     * @param modifier Modifier to add
     */
    public void addModifier( Modifier modifier )
    {
        modifiers.add( modifier );
    }

    /**
     * Deletes a modifier, both from the internal list and from the DB.
     *
     * @param modifier Modifier to delete
     */
    public void deleteModifier( Modifier modifier )
    {
        if( !modifiers.remove( modifier ) )
        {
            throw new GameException( 3, "Could not delete modifier because " +
                                     "it was not being tracked." );
        }

        modifier.destroy();
    }

    /**
     * Generates a number along a standard deviation given a luck value.
     *
     * Uses a standard deviation that is weighted by the luck parameter to
     * determine how strong the lower values are.  Higher luck values (up to
     * 255) create a more linear distribution from min to max whereas lower
     * luck values will yield *much* lower returns from the max end.
     *
     * @param luck       Luck the user has
     * @param proficient Include the final value as an option
     * @return The generated number
     */
    private int generateLuck( int luck, boolean proficient )
    {
        // Setup an array with a list of number that get...bigger
        List< Double > powers = new LinkedList<>();
        powers.add( 0.6 );
        powers.add( 1.2 );
        powers.add( 2.0 );
        powers.add( 3.5 );
        powers.add( 5.0 );
        powers.add( 7.5 );
        powers.add( 10.0 );
        powers.add( 12.0 );
        powers.add( 13.0 );
        if( proficient )
        {
            // Special case for proficient
            powers.add( 13.0 );
        }

        // Iterate over each and calculate it's weight
        int i, len = powers.size();
        for( i = 0; i < len; i++ )
        {
            // Weight uses the formula:
            //     X^2
            // X^2 + luck
            // So higher values of luck will give you more evened out
            // distributions between 0-8, whereas lower values causes 0-2 to be
            // very large and 7/8 to be less than 0.01%
            // Finally make it a whole number
            double power = powers.get( i );
            double sqrd = power * power;
            double sqp1 = sqrd + luck;
            powers.set( i, sqrd / sqp1 * 10000 );
        }

        if( proficient )
        {
            // Since proficiency is a special case...
            // Simply increase it by the same amount that the final normal level
            // went up by
            powers.set( 9, powers.get( 8 ) * 2 - powers.get( 7 ) );
        }

        // Generate a random number between 0 and whatever the max is
        double rand = Math.random() * powers.get( powers.size() - 1 );
        int targ = 0;

        // The target will be the first index that is greater than the number
        // Proficient again has special handling where it is not checked for
        // and simply returns 0 if no case was handled.
        int maxlen = proficient ? len - 1 : len;
        for( i = 0; i < maxlen; i++ )
        {
            if( rand < powers.get( i ) )
            {
                targ = i + 1;
                break;
            }
        }

        return targ;
    }

    /**
     * Returns the singleton
     *
     * @return Singleton
     */
    public static ModifierManager getInstance()
    {
        return ModifierManagerHolder.INSTANCE;
    }

    private static class ModifierManagerHolder
    {
        private static final ModifierManager INSTANCE = new ModifierManager();
    }
}

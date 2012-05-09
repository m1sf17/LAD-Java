package lad.db;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.data.GameException;
import lad.data.Minion;
import lad.data.Trainer;

/**
 * Manages all of the trainers (and consequently their minions)
 *
 * @author msflowers
 */
public class TrainerManager extends DBManager
{
    /**
     * The internal list of trainers.
     */
    private List< Trainer > trainers = new LinkedList<>();

    /**
     * Private ctor
     */
    private TrainerManager()
    {
    }

    /**
     * Returns the table profiles to load
     *
     * @return A list with the minion and trainer tables to load
     */
    @Override
    public TableProfile[] profiles()
    {
        return new TableProfile[]{
            Minion.getProfile(),
            Trainer.getProfile()
        };
    }

    /**
     * Returns a list of trainers belonging to a user
     *
     * @param userid The ID of the user to get trainers for
     * @return List of trainers (whether empty or populated)
     */
    public List< Trainer > getTrainersByUser( int userid )
    {
        List< Trainer > ret = new LinkedList<>();
        ListIterator< Trainer > iter = trainers.listIterator();

        while( iter.hasNext() )
        {
            Trainer current = iter.next();
            if( current.getOwner() == userid )
            {
                ret.add( current );
            }
        }

        return ret;
    }

    /**
     * Returns a specific trainer by its ID
     *
     * @param id The ID of the trainer to search for
     * @return Corresponding trainer with the given ID
     * @throws GameException Thrown if the given ID is not found
     */
    public Trainer getTrainerByID( int id )
    {
        ListIterator< Trainer > iter = trainers.listIterator();
        while( iter.hasNext() )
        {
            Trainer current = iter.next();
            if( current.getID() == id )
            {
                return current;
            }
        }
        throw new GameException( 1, "Trainer not found:" + id );
    }

    /**
     * Creates a trainer for the specified user
     *
     * @param userid The ID of the user to create the trainer for
     */
    public void addTrainer( int userid )
    {
        Trainer creation = Trainer.create( userid );
        addTrainer( creation );
    }

    /**
     * Adds a trainer to the internal list of trainers
     *
     * @param trainer Trainer that has been created/loaded
     */
    public void addTrainer( Trainer trainer )
    {
        trainers.add( trainer );
    }

    /**
     * Returns the singleton
     *
     * @return Singleton
     */
    public static TrainerManager getInstance()
    {
        return TrainerManagerHolder.INSTANCE;
    }

    private static class TrainerManagerHolder
    {
        private static final TrainerManager INSTANCE = new TrainerManager();
    }

}

package lad.java;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;
import lad.db.EXPManager;
import lad.db.ModifierManager;
import lad.db.MySQLDB;
import lad.db.TrainerManager;

/**
 * Performs the game loop to update data accordingly.  Run in a separate thread,
 * this class performs all of the updates each frame.  Data is also pulled
 * and pushed from this class to/from the database.  Connecting users are
 * blocked from the data until this loop has finished after which they are
 * allowed to run.
 *
 * @author msflowers
 */
public class GameLoop implements Runnable
{
    /**
     * Locks out access to the game data so only one thread may use it at a
     * time.  Although this thread will periodically lock it, the input threads
     * are also allowed to lock it.
     */
    private Semaphore semaphore = null;

    /**
     * Simple object used to synchronize data
     */
    private final Object data = new Object();

    /**
     * Holds all of the battles taking place
     */
    private final ArrayList< TrainerBattle > battles = new ArrayList<>( 100 );

    /**
     * Initializes the game loop by creating the semaphore and acquiring the
     * first lock on it.
     */
    private GameLoop()
    {
        semaphore = new Semaphore( 1, true );
        try
        {
            semaphore.acquire();
        }
        catch( InterruptedException e )
        {
            System.err.println( "Managed to get interrupted during init " +
                                "of game loop semaphore." );
            System.exit( -1 );
        }
    }

    /**
     * Initializes all handlers that are used.
     *
     * Only getInstance() should need to be called.  Handlers should be strictly
     * I/O until they get control of the input.
     */
    private void initializeHandlers()
    {
        IOInitial.getInstance();
        IOModifier.getInstance();
    }

    /**
     * Connects to MySQL DB and pulls data.
     *
     * Initializes the MySQL Connection and selects the appropriate database.
     * After the connection, all of the data will be pulled.
     */
    private void initializeData()
    {
        // Ensure the MySQL DB is connected
        MySQLDB.getConn();

        // Pull the rest of the data from the DB
        TrainerManager.getInstance().initialize();
        ModifierManager.getInstance().initialize();
        EXPManager.getInstance().initialize();
    }

    /**
     * Gets the singleton
     *
     * @return @see GameLoopHandler.INSTANCE
     */
    public static GameLoop getInstance()
    {
        return GameLoopHolder.INSTANCE;
    }

    /**
     * Acquires the semaphore.
     *
     * By only allowing the semaphore to acquire one lock at a time this will
     * ensure only one thread has control a time.
     *
     * @see release
     * @throws InterruptedException
     */
    public static void acquire() throws InterruptedException
    {
        getInstance().semaphore.acquire();
    }

    /**
     * Releases the semaphore.
     *
     * @see release
     */
    public static void release()
    {
        getInstance().semaphore.release();
    }

    /**
     * Adds a trainer battle to the list
     *
     * @param battle Battle to add
     * @throws InterruptedException Thrown if interrupted while waiting for
     *                              game loop to finish
     */
    public void addTrainerBattle( TrainerBattle battle ) throws
            InterruptedException
    {
        acquire();
        battles.add( battle );
        release();
    }

    /**
     * Performs the thread run loop.
     *
     * Starts by initializing all data.  Runs the game loop and acquires the
     * lock before performing any data and releases it afterword.  When running
     * is set to true the loop is aborted and the thread is finished.
     */
    @Override
    public void run()
    {
        Debug.log( "Started Game Loop Thread", "Thread" );
        initializeHandlers();
        initializeData();
        semaphore.release();
        synchronized( data )
        {
            try
            {
                long lastRunTime = System.currentTimeMillis();
                while( LADJava.running )
                {
                    semaphore.acquire();

                    updateTrainerBattles();
                    semaphore.release();

                    // Only run the loop once a second
                    long elapsedRunTime = System.currentTimeMillis() -
                                          lastRunTime;
                    if( elapsedRunTime < 1000 )
                    {
                        data.wait( 1000 - elapsedRunTime );
                    }
                    lastRunTime = System.currentTimeMillis();
                }
            }
            catch( InterruptedException e )
            {
                //wait
            }
        }
        MySQLDB.notifyRunner();
        Debug.log( "Ended Game Loop Thread", "Thread" );
    }

    /**
     * Updates all of the trainer battles.
     *
     * Cycles through each trainer battle and advances them each one tick.  If
     * the battle is finished the trainers and users are updated accordingly
     * and the battle is removed from the list
     */
    private void updateTrainerBattles()
    {
        ListIterator< TrainerBattle > iter = battles.listIterator();
        while( iter.hasNext() )
        {
            // Tick each battle forward one second
            TrainerBattle current = iter.next();
            current.tick( 1 );

            if( current.isFinished() )
            {
                trainerPostBattle( current.getLoser(), false );
                trainerPostBattle( current.getWinner(), true );
                // TODO: Restart battle?
                iter.remove();
            }
        }
    }

    /**
     * Updates a trainer's stats after a battle.
     *
     * Iterates over each of the modifiers the trainer had equipped and
     * update's their stats.  Also, updates the user's proficiency if there
     * was no proficiency modifier equipped.  Aborts if the trainer is an NPC.
     *
     * @param trainer Trainer that finished the battle
     * @param won True if the trainer won, false otherwise
     */
    private void trainerPostBattle( ArenaTrainer trainer, boolean won )
    {
        // Abort if NPC
        if( trainer.getTrainer().isNPC() )
        {
            return;
        }

        // Variables
        boolean hadProfMod = false;
        ListIterator< Modifier > iter = trainer.getModifiers().listIterator();

        // Advance each of the modifiers, also check if a proficiency mod was
        // found
        while( iter.hasNext() )
        {
            Modifier current = iter.next();
            current.battled( won, trainer.getWeapon() );

            if( current.getTarget() == ModifierTarget.Proficiency )
            {
                hadProfMod = true;
            }
        }

        // If no proficiency mod and the trainer won, manually increment
        // proficiency
        if( !hadProfMod && won )
        {
            int user = trainer.getTrainer().getOwner();
            UserExpTarget generalTarget = UserExpTarget.generalFromWeapon(
                                          trainer.getWeapon() );
            UserExpTarget specificTarget = UserExpTarget.specificFromWeapon(
                                          trainer.getWeapon() );
            ModifierTarget target = ModifierTarget.Proficiency;
            EXPManager.grantUserEXP( user, generalTarget, target, 1 );
            EXPManager.grantUserEXP( user, specificTarget, target, 2 );
        }
    }

    private static class GameLoopHolder
    {
        private static final GameLoop INSTANCE = new GameLoop();
    }

}

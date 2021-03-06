package lad.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Semaphore;
import lad.data.GameException;
import lad.data.Modifier;
import lad.data.ModifierTarget;
import lad.data.Trainer;
import lad.data.UserExpTarget;
import lad.data.Weapon;
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
    private final List< TrainerBattle > battles = new ArrayList<>( 100 );

    /**
     * Holds all of the trainers waiting to battle
     */
    private final Map< Trainer, Long > battleQueue = new HashMap<>( 100 );

    /**
     * Holds all of the weapons the trainers are going to wield
     */
    private final Map< Trainer, Weapon > battleWeapons = new HashMap<>( 100 );

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
     * @see lad.game.GameLoop#release()
     * @throws InterruptedException
     */
    public static void acquire() throws InterruptedException
    {
        getInstance().semaphore.acquire();
    }

    /**
     * Releases the semaphore.
     *
     * @see lad.game.GameLoop#acquire()
     */
    public static void release()
    {
        getInstance().semaphore.release();
    }

    /**
     * Adds a trainer to the battle queue.
     *
     * @param trainer Trainer to queue for battling in the arena
     * @param weapon  Weapon the trainer will be fighting with
     * @throws InterruptedException Thrown if interrupted while acquiring
     */
    public static void queueTrainer( Trainer trainer, Weapon weapon )
            throws InterruptedException
    {
        acquire();
        trainer.setBattleState( Trainer.BattleState.InBattleQueue );
        getInstance().battleQueue.put( trainer, System.currentTimeMillis() );
        getInstance().battleWeapons.put( trainer, weapon );
        release();
    }

    /**
     * Removes a trainer from the battle queue.
     *
     * @param trainer Trainer to dequeue from battling in the arena
     * @throws InterruptedException Thrown if interrupted while acquiring
     */
    public static void dequeueTrainer( Trainer trainer )
            throws InterruptedException
    {
        acquire();
        trainer.setBattleState( Trainer.BattleState.NoBattle );
        getInstance().battleQueue.remove( trainer );
        getInstance().battleWeapons.remove( trainer );
        release();
    }

    /**
     * Gets the time left a trainer has left in it's trainer battle.
     *
     * @param trainer Trainer to look up
     * @return Time(ticks) remaining in the battle
     * @throws GameException Thrown if the trainer is not in a battle.
     */
    public static int getTimeLeftInTrainerBattle( Trainer trainer )
    {
        ListIterator< TrainerBattle > iter =
                getInstance().battles.listIterator();
        while( iter.hasNext() )
        {
            TrainerBattle battle = iter.next();
            if( battle.hasTrainer( trainer ) )
            {
                return battle.getTicksRemaining();
            }
        }
        throw new GameException( 1, "Trainer is not in a battle." );
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
                    updateTrainerBattles( lastRunTime );
                    pumpTrainerBattleQueue( lastRunTime );
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
     *
     * @param currentTime Current system time in millis
     */
    private void updateTrainerBattles( Long currentTime )
    {
        ListIterator< TrainerBattle > iter = battles.listIterator();
        while( iter.hasNext() )
        {
            // Tick each battle forward one second
            TrainerBattle current = iter.next();
            current.tick( 1 );

            if( current.isFinished() )
            {
                ArenaTrainer loser = current.getLoser();
                ArenaTrainer winner = current.getWinner();
                trainerPostBattle( currentTime, loser, false );
                trainerPostBattle( currentTime, winner, true );
                iter.remove();

                final String log = "TrainerBattleResults";
                if( Debug.isLogEnabled( log ) )
                {
                    String winnerName = winner.getTrainer().isNPC() ?
                                        "NPC" : "Trainer #" +
                                        winner.getTrainer().getID();
                    String loserName = loser.getTrainer().isNPC() ?
                                       "NPC" : "Trainer #" +
                                       loser.getTrainer().getID();
                    String winnerWeapon = "(" + winner.getWeapon().toString() +
                                          ")";
                    String loserWeapon = "(" + loser.getWeapon().toString() +
                                         ")";

                    Debug.log( winnerName + winnerWeapon + " won against " +
                               loserName + loserWeapon, log );
                }
            }
        }
    }

    /**
     * Updates a trainer's stats after a battle.
     *
     * Iterates over each of the modifiers the trainer had equipped and
     * update's their stats.  Also, updates the user's proficiency if there
     * was no proficiency modifier equipped.  Also adds all of the battle
     * statistics to the trainer accordingly.  Aborts if the trainer is an NPC.
     *
     * @param currentTime Current system time in millis
     * @param trainer Trainer that finished the battle
     * @param won True if the trainer won, false otherwise
     */
    private void trainerPostBattle( Long currentTime, ArenaTrainer trainer,
                                    boolean won )
    {
        // Abort if NPC
        if( trainer.getTrainer().isNPC() )
        {
            return;
        }

        // Put the trainer back into the queue
        final Long winnerExtraTime = 0L;
        final Long loserExtraTime = 30000L;
        final Long extraTime = won ? winnerExtraTime : loserExtraTime;
        battleQueue.put( trainer.getTrainer(), currentTime + extraTime );
        battleWeapons.put( trainer.getTrainer(), trainer.getWeapon() );
        trainer.getTrainer().setBattleState(
                Trainer.BattleState.InBattleQueue );

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

        // Add in the statistics
        Trainer trnr = trainer.getTrainer();
        int[] intStats = trainer.getStorableIntStatistics();
        double[] doubleStats = trainer.getStorableDoubleStatistics();
        intStats[ intStats.length - 1 ] = won ? 1 : 0;
        EXPManager.addBattleStats( 1, trnr.getOwner(), intStats, doubleStats );
        EXPManager.addBattleStats( 2, trnr.getID(), intStats, doubleStats );
    }

    /**
     * Pumps the trainer battle queue.
     *
     * @param currentTime Current system time in millis
     */
    private void pumpTrainerBattleQueue( long currentTime )
    {
        final long queueTime = 30000; // 30 secs
        final long waitTime = 30000; // 30 secs
        Iterator< Trainer > iter = battleQueue.keySet().iterator();

        Trainer trainerOnDeck = null;
        Trainer trainerToBattleNPC = null;
        while( iter.hasNext() )
        {
            Trainer trainer = iter.next();
            Long entryTime = battleQueue.get( trainer );
            Long timeInQueue = currentTime - entryTime;

            // Ignore this trainer if it hasn't finished it's queue
            if( timeInQueue < queueTime )
            {
                continue;
            }

            // Trainer is able to battle, if another trainer can as well,
            // Battle the two
            if( trainerOnDeck != null )
            {
                Weapon weapon = battleWeapons.get( trainer );
                Weapon onDeckWeapon = battleWeapons.get( trainerOnDeck );
                battles.add( TrainerBattle.battle(
                        trainerOnDeck, trainer, onDeckWeapon, weapon ) );
                trainerOnDeck = null;
                battleQueue.remove( trainer );
                battleWeapons.remove( trainer );
                battleQueue.remove( trainerOnDeck );
                battleWeapons.remove( trainerOnDeck );
            }

            // This trainer has reached the max time, fight an NPC
            if( timeInQueue > queueTime + waitTime )
            {
                trainerToBattleNPC = trainer;
            }

            // This trainer is no longer in the queue, can fight and may end up
            // fighting an NPC.  Set it as "on deck"
            trainerOnDeck = trainer;
        }

        // Nobody wanted to fight this trainer for the max time, so fight an NPC
        if( trainerToBattleNPC != null )
        {
            trainerOnDeck = null;
            battles.add( TrainerBattle.battleNPC( trainerToBattleNPC,
                         battleWeapons.get( trainerToBattleNPC ) ) );
            battleQueue.remove( trainerToBattleNPC );
            battleWeapons.remove( trainerToBattleNPC );
        }

        // Trainer is on deck and nobody else wants to fight, set state
        if( trainerOnDeck != null )
        {
            trainerOnDeck.setBattleState(
                    Trainer.BattleState.LookingForBattle );
        }
    }

    private static class GameLoopHolder
    {
        private static final GameLoop INSTANCE = new GameLoop();
    }

}

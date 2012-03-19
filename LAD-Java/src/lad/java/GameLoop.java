package lad.java;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Semaphore;
import lad.db.MySQLDB;

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
     * Holds whether this thread should keep running or stop
     */
    public static boolean running = true;

    /**
     * Simple object used to synchronize data
     */
    private Object data = new Object();

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
        InitialIO.getInstance();
    }

    /**
     * Connects to MySQL DB and pulls data.
     *
     * Initializes the MySQL Connection and selects the appropriate database.
     * After the connection, all of the data will be pulled.
     */
    private void initializeData()
    {
        try
        {
            Connection conn = MySQLDB.getConn();
            Statement stmt = conn.createStatement();
            stmt.executeQuery( "USE admin_lad" );

            // Pull the rest of the data from the DB
        }
        catch( SQLException e )
        {
            System.err.println( "Error while initializing data from DB." );
            System.err.println( e.toString() );
            System.exit( -1 );
        }
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
     * Performs the thread run loop.
     *
     * Starts by initializing all data.  Runs the game loop and acquires the
     * lock before performing any data and releases it afterword.  When running
     * is set to true the loop is aborted and the thread is finished.
     */
    @Override
    public void run()
    {
        initializeHandlers();
        initializeData();
        semaphore.release();
        synchronized( data )
        {
            try
            {
                while( running )
                {
                    semaphore.acquire();
                    data.wait( 980 );
                    // Perform logic here instead of the two waits
                    semaphore.release();
                    data.wait( 20 );
                }
            }
            catch( InterruptedException e )
            {
                //wait
            }
        }
    }

    private static class GameLoopHolder
    {
        private static final GameLoop INSTANCE = new GameLoop();
    }

}

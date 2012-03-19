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
    public static boolean running = true;
    private Object data = new Object();

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

    private void initializeHandlers()
    {
        InitialIO.getInstance();
    }

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

    public static GameLoop getInstance()
    {
        return GameLoopHolder.INSTANCE;
    }
    
    public static void acquire() throws InterruptedException
    {
        getInstance().semaphore.acquire();
    }

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

package lad.game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Main entry point for server
 *
 * @author msflowers & kakon
 */
public class LADJava
{
    /**
     * Global variable to hold whether any threads should stop running
     */
    static public boolean running = true;

    /**
     * Awaits a connection from the PHP server, handles the server/client
     * sockets and creates a thread to message back to the PHP server.
     *
     * If a connection is not made within 1000 milliseconds the endless loop is
     * repeated.  The loop is broken by the setting the static variable
     * listening to false
     *
     * @param args Unused
     */
    public static void main(String[] args)
    {
        // Set debug state
        Debug.enableLog( "Thread" );
        Debug.enableLog( "BATTLEMATCHES" );
        // Variables
        ServerSocket socket = null;

        // Try and create the server socket on port 19191, if it can't be
        // made, then abort.
        try
        {
            socket = new ServerSocket( 19191 );
            socket.setSoTimeout( 1000 );
        }
        catch( IOException e )
        {
            System.err.println( "Could not listen on port 19191." );
            System.exit( -1 );
        }

        // Start up the game loop thread
        new Thread( GameLoop.getInstance() ).start();

        // Loop until we should stop listening
        while( running )
        {
            try
            {
                // Make a socket for accepting data on and wait until we have
                // a connection made
                Socket socketClient;
                socketClient = socket.accept();
                new Thread( new IOThread( socketClient ) ).start();

            }
            catch( SocketTimeoutException te )
            {
                // Do nothing because this will happen every second that
                // someone does not connect
            }
            catch( IOException i )
            {
                // This really shouldn't happen but just in case something broke
                System.err.println( i.toString() );
                System.err.println( "IO Exception with socket." );
                System.exit( -1 );
            }
        }

        // Try to close the server socket since we're all done
        try
        {
            socket.close();
        }
        catch( IOException e )
        {
            System.err.println( "Failed to close socket." );
            System.exit( -1 );
        }

        System.out.println( "-=- LAD FINISHED -=-" );
    }
}

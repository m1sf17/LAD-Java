package lad.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * The IO Thread that handles input/output from the PHP server.  Once a socket
 * is established this thread handles all of the input/output from the socket.
 *
 * @author msflowers
 */
public class IOThread implements Runnable
{
    /**
     * The client that is requesting an action
     */
    private Socket client;

    /**
     * Basic ctor
     *
     * @param n_client The client that is to be run
     */
    public IOThread( Socket n_client )
    {
        client = n_client;
    }

    /**
     * Executes in a separate thread.  Pulls input from the PHP server and
     * replies with the JavaScript to be run client side.
     */
    @Override
    public void run()
    {
        long startTime = System.nanoTime();
        try
        {
            // A connection has been made, setup the input/output reader/writer
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader( new InputStreamReader(
                client.getInputStream() ) );
            String inputLine;

            // Loop over each line of input
            // If we get the end transmission string then we're done
            String processedString = "";
            while( ( inputLine = in.readLine()) != null && 
                     !inputLine.equals( "end,transmission" ) &&
                     !inputLine.equals( "end,server" ) )
            {
                processedString += inputLine + "\n";
            }

            if( inputLine.equals( "end,server" ) )
            {
                LADJava.running = false;
                out.write( "genericErrorDialog('Server Shutdown'," +
                           "'Shutdown completed.');" );
            }
            else
            {
                // Send input to handler to get output
                MessageManager mgr = new MessageManager();
                out.write( mgr.handle( processedString ) );
                mgr.finished();
            }

            // Inform the PHP server we're done
            out.write( "\nDONE\n" );

            // Close the reader/writer so they are flushed
            out.close();
            in.close();
        }
        catch( IOException e )
        {
            Debug.log( "IO Exception" + e.getMessage(), "THREAD" );
        }

        // Calculate elapsed time
        long elapsedTime = System.nanoTime() - startTime;
        float ms = (float)elapsedTime / 1000000;
        String log = String.format( "Ran IO Thread in %.4f ms\n", ms );
        Debug.log( log );
    }
}

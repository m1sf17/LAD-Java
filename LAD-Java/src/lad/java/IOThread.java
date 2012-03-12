package lad.java;

import java.io.*;
import java.net.*;

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
     * Constructor
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
        try
        {
            // A connection has been made, setup the input/output reader/writer
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader( new InputStreamReader(
                client.getInputStream() ) );
            String inputLine, outputLine;

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
                LADJava.listening = false;
            }

            // Send input to handler to get output
            outputLine = MessageManager.getInstance().handle( processedString );
            out.write( outputLine );

            // Inform the PHP server we're done
            out.write( "DONE\n" );

            // Close the reader/writer so they are flushed
            out.close();
            in.close();
        }
        catch( IOException e )
        {
            System.out.println( "IO Exception" );
            System.exit( -1 );
        }
    }

}

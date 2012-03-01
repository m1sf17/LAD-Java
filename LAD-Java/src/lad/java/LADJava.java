package lad.java;

import java.io.*;
import java.net.*;

/**
 * Main entry point for server
 *
 * @author msflowers & kakon
 */
public class LADJava {

    /**
     * Awaits a connection from the PHP server, handles the server/client
     * sockets and dispatches the message back to the PHP server
     *
     * @param args Unused
     */
    public static void main(String[] args)
    {
        // Variables
        ServerSocket socket = null;
        boolean listening = true;

        // Try and create the server socket on port 19191, if it can't be
        // made, then abort
        try
        {
            socket = new ServerSocket( 19191 );
        }
        catch( IOException e )
        {
            System.err.println( "Could not listen on port 19191." );
            System.exit( -1 );
        }

        // Loop until we should stop listening
        while( listening )
        {
            try
            {
                // Make a socket for accepting data on and wait until we have
                // a connection made
                Socket socketClient;
                socketClient = socket.accept();

                // A connection has been made, setup the input/output reader/writer
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);
                BufferedReader in = new BufferedReader( new InputStreamReader(
                    socketClient.getInputStream() ) );
                String inputLine, outputLine;

                // Loop over each line of input
                // If we get the end transmission string then we're done
                while( ( inputLine = in.readLine()) != null && !inputLine.equals( "end,transmission" ) )
                {
                    System.out.println( "Got " );
                    System.out.println( inputLine );
                }

                // Dummy output
                outputLine = "alert('This response is from the Java Server!');\n";
                out.write( outputLine );

                // Inform the PHP server we're done
                out.write( "DONE\n" );

                // Close the reader/writer so they are flushed
                out.close();
                in.close();
            }
            catch( IOException i )
            {
                // This really shouldn't happen but just in case something broke
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
    }
}

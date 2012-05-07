package lad.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import lad.data.GameException;

/**
 * Manages all incoming messages and dispatches them to the appropriate handler.
 *
 * Each handler initializes itself by adding the messages it can handle to the
 * list handlerMap.  When an incoming message is received each piece is checked
 * in the map for a match.  An asterisk denotes the value does not need to be
 * matched.  A single manager is responsible for each thread.  However, each
 * handler is global, thus all of the output data is handled here in the
 * manager.
 *
 * @author msflowers
 */
public class MessageManager
{
    /**
     * Internal buffer for storing text to write.
     */
    protected final StringBuffer buffer = new StringBuffer( 1024 );

    /**
     * Holds the mapping of all message pieces to their corresponding
     * handler.
     */
    private static Map< MessagePiece, MessageHandler > handlerMap =
                   new HashMap<>( 100 );

    /**
     * Holds the list of managers (regardless of thread)
     */
    private static final Map< Thread, MessageManager > managerList =
            Collections.synchronizedMap(
              new HashMap< Thread, MessageManager >( 10 )
            );;

    /**
     * Public ctor, adds this manager to the internal list.
     */
    public MessageManager()
    {
        started();
    }

    /**
     * Gets the correct manager for the current thread.
     *
     * @return Manager for the current thread
     */
    public static MessageManager getInstance()
    {
        return managerList.get( Thread.currentThread() );
    }

    /**
     * Adds a handler to the map of messages along with the pieces it runs.
     *
     * @param pieces List of pieces that the handler can run
     * @param handle The actual handler that gets run when a message is matched
     */
    public static void addHandler( MessageList pieces, MessageHandler handle )
    {
        int len = pieces.size();
        for( int i = 0; i < len; i++ )
        {
            handlerMap.put( pieces.get( i ), handle );
        }
    }

    /**
     * Handles input from a user.
     *
     * Attempts to match the message pairs in the parameter to one of the
     * handler that is registered.  If a pair is matched then the matching
     * handler is run with all of the pairs.
     *
     * @param pairs All of the pairs in a single string
     * @return Dependent on whichever handler is run
     */
    public String handle( String pairs )
    {
        // Userid of the user
        int userid = -1;

        // Split up the string on new lines
        String[] strings = pairs.split( "\n" );

        // Create the list of message pieces by iterating over each line
        // and setting the text before the first comma to the variable and the
        // text after the first comma to the value
        MessageList pieces = new MessageList();
        for( int i = 0; i < strings.length; i++ )
        {
            String var, val, current = strings[ i ];
            int commaindex = current.indexOf( ',' );
            // If no comma was found, skip this message
            if( commaindex <= 0 )
            {
                System.out.println( "Invalid message from client: " + current );
                break;
            }
            var = current.substring( 0, commaindex );
            val = current.substring( commaindex + 1 );

            if( var.compareTo( "userid" ) == 0 )
            {
                userid = Integer.decode( val );
            }
            else
            {
                pieces.add( new MessagePiece( var, val ) );
            }
        }

        // If no valid messages were found something went wrong or the user sent
        // something bad
        if( pieces.size() <= 0 )
        {
            System.out.println( "No valid messages received from client." );
            return "";
        }

        // If we didn't get the userid packet, something went wrong...very.
        if( userid == -1 )
        {
            System.out.println( "Did not receive userid packet." );
            return "";
        }

        // Iterate over the list of sent messages against the list of handler
        // messages and check for a match
        ListIterator< MessagePiece > iter = pieces.listIterator();
        MessageHandler handler = null;
        while( iter.hasNext() && handler == null )
        {
            MessagePiece userPiece = iter.next();
            // Now get the handler messages and iterate over each of those
            Set< MessagePiece > handlerpieces = handlerMap.keySet();
            Iterator< MessagePiece > handleriter = handlerpieces.iterator();
            while( handleriter.hasNext() )
            {
                // First check if the variables match
                MessagePiece handlerpiece = handleriter.next();
                if( handlerpiece.getVariable().compareTo(
                        userPiece.getVariable() ) != 0 )
                {
                    continue;
                }
                // Variables match, values can either match or have the handler
                // equal to '*'
                String handlervalue = handlerpiece.getValue();
                if( handlervalue.compareTo( "*" ) == 0 ||
                    handlervalue.compareTo( userPiece.getValue() ) == 0 )
                {
                    handler = handlerMap.get( handlerpiece );
                }
            }
        }

        // If no handler was found then abort
        if( handler == null )
        {
            return debugInfo( "unhandled", null, pairs );
        }

        // Run the handler and return it's result
        try
        {
            handler.doHandle( pieces, userid );
            return buffer.toString();
        }
        catch( NumberFormatException n )
        {
            // Parse error (typically with a number)
            return debugInfo( "Parse error", n, pairs );
        }
        catch( InterruptedException ie )
        {
            // Interrupted (system abort?)
            return debugInfo( "System interrupted", ie, pairs );
        }
        catch( GameException ge )
        {
            // A generic game error (most common)
            return debugInfo( "Game error", ge, pairs );
        }
        catch( Exception e )
        {
            // Some other kind of error
            return debugInfo( "Unhandled error", e, pairs );
        }
    }

    /**
     * Output some debug info when an error occurs
     *
     * @param pairs The pairs that the system found.
     * @return A dialog saying that an error occurred.
     */
    private String debugInfo( String error, Exception e, String pairs )
    {
        if( error.compareTo( "unhandled" ) == 0 )
        {
            System.out.print( "No message was matched in " );
            System.out.print( handlerMap.size() );
            System.out.println( " message checks." );
        }

        System.out.println( error );
        System.out.println( "=====PAIRS=====" );
        System.out.print( pairs );
        System.out.println( "===END PAIRS===" );
        if( e != null )
        {
            e.printStackTrace();
        }

        String title = "Server Error";
        String msg = "An error occurred.  Please try again later.";
        String func = "$('div#LAD').dialog( \"close\" );";
        if( e instanceof GameException )
        {
            // If it's a low severity simply go back to main page
            if( 1 == ((GameException)e).getSeverity())
            {
                func = "$.ladAjax({'viewalltrainers':''});";
            }
        }
        return "genericErrorDialog('" + title + "','" + msg +
                "',function(){ " + func + "});";
    }
    
    /**
     * Adds this manager to the list (it is starting).
     */
    private void started()
    {
        synchronized( managerList )
        {
            managerList.put( Thread.currentThread(), this );
        }
    }

    /**
     * Removes this manager from the list (it is done).
     */
    public void finished()
    {
        synchronized( managerList )
        {
            managerList.remove( Thread.currentThread() );
        }
    }

    /**
     * Simply writes a string to the internal buffer.
     *
     * @param str String to write
     */
    protected void write( String str )
    {
        buffer.append( str );
    }

    /**
     * Overwrites the string in the internal buffer.
     *
     * Useful if text has already been written to the buffer, then an error
     * occurs.  Then, the error dialog would be shown but none of the prepared
     * text.
     *
     * @param str String to write
     */
    protected void writeReplace( String str )
    {
        buffer.setLength( 0 );
        buffer.append( str );
    }
}

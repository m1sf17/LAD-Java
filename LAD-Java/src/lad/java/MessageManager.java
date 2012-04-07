package lad.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Manages all incoming messages and dispatches them to the appropriate handler.
 *
 * Each handler initializes itself by adding the messages it can handle to the
 * list @see handlerMap.  When an incoming message is received each piece is
 * checked in the map for a match.  An asterisk denotes the value does not need
 * to be matched.
 *
 * @author msflowers
 */
public class MessageManager
{
    /**
     * Holds the mapping of all message pieces to their corresponding
     * handler.
     */
    private HashMap< MessagePiece, MessageHandler > handlerMap =
            new HashMap<>( 100 );

    /**
     * Private ctor, doesn't do anything.
     */
    private MessageManager()
    {
    }

    /**
     * Gets the singleton
     *
     * @return The singleton
     */
    public static MessageManager getInstance()
    {
        if( MessageManagerHolder.INSTANCE == null )
        {
            MessageManagerHolder.INSTANCE = new MessageManager();
        }
        return MessageManagerHolder.INSTANCE;
    }

    /**
     * Adds a handler to the map of messages along with the pieces it runs.
     *
     * @param pieces List of pieces that the handler can run
     * @param handle The actual handler that gets run when a message is matched
     */
    public void addHandler( List< MessagePiece > pieces, MessageHandler handle )
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
        LinkedList< MessagePiece > pieces = new LinkedList<>();
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
            System.out.print( "No message was matched in " );
            System.out.print( handlerMap.size() );
            System.out.println( " message checks." );

            System.out.println( "=====PAIRS=====" );
            System.out.print( pairs );
            System.out.println( "===END PAIRS===" );

            String title = "Server Error";
            String msg = "Not implemented.  Please try again later.";
            String func = "$('#LAD.popup .close_popup').trigger(\"click\");";
            return "genericErrorDialog('" + title + "','" + msg +
                    "',function(){ " + func + "});";
        }

        // Run the handler and return it's result
        return handler.handle( pieces, userid );
    }

    /**
     * Private class that manages the singleton.
     */
    private static class MessageManagerHolder
    {
        /**
         * The actual singleton which is created at runtime.
         */
        private static MessageManager INSTANCE = null;
    }

}

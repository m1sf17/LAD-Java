package lad.java;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Utility class for a linked list of message pieces
 *
 * @author msflowers
 */
public class MessageList extends LinkedList< MessagePiece >
{
    /**
     * Serial version of the class
     */
    final static private long serialVersionUID = 1;

    /**
     * Gets a specific piece based on the variable
     *
     * @param variable The variable of the piece to get
     * @return The message piece with the variable
     */
    public MessagePiece get( String variable )
    {
        ListIterator< MessagePiece > iter = listIterator();
        while( iter.hasNext() )
        {
            MessagePiece piece = iter.next();
            if( piece.getVariable().compareToIgnoreCase( variable ) == 0 )
            {
                return piece;
            }
        }
        return null;
    }

    /**
     * Gets a specific piece based on the variable
     *
     * @param variable The variable of the piece to get
     * @return The message piece with the variable
     */
    public String getValue( String variable )
    {
        ListIterator< MessagePiece > iter = listIterator();
        while( iter.hasNext() )
        {
            MessagePiece piece = iter.next();
            if( piece.getVariable().compareToIgnoreCase( variable ) == 0 )
            {
                return piece.getValue();
            }
        }
        return null;
    }
}

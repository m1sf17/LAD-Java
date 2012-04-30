package lad.game;

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
     * @throws IndexOutOfBoundsException Thrown if the variable is not found
     */
    public MessagePiece get( String variable )
            throws IndexOutOfBoundsException
    {
        ListIterator< MessagePiece > iter = listIterator();
        while( iter.hasNext() )
        {
            MessagePiece piece = iter.next();
            if( piece.equals( variable ) )
            {
                return piece;
            }
        }
        throw new IndexOutOfBoundsException( "Message piece not found: " +
                                             variable );
    }

    /**
     * Gets a specific piece based on the variable
     *
     * @param variable The variable of the piece to get
     * @return The message piece with the variable
     * @throws IndexOutOfBoundsException Thrown if the variable is not found
     */
    public String getValue( String variable )
            throws IndexOutOfBoundsException
    {
        return get( variable ).getValue();
    }
}

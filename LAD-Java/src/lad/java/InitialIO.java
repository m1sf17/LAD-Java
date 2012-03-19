package lad.java;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles initial connection with users to the java module.
 *
 * @author msflowers
 */
public class InitialIO extends MessageHandler
{
    /**
     * Calls super
     */
    private InitialIO()
    {
        super();
    }

    /**
     * Returns singleton
     *
     * @return Singleton.
     */
    public static InitialIO getInstance()
    {
        return InitialIOHolder.INSTANCE;
    }

    /**
     * Handleable pieces.
     *
     * Piece: login,
     * @return
     */
    @Override
    public List< MessagePiece > getPieces()
    {
        List< MessagePiece > pieces = new LinkedList<>();
        pieces.add( new MessagePiece( "login", "*" ) );
        return pieces;
    }

    /**
     * Handles pieces
     * @param pieces Ignored
     * @return An alert saying we got it.
     */
    @Override
    public String handle( List< MessagePiece > pieces )
    {
        return "alert('Yay!');";
    }

    private static class InitialIOHolder
    {
        private static final InitialIO INSTANCE = new InitialIO();
    }
}

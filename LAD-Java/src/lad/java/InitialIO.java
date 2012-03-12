package lad.java;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author msflowers
 */
public class InitialIO extends MessageHandler
{
    private InitialIO()
    {
        super();
    }

    public static InitialIO getInstance()
    {
        return InitialIOHolder.INSTANCE;
    }

    private static class InitialIOHolder
    {

        private static final InitialIO INSTANCE = new InitialIO();
    }

    @Override
    public List< MessagePiece > getPieces()
    {
        List< MessagePiece > pieces = new LinkedList<>();
        pieces.add( new MessagePiece( "login", "*" ) );
        return pieces;
    }

    @Override
    public String handle( List< MessagePiece > pieces )
    {
        return "alert('Yay!');";
    }
}

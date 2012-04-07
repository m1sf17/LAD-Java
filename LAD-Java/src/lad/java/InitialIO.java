package lad.java;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.db.TrainerManager;

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
     * Login piece for comparing to
     */
    private final static MessagePiece loginPiece = new MessagePiece( "login" );

    /**
     * Handleable pieces.
     *
     * Piece: login,
     * Piece: viewtrainers,
     *
     * @return
     */
    @Override
    public List< MessagePiece > getPieces()
    {
        List< MessagePiece > pieces = new LinkedList<>();
        pieces.add( new MessagePiece( loginPiece ) );
        pieces.add( new MessagePiece( "viewtrainers" ) );
        return pieces;
    }

    /**
     * Handles pieces
     * @param pieces Decides which response to show
     *
     * @return An alert saying we got it.
     */
    @Override
    public String handle( List< MessagePiece > pieces, int userid )
    {
        String output = "";

        if( pieces.contains( loginPiece ) )
        {
            output += "function doJava( params ){" +
                        "doAjax( 'java_run', params );" +
                      "}";
            output += "function java(){" +
                        "return getPopupContext('LAD');" +
                      "}";
        }

        if( pieces.contains( loginPiece ) ||
            pieces.contains( new MessagePiece( "viewtrainers" ) ) )
        {
            LinkedList< Trainer > trainers =
                TrainerManager.getInstance().getTrainersByUser( userid );
            ListIterator< Trainer > iter = trainers.listIterator();

            int index = 1;

            while( iter.hasNext() )
            {
                Trainer curr = iter.next();
                output += "java().append('";
                output += "Trainer " + index + ": Level ";
                output += curr.getLevel() + " Exp:" + curr.getExp();
                output += "<br>');";
                output += "$('<button>View</button>').button().click(" +
                          "function(){" +
                            "doJava( { viewtrainer: " + index + "});" +
                          "}).appendTo( java() );";
            }

            output += "$('<button>Add Trainer</button>').button().click(" +
                      "function(){" +
                        "doJava( { addtrainer: '*' });" +
                      "}).appendTo( java() );";
        }
        return output;
    }

    private static class InitialIOHolder
    {
        private static final InitialIO INSTANCE = new InitialIO();
    }
}

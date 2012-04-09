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
    private final static MessagePiece
            loginPiece = new MessagePiece( "login" );

    /**
     * View trainers piece for comparing to
     */
    private final static MessagePiece
            viewtrainersPiece = new MessagePiece( "viewtrainers" );

    /**
     * Add trainers piece for comparing to
     */
    private final static MessagePiece
            addtrainerPiece = new MessagePiece( "addtrainer" );

    /**
     * Train minion piece for comparing to
     */
    private final static MessagePiece
            trainminionPiece = new MessagePiece( "trainminion" );

    /**
     * Add minion piece for comparing to
     */
    private final static MessagePiece
            addminionPiece = new MessagePiece( "addminion" );

    /**
     * Battle minions piece for comparing to
     */
    private final static MessagePiece
            battleminionPiece = new MessagePiece( "battleminion" );

    /**
     * Handleable pieces.
     *
     * Piece: @see loginPiece
     * Piece: @see viewtrainersPiece
     * Piece: @see addtrainerPiece
     * Piece: @see trainminionPiece
     * Piece: @see addminionPiece
     * Piece: @see battleminionPiece
     *
     * @return List with all of the above pieces
     */
    @Override
    public List< MessagePiece > getPieces()
    {
        List< MessagePiece > pieces = new LinkedList<>();
        pieces.add( new MessagePiece( loginPiece ) );
        pieces.add( new MessagePiece( viewtrainersPiece ) );
        pieces.add( new MessagePiece( addtrainerPiece ) );
        pieces.add( new MessagePiece( trainminionPiece ) );
        pieces.add( new MessagePiece( addminionPiece ) );
        pieces.add( new MessagePiece( battleminionPiece ) );
        return pieces;
    }

    /**
     * Handles pieces based on their pieces
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
            output += outputTrainerView( userid );
        }
        return output;
    }

    /**
     * Output the default view for viewing trainers
     *
     * @param userid ID of the requesting user
     *
     * @return A string with the resulting text
     */
    private String outputTrainerView( int userid )
    {
        String output = "";
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

        return output;
    }

    private static class InitialIOHolder
    {
        private static final InitialIO INSTANCE = new InitialIO();
    }
}

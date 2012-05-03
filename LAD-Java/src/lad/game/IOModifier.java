package lad.game;

import java.util.List;
import java.util.ListIterator;
import lad.data.Modifier;
import lad.db.ModifierManager;

/**
 * Handles modifier-related messages with the user.
 *
 * @author msflowers
 */
public class IOModifier extends MessageHandler
{
    /**
     * Calls super
     */
    private IOModifier()
    {
        super();
    }

    /**
     * Returns singleton
     *
     * @return Singleton
     */
    public static IOModifier getInstance()
    {
        return IOModifierHolder.INSTANCE;
    }

    /**
     * Viewing all modifiers for comparing to
     */
    private final static MessagePiece
            viewModifiersPiece = new MessagePiece( "viewmodifiers" );

    /**
     * Handleable pieces.
     *
     * Piece: viewModifiersPiece
     *
     * @return List with the above pieces
     */
    @Override
    public MessageList getPieces()
    {
        MessageList pieces = new MessageList();
        pieces.add( viewModifiersPiece );
        return pieces;
    }

    /**
     * Handles pieces based on their pieces
     */
    @Override
    public void handle( MessageList pieces, int userid )
    {
        if( pieces.contains( viewModifiersPiece ) )
        {
            ModifierManager mm = ModifierManager.getInstance();
            List< Modifier > modifiers =
                    mm.getByUserID( userid );

            // Output a fancy table if possible
            if( modifiers.size() > 0 )
            {
                ListIterator< Modifier > iter = modifiers.listIterator();
                int index = 1;

                write( "var headers = [];" );
                write( "headers[ 'Type' ] = 'true';" );
                write( "headers[ 'Battles' ] = 'true';" );
                write( "headers[ 'Action' ] = '';" );
                write( "java().append(" );

                write( "makeSortableTable(headers," );
                write( "[" );
                while( iter.hasNext() )
                {
                    Modifier curr = iter.next();

                    write( "['" + curr.toString() +
                           "'," + curr.getBattles() + "," +
                           "'']" );
                    index++;

                    if( iter.hasNext() )
                    {
                        write( "," );
                    }
                }
                write( "],'modifiers'));" );
            }
        }

        // And include the button to return to trainer
        IOInitial.getInstance().outputReturnToMainButton();

        MessageManager.getInstance().clearJava();
    }

    private static class IOModifierHolder
    {
        private static final IOModifier INSTANCE = new IOModifier();
    }
}

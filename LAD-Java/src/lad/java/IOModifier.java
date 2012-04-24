package lad.java;

import java.util.List;
import java.util.ListIterator;
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
    public String handle( MessageList pieces, int userid )
    {
        String output = "";
        if( pieces.contains( viewModifiersPiece ) )
        {
            ModifierManager mm = ModifierManager.getInstance();
            List< Modifier > modifiers =
                    mm.getModifiersByUserID( userid );

            // Output a fancy table if possible
            if( modifiers.size() > 0 )
            {
                ListIterator< Modifier > iter = modifiers.listIterator();
                int index = 1;

                output += "function postSortModifiers(){}";

                output += "java().append(";

                output += "makeSortableTable([";
                output += "'Type','Battles','Action'],";
                output += "[";
                while( iter.hasNext() )
                {
                    Modifier curr = iter.next();

                    output += "['" + curr.toString() +
                              "'," + curr.getBattles() + "," +
                              "'']";
                    index++;

                    if( iter.hasNext() )
                    {
                        output += ",";
                    }
                }
                output += "],'modifiers',postSortModifiers));";

            }
        }

        // And include the button to return to trainer
        output += IOInitial.getInstance().outputReturnToTrainerButton();

        MessageManager.getInstance().clearJava();
        return output;
    }

    private static class IOModifierHolder
    {
        private static final IOModifier INSTANCE = new IOModifier();
    }
}

package lad.game;

import java.util.List;
import java.util.ListIterator;
import lad.data.GameException;
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
     * Deleting a modifier for comparing to
     */
    private final static MessagePiece
            deleteModifierPiece = new MessagePiece( "deletemodifier" );

    /**
     * Handleable pieces.
     *
     * Piece: viewModifiersPiece
     *        deleteModifierPiece
     *
     * @return List with the above pieces
     */
    @Override
    public MessageList getPieces()
    {
        MessageList pieces = new MessageList();
        pieces.add( viewModifiersPiece );
        pieces.add( deleteModifierPiece );
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
            outputModifiers( userid );
        }
        else if( pieces.contains( deleteModifierPiece ) )
        {
            ModifierManager mm = ModifierManager.getInstance();
            int modID = Integer.valueOf( pieces.getValue( "deletemodifier" ) );
            Modifier mod = mm.getByID( modID );

            // Make sure user actually owns piece
            if( mod.getOwner() != userid )
            {
                throw new GameException( 2, "Modifier does not belong to " +
                                         "requestor (mod:" + modID + ",owner:" +
                                         mod.getOwner() + ")" );
            }

            // Don't allow modifiers in battle to be deleted
            if( mod.getEquipped() != null )
            {
                throw new GameException( 2, "Modifer is in battle." );
            }

            // User owns it, delete it
            mm.deleteModifier( mod );

            // Re-output new listing
            outputModifiers( userid );
        }
    }

    /**
     * Outputs the list of modifiers.
     *
     * @param userid ID of the user to get the modifiers for
     */
    protected void outputModifiers( int userid )
    {
        ModifierManager mm = ModifierManager.getInstance();
        List< Modifier > modifiers = mm.getByUserID( userid );

        // Output a fancy table if possible
        if( modifiers.size() > 0 )
        {
            ListIterator< Modifier > iter = modifiers.listIterator();
            int index = 1;

            write( "$.lad.modifiers.overview([" );
            while( iter.hasNext() )
            {
                Modifier curr = iter.next();
                String isEquipped = curr.getEquipped() == null ? "0" : "1";

                write( "[" + curr.getID() + "," +
                       "\"" + curr.toString() +
                       "\"," + curr.getBattles() + 
                       "," + isEquipped + "]" );
                index++;

                if( iter.hasNext() )
                {
                    write( "," );
                }
            }
            write( "]);" );
        }
    }

    private static class IOModifierHolder
    {
        private static final IOModifier INSTANCE = new IOModifier();
    }
}

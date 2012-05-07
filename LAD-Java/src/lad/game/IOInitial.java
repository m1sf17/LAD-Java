package lad.game;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import lad.data.GameException;
import lad.data.UserExp;
import lad.db.EXPManager;

/**
 * Handles initial connection with users to the java module.
 *
 * @author msflowers
 */
public class IOInitial extends MessageHandler
{
    /**
     * Calls super
     */
    private IOInitial()
    {
        super();
    }

    /**
     * Returns singleton
     *
     * @return Singleton.
     */
    public static IOInitial getInstance()
    {
        return IOInitialHolder.INSTANCE;
    }

    /**
     * Login piece for comparing to
     */
    private final static MessagePiece
            loginPiece = new MessagePiece( "login" );

    /**
     * Piece for viewing the EXP the user has for comparing to
     */
    private final static MessagePiece
            viewexpPiece = new MessagePiece( "viewuserexp" );

    /**
     * Piece for getting the JS
     */
    private final static MessagePiece
            getjsPiece = new MessagePiece( "getJS" );

    /**
     * Piece for getting the CSS
     */
    private final static MessagePiece
            getcssPiece = new MessagePiece( "getCSS" );

    /**
     * Handleable pieces.
     *
     * Piece: loginPiece
     * Piece: viewexpPiece
     * Piece: getjsPiece
     * Piece: getcssPiece
     *
     * @return List with all of the above pieces
     */
    @Override
    public MessageList getPieces()
    {
        MessageList pieces = new MessageList();
        pieces.add( loginPiece );
        pieces.add( viewexpPiece );
        pieces.add( getjsPiece );
        pieces.add( getcssPiece );
        return pieces;
    }

    /**
     * Handles pieces based on their pieces
     *
     * @throws InterruptedException Thrown if a sub function throws it
     */
    @Override
    public void handle( MessageList pieces, int userid )
                   throws InterruptedException
    {

        if( pieces.contains( loginPiece ) )
        {
            IOTrainer.getInstance().outputMainView( userid );
        }
        else if( pieces.contains( viewexpPiece ) )
        {
            // Simple output of all the EXP's
            List< UserExp > userexp = EXPManager.getExpByUserID( userid );
            ListIterator< UserExp > iter = userexp.listIterator();
            
            write( "$.lad.userexp.overview([" );

            while( iter.hasNext() )
            {
                UserExp curr = iter.next();
                write( "['" + curr.getTarget().toString() + "','" +
                       curr.getType().toString() + "'," + curr.getLevel() +
                       "," + curr.getExp() + ']');

                if( iter.hasNext() )
                {
                    write( "," );
                }
            }

            write( "],'userexp');" );
        }
        else if( pieces.contains( getjsPiece ) )
        {
            // Output the Javascript file
            try
            {
                URL file = ClassLoader.getSystemClassLoader().getResource( 
                  "lad/JS/game.js" );
                BufferedInputStream stream =
                        (BufferedInputStream)file.getContent();
                int avail = stream.available();
                byte buff[] = new byte[ avail ];
                stream.read( buff );
                String output = new String( buff );
                write( output );
            }
            catch( Exception e )
            {
                throw new GameException( 3, "Internal JS file not found." );
            }
            write( "createWindow('LAD');" +
                   "addMenuButton('LAD','ui-icon-home',function(){" +
                   "doAjax('java_run',{login:''});});" );
        }
        else if( pieces.contains( getcssPiece ) )
        {
            // TODO: Output the CSS file
        }
    }

    private static class IOInitialHolder
    {
        private static final IOInitial INSTANCE = new IOInitial();
    }
}

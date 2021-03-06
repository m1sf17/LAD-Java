package lad.game;

import java.util.List;
import java.util.ListIterator;
import lad.data.ModifierTarget;
import lad.data.TrainerBattleStats;
import lad.data.UserExp;
import lad.data.UserExpTarget;
import lad.data.Weapon;
import lad.db.EXPManager;

/**
 * Handles initial connection with users to the java module.
 *
 * TODO: Tests
 * TODO: Initialize JS/CSS on startup
 * TODO: Output errors on GameException
 * TODO: Balance weapons
 * TODO: (maybe) Organize tutorials into paged format
 * TODO: Add ability to turn off/on tutorial
 * TODO: Add losing/winning to arena battle status
 * TODO: Add strike through to used modifiers
 * TODO: Add percentages to user exp statistics
 * TODO: On same page updates, do not refresh screen
 * TODO: Custom naming of trainers
 * TODO: Internal naming of minions (based on ID)
 * TODO: Add notification for trainer battle completionrr
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
     * Piece for increasing the level of EXP for comparing to
     */
    private final static MessagePiece
            increaseexpPiece = new MessagePiece( "increaseexplevel" );

    /**
     * Piece for viewing the user's battle stats for comparing to
     */
    private final static MessagePiece
            viewuserstatsPiece = new MessagePiece( "viewuserstats" );

    /**
     * Piece for getting the JS
     */
    private final static MessagePiece getjsPiece = new MessagePiece( "getJS" );

    /**
     * Piece for getting the CSS
     */
    private final static MessagePiece
            getcssPiece = new MessagePiece( "getCSS" );

    /**
     * Piece for getting an image
     */
    private final static MessagePiece
            getimagePiece = new MessagePiece( "getimg" );

    /**
     * Cached version of the JS
     */
    private static String cachedJS = null;

    /**
     * Cached version of the CSS
     */
    private static String cachedCSS = null;

    /**
     * Handleable pieces.
     *
     * Piece: loginPiece
     * Piece: viewexpPiece
     * Piece: increaseexpPiece
     * Piece: getjsPiece
     * Piece: getcssPiece
     * Piece: viewuserstatsPiece
     * Piece: getimagePiece
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
        pieces.add( increaseexpPiece );
        pieces.add( viewuserstatsPiece );
        pieces.add( getimagePiece );
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
            write( "$.lad.window();" );
            IOTrainer.getInstance().outputMainView( userid );
        }
        else if( pieces.contains( viewexpPiece ) )
        {
            outputEXP( userid );
        }
        else if( pieces.contains( increaseexpPiece ) )
        {
            int levels =
                    Integer.valueOf( pieces.getValue( "increaseexplevel" ) );
            ModifierTarget type =
                    ModifierTarget.fromString( pieces.getValue( "type" ) );
            UserExpTarget target =
                    UserExpTarget.fromString( pieces.getValue( "target" ) );

            EXPManager.advanceUserEXP( userid, target, type, levels );

            outputEXP( userid );
        }
        else if( pieces.contains( viewuserstatsPiece ) )
        {
            TrainerBattleStats stats = EXPManager.getBattleStats( 1, userid );

            write( "$.lad.stats.view(" + stats.toJSString() + ",'User');" );
        }
        else if( pieces.contains( getjsPiece ) )
        {
            if( cachedJS == null )
            {
                String js = readPackagedFile( "lad/files/game.js" );
                StringBuffer buffer = new StringBuffer( 200 );

                // Weapon strings
                buffer.append( "return [ " );
                for( Weapon w : Weapon.values() )
                {
                    buffer.append( "\"" );
                    buffer.append( w.toString() );
                    buffer.append( "\"," );
                }
                buffer.deleteCharAt( buffer.length() - 1 );
                buffer.append( "];" );
                js = magicComment( "WEAPON STRING", js, buffer );

                // Weapon objects
                buffer.setLength( 0 );
                buffer.append( "return [ " );
                for( Weapon w : Weapon.values() )
                {
                    // Type
                    UserExpTarget gen = UserExpTarget.generalFromWeapon( w );
                    List< Double > atts = w.getAttributesList();
                    ListIterator< Double > iter = atts.listIterator();

                    buffer.append( "this.weapon( \"" );
                    buffer.append( w.toString() );
                    buffer.append( "\"," );

                    buffer.append( gen.getValue() );
                    buffer.append( "," );

                    while( iter.hasNext() )
                    {
                        buffer.append( String.format( "%.2f", iter.next() ) );

                        if( iter.hasNext() )
                        {
                            buffer.append( "," );
                        }
                    }

                    buffer.append( ")," );
                }
                buffer.deleteCharAt( buffer.length() - 1 );
                buffer.append( "];" );
                js = magicComment( "WEAPON OBJECTS", js, buffer );

                // Battle Statistic Types
               String stats = "return " +
                   TrainerBattleStats.getStatTypes().toJSString() + ";";

               js = magicComment( "STAT TYPES", js, stats );
                
                // Cache it
                cachedJS = js;
            }

            // Outputs the js
            write( cachedJS );
            
            // Also include this so that the view works
            write( "addMenuButton('LAD','ui-icon-home',function(){" +
                   "$.ladAjax({ 'login': '' });});" );
        }
        else if( pieces.contains( getimagePiece ) )
        {
            String img = pieces.getValue( "getimg" );
            write( readPackagedFile( "lad/files/" + img ) );
        }
        else if( pieces.contains( getcssPiece ) )
        {
            if( cachedCSS == null )
            {
                cachedCSS = readPackagedFile( "lad/files/game.css" );
            }
            write( cachedCSS );
        }
    }

    /**
     * Finds the location of a pair of matching magic comments.
     *
     * Searches the given string for a comment matching //# and the given
     * string.  The second string searched for is one matching //# END and the
     * given string.  The final parameter is replaced into the string. Returns
     * the resulting string.
     *
     * @param needle      String to search for
     * @param haystack    String to search in
     * @param replacement String to replace into
     * @return Resulting string
     */
    private String magicComment( CharSequence needle, String haystack,
                                 CharSequence replacement )
    {
        int startIndex = haystack.indexOf( "//# " + needle );
        String endString = "//# END " + needle;
        int endIndex = haystack.indexOf( endString ) + endString.length();
        CharSequence region = haystack.subSequence( startIndex, endIndex + 1 );

        return haystack.replace( region, replacement );
    }

    /**
     * Outputs the EXP block
     */
    private void outputEXP( int userid )
    {
        // Simple output of all the EXP's
        List< UserExp > userexp = EXPManager.getExpByUserID( userid );
        ListIterator< UserExp > iter = userexp.listIterator();

        write( "$.lad.userexp.overview([" );

        while( iter.hasNext() )
        {
            UserExp curr = iter.next();
            List< Integer > bonusLevels = curr.getBonusLevels();
            ListIterator< Integer > bonusIter = bonusLevels.listIterator();
            StringBuilder bonusLevelsStr = new StringBuilder( "[" );
            while( bonusIter.hasNext() )
            {
                bonusLevelsStr.append( bonusIter.next() );

                if( bonusIter.hasNext() )
                {
                    bonusLevelsStr.append( "," );
                }
            }
            bonusLevelsStr.append( "]" );

            write( "['" + curr.getTarget().toString() + "','" +
                    curr.getType().toString() + "'," + curr.getLevel() +
                    "," + curr.getExp() + ',' + bonusLevelsStr.toString() +
                    "]" );

            if( iter.hasNext() )
            {
                write( ",\n" );
            }
        }

        write( "],'userexp');" );
    }

    private static class IOInitialHolder
    {
        private static final IOInitial INSTANCE = new IOInitial();
    }
}

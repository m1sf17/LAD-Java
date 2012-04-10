package lad.java;

import java.util.LinkedList;
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
            viewalltrainersPiece = new MessagePiece( "viewalltrainers" );

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
     * View a specific trainer piece for comparing to
     */
    private final static MessagePiece
            viewtrainerPiece = new MessagePiece( "viewtrainer" );

    /**
     * Handleable pieces.
     *
     * Piece: @see loginPiece
     * Piece: @see viewalltrainersPiece
     * Piece: @see addtrainerPiece
     * Piece: @see trainminionPiece
     * Piece: @see addminionPiece
     * Piece: @see battleminionPiece
     * Piece: @see viewtrainerPiece
     *
     * @return List with all of the above pieces
     */
    @Override
    public MessageList getPieces()
    {
        MessageList pieces = new MessageList();
        pieces.add( loginPiece );
        pieces.add( viewalltrainersPiece );
        pieces.add( addtrainerPiece );
        pieces.add( trainminionPiece );
        pieces.add( addminionPiece );
        pieces.add( battleminionPiece );
        pieces.add( viewtrainerPiece );
        return pieces;
    }

    /**
     * Handles pieces based on their pieces
     *
     * @throws IndexOutOfBoundsException Thrown if a sub function throws it
     */
    @Override
    public String handle( MessageList pieces, int userid )
            throws IndexOutOfBoundsException
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
            pieces.contains( viewalltrainersPiece ) )
        {
            output += outputTrainerView( userid );
        }
        else if( pieces.contains( addtrainerPiece ) )
        {
            TrainerManager tm = TrainerManager.getInstance();
            // Validation: User has less than 8 trainers
            LinkedList< Trainer > trainers = tm.getTrainersByUser( userid );
            if( trainers.size() >= 8 )
            {
                // abort, user shouldn't be able to get above 8
                return "";
            }

            // Add the trainer and output default view
            tm.addTrainer( userid );
            output += outputTrainerView( userid );
        }
        else if( pieces.contains( viewtrainerPiece ) )
        {
            int trainer = Integer.valueOf( pieces.getValue( "viewtrainer" ) );

            output += outputMinionView( userid, trainer );
        }
        else if( pieces.contains( trainminionPiece ) )
        {
            Trainer trnr = TrainerManager.getInstance().getTrainerByID(
                    Integer.valueOf( pieces.getValue( "trainer" ) ) );
            int minionID = Integer.valueOf( pieces.getValue( "trainminion" ) );

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID does not match." );
            }

            ListIterator< Minion > iter = trnr.getMinions().listIterator();
            Minion target = null;
            while( iter.hasNext() )
            {
                Minion min = iter.next();
                if( min.getID() == minionID )
                {
                    target = min;
                    break;
                }
            }

            if( target == null )
            {
                throw new IndexOutOfBoundsException( "Minion not owned." );
            }

            target.adjustExp( 1 );
            output += outputMinionView( userid, trnr.getID() );
        }
        else if( pieces.contains( addminionPiece ) )
        {
            Trainer trnr = TrainerManager.getInstance().getTrainerByID(
                    Integer.valueOf( pieces.getValue( "trainer" ) ) );

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID does not match." );
            }

            if( trnr.getMinions().size() >=8 )
            {
                throw new IndexOutOfBoundsException( "8 minions max." );
            }

            Minion adder = Minion.create( trnr.getID() );
            trnr.addMinion( adder );

            output += outputMinionView( userid, trnr.getID() );
        }
        else if( pieces.contains( battleminionPiece ) )
        {
            Trainer trnr = TrainerManager.getInstance().getTrainerByID(
                    Integer.valueOf( pieces.getValue( "trainer" ) ) );
            int minion1ID = Integer.valueOf( pieces.getValue( "minion1" ) );
            int minion2ID = Integer.valueOf( pieces.getValue( "minion2" ) );

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID does not match." );
            }

            ListIterator< Minion > iter = trnr.getMinions().listIterator();
            Minion target1 = null, target2 = null;
            while( iter.hasNext() )
            {
                Minion min = iter.next();
                if( min.getID() == minion1ID )
                {
                    target1 = min;
                    continue;
                }
                if( min.getID() == minion2ID )
                {
                    target2 = min;
                    continue;
                }
            }

            if( target1 == null || target2 == null )
            {
                throw new IndexOutOfBoundsException( "Minion not owned." );
            }

            trnr.battle( target1, target2 );
            output += outputMinionView( userid, trnr.getID() );
        }
        return output;
    }

    /**
     * Output the default view for viewing trainers.
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

        System.out.println( "Trainers: " + trainers.size() );

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
                        "doJava( { viewtrainer: " + curr.getID() + "});" +
                        "}).appendTo( java() );";
        }

        if( trainers.size() < 8 )
        {
            output += "$('<button>Add Trainer</button>').button().click(" +
                        "function(){" +
                        "doJava( { addtrainer: '' });" +
                        "}).appendTo( java() );";
        }

        return output;
    }

    /**
     * Output the default view for viewing a specific trainer.
     *
     * @param userid ID of the requesting user
     * @param trainer ID of the trainer to view
     *
     * @throws IndexOutOfBoundsException Thrown when the trainer isn't found
     * @return A string with the resulting text
     */
    private String outputMinionView( int userid, int trainer )
            throws IndexOutOfBoundsException
    {
        String output = "";

        Trainer trnr = TrainerManager.getInstance().getTrainerByID( trainer );

        if( trnr.getOwner() != userid )
        {
            throw new IndexOutOfBoundsException( "User ID does not match." );
        }

        int level = trnr.getLevel();
        int exp = trnr.getExp();

        output += "java().append('";
        output += "Trainer #" + trainer;
        output += "<br>Level:" + level;
        output += "<br>Exp:" + exp;
        output += "<br><br>');";

        ListIterator< Minion > iter = trnr.getMinions().listIterator();
        int index = 1;

        String options = "";
        while( iter.hasNext() )
        {
            Minion minion = iter.next();
            output += "java().append('" +
                      "Minion #" + index + " " +
                      "Level: " + minion.getLevel() + " " +
                      "Exp: " + minion.getExp() + " ');";
            output += "$('<button>Train</button>').button().click(" +
                        "function(){" +
                        "doJava( { trainminion: " + minion.getID() + "," +
                                  "trainer:" + trainer + "});" +
                        "}).appendTo( java() );";

            options += "<option value=" + minion.getID() + ">" + index +
                       "</option>";
            index++;
        }

        if( index > 2 )
        {
            output += "java().append('<br><br>Battle: <select id=\"minion1\">" +
                      options + "</select> with <select id=\"minion2\">" +
                      options + "</select>');";
            output += "$('<button>Battle</button>').button().click(" +
                        "function(){" +
                        "doJava( { battleminions: ''," +
                                  "minion1: $('#minion1').val()," +
                                  "minion2: $('#minion2').val()});" +
                        "}).appendTo( java() );";
        }

        if( index < 8 )
        {
            output += "$('<button>Add Minion</button>').button().click(" +
                        "function(){" +
                        "doJava( { addminion: ''," +
                                  "trainer: '" + trainer + "'});" +
                        "}).appendTo( java() );";
        }

        return output;
    }

    private static class InitialIOHolder
    {
        private static final InitialIO INSTANCE = new InitialIO();
    }
}

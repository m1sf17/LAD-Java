package lad.game;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.data.Minion;
import lad.data.Trainer;
import lad.data.UserExp;
import lad.db.EXPManager;
import lad.db.ModifierManager;
import lad.db.TrainerManager;

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
     * Piece for viewing the EXP the user has for comparing to
     */
    private final static MessagePiece
            viewexpPiece = new MessagePiece( "viewuserexp" );

    /**
     * Piece for sending a trainer to battler for comparing to
     */
    private final static MessagePiece
            trainertoarenaPiece = new MessagePiece( "trainertoarena" );

    /**
     * Handleable pieces.
     *
     * Piece: loginPiece
     * Piece: viewalltrainersPiece
     * Piece: addtrainerPiece
     * Piece: trainminionPiece
     * Piece: addminionPiece
     * Piece: battleminionPiece
     * Piece: viewtrainerPiece
     * Piece: viewexpPiece
     * Piece: trainertoarenaPiece
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
        pieces.add( viewexpPiece );
        pieces.add( trainertoarenaPiece );
        return pieces;
    }

    /**
     * Handles pieces based on their pieces
     *
     * @throws IndexOutOfBoundsException Thrown if a sub function throws it
     */
    @Override
    public String handle( MessageList pieces, int userid )
            throws IndexOutOfBoundsException,
                   InterruptedException
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
            GameLoop.acquire();
            tm.addTrainer( userid );
            GameLoop.release();
            output += outputTrainerView( userid );
        }
        else if( pieces.contains( viewtrainerPiece ) )
        {
            int trainer = Integer.valueOf( pieces.getValue( "viewtrainer" ) );

            output += outputMinionView( userid, trainer );
        }
        else if( pieces.contains( trainminionPiece ) )
        {
            // Make sure the trainer belongs to the user
            Trainer trnr = TrainerManager.getInstance().getTrainerByID(
                    Integer.valueOf( pieces.getValue( "trainer" ) ) );
            int minionID = Integer.valueOf( pieces.getValue( "trainminion" ) );

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID mismatch." );
            }

            // Find the current minion data from the list
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

            // Make sure the minion belongs to the trainer
            if( target == null )
            {
                throw new IndexOutOfBoundsException( "Minion not owned." );
            }

            GameLoop.acquire();
            target.adjustExp( 1 );
            GameLoop.release();
            output += outputMinionView( userid, trnr.getID() );
        }
        else if( pieces.contains( addminionPiece ) )
        {
            // Make sure the trainer belongs to the user
            Trainer trnr = TrainerManager.getInstance().getTrainerByID(
                    Integer.valueOf( pieces.getValue( "trainer" ) ) );

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID mismatch." );
            }

            // Make sure the trainer doesn't already have 8 minions
            if( trnr.getMinions().size() >=8 )
            {
                throw new IndexOutOfBoundsException( "8 minions max." );
            }

            GameLoop.acquire();
            Minion adder = Minion.create( trnr.getID() );
            trnr.addMinion( adder );
            GameLoop.release();

            output += outputMinionView( userid, trnr.getID() );
        }
        else if( pieces.contains( battleminionPiece ) )
        {
            // Make sure the trainer belongs to the user
            Trainer trnr = TrainerManager.getInstance().getTrainerByID(
                    Integer.valueOf( pieces.getValue( "battleminion" ) ) );
            int minion1ID = Integer.valueOf( pieces.getValue( "minion1" ) );
            int minion2ID = Integer.valueOf( pieces.getValue( "minion2" ) );

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID mismatch." );
            }

            // Return a generic error if the minions match
            if( minion1ID == minion2ID )
            {
                return "genericErrorDialog('Error','Cannot battle a minion " +
                       "with itself.');";
            }

            // Find both of the minions
            ListIterator< Minion > iter = trnr.getMinions().listIterator();
            Minion target1 = null, target2 = null;
            while( iter.hasNext() )
            {
                Minion min = iter.next();
                if( min.getID() == minion1ID )
                {
                    target1 = min;
                }
                else if( min.getID() == minion2ID )
                {
                    target2 = min;
                }
            }

            // Make sure both minions belong to the trainer
            if( target1 == null || target2 == null )
            {
                throw new IndexOutOfBoundsException( "Minion not owned." );
            }

            // Battle them and grant a modifier
            GameLoop.acquire();
            Minion loser = trnr.battle( target1, target2 );
            int luck = loser.getLevel() + trnr.getLevel();
            ModifierManager.getInstance().addModifier( userid, luck );
            GameLoop.release();
            output += outputMinionView( userid, trnr.getID() );
        }
        else if( pieces.contains( viewexpPiece ) )
        {
            // Simple output of all the EXP's
            List< UserExp > userexp = EXPManager.getExpByUserID( userid );
            ListIterator< UserExp > iter = userexp.listIterator();

            output += "java().append(";

            while( iter.hasNext() )
            {
                UserExp curr = iter.next();
                output += "'" + curr.getTarget().toString() + ": " +
                          "Level: " + curr.getLevel() + " Exp: " +
                          curr.getExp() + "<br><br>'";

                if( iter.hasNext() )
                {
                    output += "+";
                }
            }

            output += ");" + outputReturnToTrainerButton();
        }
        else if( pieces.contains( trainertoarenaPiece ) )
        {
            // Make sure the trainer belongs to the user
            Trainer trnr = TrainerManager.getInstance().getTrainerByID(
                    Integer.valueOf( pieces.getValue( "trainertoarena" ) ) );

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID does not match." );
            }

            // TODO: Fill in GUI for sending trainer to arena
            // TODO: Add button for sending trainer to arena
        }
        
        // An error will instantly return.  It's safe to say all errors were
        // handled so clear the window before outputting more text.
        MessageManager.getInstance().clearJava();
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

        int index = 1;

        // Output each trainer
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

            index++;
        }

        // Add the "Add Trainer" button
        if( trainers.size() < 8 )
        {
            output += "$('<button>Add Trainer</button>').button().click(" +
                        "function(){" +
                        "doJava( { addtrainer: '' });" +
                        "}).appendTo( java() );";
        }

        // Add the modifiers button
        output += "java().append( '<br/><br/>' ).append(" +
                  "$('<button>Modifiers</button>').button().click(function(){" +
                    "doJava( { viewmodifiers: '' } );" +
                  "}));";

        // Add the User EXP button
        output += "java().append( " +
                  "$('<button>User EXP</button>').button().click(function(){" +
                    "doJava( { viewuserexp: '' } );" +
                  "}));";

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
        // Ensure the trainer belongs to the user
        String output = "";
        Trainer trnr = TrainerManager.getInstance().getTrainerByID( trainer );

        if( trnr.getOwner() != userid )
        {
            throw new IndexOutOfBoundsException( "User ID does not match." );
        }

        int level = trnr.getLevel();
        int exp = trnr.getExp();

        // Output the trainer profile
        output += "java().append('";
        output += "Trainer #" + trainer;
        output += "<br>Level:" + level;
        output += "<br>Exp:" + exp;
        output += "<br><br>');";

        List< Minion > minionList = trnr.getMinions();
        ListIterator< Minion > iter = minionList.listIterator();
        int index = 1;

        // Output each of the minions
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
            output += "java().append('<br/>');";

            options += "<option value=" + minion.getID() + ">" + index +
                       "</option>";
            index++;
        }

        // If there's at least 2 minions, let them be able to battle
        if( minionList.size() >= 2 )
        {
            output += "java().append('<br><br>Battle: <select id=\"minion1\">" +
                      options + "</select> with <select id=\"minion2\">" +
                      options + "</select>');";
            output += "$('<button>Battle</button>').button().click(" +
                        "function(){" +
                        "doJava( { battleminion: " + trainer + "," +
                                  "minion1: $('#minion1').val()," +
                                  "minion2: $('#minion2').val()});" +
                        "}).appendTo( java() );";
        }

        // If there is less than 8 minions, allow the trainer to get another
        if( minionList.size() < 8 )
        {
            output += "$('<button>Add Minion</button>').button().click(" +
                        "function(){" +
                        "doJava( { addminion: ''," +
                                  "trainer: '" + trainer + "'});" +
                        "}).appendTo( java() );";
        }

        return output + outputReturnToTrainerButton();
    }

    /**
     * Creates a button for returning to the trainer view.
     *
     * @return String for a button to return to trainer view.
     */
    public String outputReturnToTrainerButton()
    {
        return "java().append(" +
               "$('<button>Return to Trainers</button>').button()" +
               ".click(function(){" +
                 "doJava({ 'viewalltrainers' : ''});" +
               "}));";
    }

    private static class IOInitialHolder
    {
        private static final IOInitial INSTANCE = new IOInitial();
    }
}

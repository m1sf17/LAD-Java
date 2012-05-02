package lad.game;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.data.Minion;
import lad.data.Trainer;
import lad.data.UserExp;
import lad.data.Weapon;
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
     * Piece for having a trainer leave the arena queue
     */
    private final static MessagePiece
            trainerleavequeuePiece = new MessagePiece( "trainerleavequeue" );

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
     * Piece: trainerleavequeuePiece
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
        pieces.add( trainerleavequeuePiece );
        return pieces;
    }

    /**
     * Handles pieces based on their pieces
     *
     * @throws IndexOutOfBoundsException Thrown if a sub function throws it
     */
    @Override
    public void handle( MessageList pieces, int userid )
            throws IndexOutOfBoundsException,
                   InterruptedException
    {
        if( pieces.contains( loginPiece ) )
        {
            write( "function doJava( params ){" +
                     "doAjax( 'java_run', params );" +
                   "}" );
            write( "function java(){" +
                     "return getPopupContext('LAD');" +
                   "}" );
        }

        if( pieces.contains( loginPiece ) ||
            pieces.contains( viewalltrainersPiece ) )
        {
            outputMainView( userid );
        }
        else if( pieces.contains( addtrainerPiece ) )
        {
            TrainerManager tm = TrainerManager.getInstance();
            // Validation: User has less than 8 trainers
            LinkedList< Trainer > trainers = tm.getTrainersByUser( userid );
            if( trainers.size() >= 8 )
            {
                // abort, user shouldn't be able to get above 8
                writeReplace( "" );
            }

            // Add the trainer and output default view
            GameLoop.acquire();
            tm.addTrainer( userid );
            GameLoop.release();
            outputMainView( userid );
        }
        else if( pieces.contains( viewtrainerPiece ) )
        {
            int trainer = Integer.valueOf( pieces.getValue( "viewtrainer" ) );

            outputTrainerView( userid, trainer );
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
            outputTrainerView( userid, trnr.getID() );
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

            outputTrainerView( userid, trnr.getID() );
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
                writeReplace( "genericErrorDialog('Error'," +
                              "'Cannot battle a minion " +
                              "with itself.');" );
                return;
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
            outputTrainerView( userid, trnr.getID() );
        }
        else if( pieces.contains( viewexpPiece ) )
        {
            // Simple output of all the EXP's
            List< UserExp > userexp = EXPManager.getExpByUserID( userid );
            ListIterator< UserExp > iter = userexp.listIterator();
            
            write( "function postSortModifiers(){}" );
            write( "java().append(" );
            write( "makeSortableTable([" );
            write( "'Type','Target','Level','Exp']," );
            write( "[" );

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

            write( "],'userexp',postSortModifiers));" );
            outputReturnToMainButton();
        }
        else if( pieces.contains( trainertoarenaPiece ) )
        {
            // Make sure the trainer belongs to the user
            int trnrID = Integer.valueOf( pieces.getValue( "trainertoarena" ) );
            Trainer trnr = TrainerManager.getInstance().
                    getTrainerByID( trnrID );
            Weapon weapon = Weapon.values()[ Integer.valueOf(
                    pieces.getValue( "weapon" ))];

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID does not match." );
            }

            // Err...send the trainer to the queue
            GameLoop.queueTrainer( trnr, weapon );

            // Display the trainer's view
            outputTrainerView( userid, trnrID );
        }
        else if( pieces.contains( trainerleavequeuePiece ) )
        {
            // Make sure the trainer belongs to the user
            int trnrID = Integer.valueOf( pieces.getValue(
                    "trainerleavequeue" ) );
            Trainer trnr = TrainerManager.getInstance().
                    getTrainerByID( trnrID );

            if( trnr.getOwner() != userid )
            {
                throw new IndexOutOfBoundsException( "User ID does not match." );
            }

            // Check if the trainer is actually in a queue
            if( trnr.getBattleState() == Trainer.BattleState.InBattleQueue ||
                trnr.getBattleState() == Trainer.BattleState.LookingForBattle )
            {
                GameLoop.dequeueTrainer( trnr );
            }

            // Output the trainer again
            outputTrainerView( userid, trnrID );
        }
        
        // An error will instantly return.  It's safe to say all errors were
        // handled so clear the window before outputting more text.
        MessageManager.getInstance().clearJava();
    }

    /**
     * Output the default view for viewing trainers and other main options.
     *
     * @param userid ID of the requesting user
     */
    private void outputMainView( int userid )
    {
        LinkedList< Trainer > trainers =
            TrainerManager.getInstance().getTrainersByUser( userid );
        ListIterator< Trainer > iter = trainers.listIterator();

        int index = 1;

        // Output each trainer
        while( iter.hasNext() )
        {
            Trainer curr = iter.next();
            write( "java().append('" );
            write( "Trainer " + index + ": Level " );
            write( curr.getLevel() + " Exp:" + curr.getExp() );
            write( "<br>');" );
            write( "$('<button>View</button>').button().click(" +
                     "function(){" +
                     "doJava( { viewtrainer: " + curr.getID() + "});" +
                     "}).appendTo( java() );" );

            index++;
        }

        // Add the "Add Trainer" button
        if( trainers.size() < 8 )
        {
            write( "$('<button>Add Trainer</button>').button().click(" +
                     "function(){" +
                     "doJava( { addtrainer: '' });" +
                   "}).appendTo( java() );" );
        }

        // Add the modifiers button
        write( "java().append( '<br/><br/>' ).append(" +
               "$('<button>Modifiers</button>').button().click(function(){" +
                 "doJava( { viewmodifiers: '' } );" +
               "}));" );

        // Add the User EXP button
        write( "java().append( " +
               "$('<button>User EXP</button>').button().click(function(){" +
                 "doJava( { viewuserexp: '' } );" +
               "}));" );
    }

    /**
     * Output the default view for viewing a specific trainer.
     *
     * @param userid ID of the requesting user
     * @param trainer ID of the trainer to view
     *
     * @throws IndexOutOfBoundsException Thrown when the trainer isn't found
     */
    private void outputTrainerView( int userid, int trainer )
            throws IndexOutOfBoundsException
    {
        // Ensure the trainer belongs to the user
        Trainer trnr = TrainerManager.getInstance().getTrainerByID( trainer );

        if( trnr.getOwner() != userid )
        {
            throw new IndexOutOfBoundsException( "User ID does not match." );
        }

        int level = trnr.getLevel();
        int exp = trnr.getExp();
        Trainer.BattleState battleState = trnr.getBattleState();

        // Output the trainer profile
        write( "java().append('" );
        write( "Trainer #" + trainer );
        write( "<br>Level:" + level );
        write( "<br>Exp:" + exp );
        write( "<br>Battle State:" + battleState.toString() );
        if( battleState == Trainer.BattleState.InBattle )
        {
            write( "(" + GameLoop.getTimeLeftInTrainerBattle( trnr ) +
                   "s left)" );
        }
        write( "<br><br>');" );

        List< Minion > minionList = trnr.getMinions();
        ListIterator< Minion > iter = minionList.listIterator();
        int index = 1;

        // Output each of the minions
        String options = "";
        while( iter.hasNext() )
        {
            Minion minion = iter.next();
            write( "java().append('" +
                   "Minion #" + index + " " +
                   "Level: " + minion.getLevel() + " " +
                   "Exp: " + minion.getExp() + " ');" );
            write( "$('<button>Train</button>').button().click(" +
                     "function(){" +
                     "doJava( { trainminion: " + minion.getID() + "," +
                               "trainer:" + trainer + "});" +
                     "}).appendTo( java() );" );
            write( "java().append('<br/>');" );

            options += "<option value=" + minion.getID() + ">" + index +
                       "</option>";
            index++;
        }

        // If there's at least 2 minions, let them be able to battle
        if( minionList.size() >= 2 )
        {
            write( "java().append('<br><br>Battle: <select id=\"minion1\">" +
                   options + "</select> with <select id=\"minion2\">" +
                   options + "</select>');" );
            write( "$('<button>Battle</button>').button().click(" +
                     "function(){" +
                     "doJava( { battleminion: " + trainer + "," +
                               "minion1: $('#minion1').val()," +
                               "minion2: $('#minion2').val()});" +
                     "}).appendTo( java() );" );
        }

        // If there is less than 8 minions, allow the trainer to get another
        if( minionList.size() < 8 )
        {
            write( "$('<button>Add Minion</button>').button().click(" +
                     "function(){" +
                     "doJava( { addminion: ''," +
                               "trainer: '" + trainer + "'});" +
                   "}).appendTo( java() );" );
        }

        // If the trainer is not battling, allow it to battle
        if( trnr.getBattleState() == Trainer.BattleState.NoBattle )
        {
            write( "$('<button>Arena Battle</button>').button().click(" +
                     "function(){" +
                     "genericDialog('Weapon Selection','Select a weapon " +
                     "for your trainer to battle with.',{" );
            Weapon weapons[] = Weapon.values();
            for( int i = 0; i < weapons.length; i++ )
            {
                write( weapons[ i ].toString() + ":function(){" +
                         "doJava({'trainertoarena':" + trainer + "," +
                           "'weapon':" + i + "});" +
                         "$(this).dialog('close').remove();" +
                       "}" );
                if( i != weapons.length - 1 )
                {
                    write( "," );
                }
            }
            write( "});}).appendTo( java() );" );
        }
        
        // If the trainer is not battling, but in queue, let it leave
        if( trnr.getBattleState() == Trainer.BattleState.InBattleQueue ||
            trnr.getBattleState() == Trainer.BattleState.LookingForBattle )
        {
            write( "$('<button>Leave Arena</button>').button().click(" +
                     "function(){" +
                     "doJava({'trainerleavequeue':" + trainer + "});" +
                   "}).appendTo( java() );" );
        }

        outputReturnToMainButton();
    }

    /**
     * Outputs a button for returning to the trainer view.
     */
    public void outputReturnToMainButton()
    {
        write( "java().append(" +
               "$('<button>Return to Overview</button>').button()" +
               ".click(function(){" +
                 "doJava({ 'viewalltrainers' : ''});" +
               "}));" );
    }

    private static class IOInitialHolder
    {
        private static final IOInitial INSTANCE = new IOInitial();
    }
}

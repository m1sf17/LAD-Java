package lad.game;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.data.GameException;
import lad.data.Minion;
import lad.data.Trainer;
import lad.data.Weapon;
import lad.db.ModifierManager;
import lad.db.TrainerManager;

/**
 * Handles all transactions with the trainers
 *
 * @author msflowers
 */
public class IOTrainer extends MessageHandler
{
    /**
     * Calls super
     */
    private IOTrainer()
    {
        super();
    }

    /**
     * Returns singleton
     *
     * @return Singleton.
     */
    public static IOTrainer getInstance()
    {
        return IOTrainerHolder.INSTANCE;
    }

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
     * Piece: viewalltrainersPiece
     * Piece: addtrainerPiece
     * Piece: trainminionPiece
     * Piece: addminionPiece
     * Piece: battleminionPiece
     * Piece: viewtrainerPiece
     * Piece: trainertoarenaPiece
     * Piece: trainerleavequeuePiece
     *
     * @return List with all of the above pieces
     */
    @Override
    public MessageList getPieces()
    {
        MessageList pieces = new MessageList();
        pieces.add( viewalltrainersPiece );
        pieces.add( addtrainerPiece );
        pieces.add( trainminionPiece );
        pieces.add( addminionPiece );
        pieces.add( battleminionPiece );
        pieces.add( viewtrainerPiece );
        pieces.add( trainertoarenaPiece );
        pieces.add( trainerleavequeuePiece );
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

        if( pieces.contains( viewalltrainersPiece ) )
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
                return;
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
                throw new GameException( 2, "User ID mismatch." );
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
                throw new GameException( 1, "Minion not owned." );
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
                throw new GameException( 2, "User ID mismatch." );
            }

            // Make sure the trainer doesn't already have 8 minions
            if( trnr.getMinions().size() >= 8 )
            {
                throw new GameException( 1, "8 minions max." );
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
                throw new GameException( 2, "User ID mismatch." );
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
                throw new GameException( 1, "Minion not owned." );
            }

            // Make sure both minions are at least level 1
            if( target1.getLevel() < 1 || target2.getLevel() < 1 )
            {
                throw new GameException( 1, "Minion not level 1." );
            }

            // Battle them and grant a modifier
            GameLoop.acquire();
            Minion loser = trnr.battle( target1, target2 );
            int luck = loser.getLevel() + trnr.getLevel();
            ModifierManager.getInstance().addModifier( userid, luck );
            GameLoop.release();
            outputTrainerView( userid, trnr.getID() );
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
                throw new GameException( 2, "User ID does not match." );
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
                throw new GameException( 2, "User ID does not match." );
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
    }

    /**
     * Output the default view for viewing trainers and other main options.
     *
     * @param userid ID of the requesting user
     */
    protected void outputMainView( int userid )
    {
        LinkedList< Trainer > trainers =
            TrainerManager.getInstance().getTrainersByUser( userid );
        ListIterator< Trainer > iter = trainers.listIterator();

        // Output each trainer
        write( "$.lad.main.overview([" );
        while( iter.hasNext() )
        {
            Trainer curr = iter.next();
            write( "[" + curr.getID() + "," + curr.getLevel() + "," +
                   curr.getExp() +"]" );

            if( iter.hasNext() )
            {
                write( "," );
            }
        }
        write( "]);" );
    }

    /**
     * Output the default view for viewing a specific trainer.
     *
     * @param userid ID of the requesting user
     * @param trainer ID of the trainer to view
     */
    protected void outputTrainerView( int userid, int trainer )
    {
        // Ensure the trainer belongs to the user
        Trainer trnr = TrainerManager.getInstance().getTrainerByID( trainer );

        if( trnr.getOwner() != userid )
        {
            throw new GameException( 2, "User ID does not match." );
        }

        int level = trnr.getLevel();
        int exp = trnr.getExp();
        Trainer.BattleState battleState = trnr.getBattleState();
        String battleStateStr = battleState.toString();
        String stateNumber;

        // If the trainer is not battling, allow it to battle
        // 1 == Can Battle, 2 == Can Leave Battle, 0 == Neither
        if( battleState == Trainer.BattleState.InBattle )
        {
            battleStateStr +=
                "(" + GameLoop.getTimeLeftInTrainerBattle( trnr ) + "s left)";
            stateNumber = "0";
        }
        else if( trnr.getBattleState() == Trainer.BattleState.NoBattle )
        {
            stateNumber = "1";
        }
        else
        {
            stateNumber = "2";
        }

        // Output the trainer profile
        write( "$.lad.trainer.overview(" + trainer + "," + level + "," + exp +
               ",'" + battleStateStr + "'," );

        List< Minion > minionList = trnr.getMinions();
        ListIterator< Minion > iter = minionList.listIterator();
        int index = 1;

        // Output each of the minions
        write( "[" );
        while( iter.hasNext() )
        {
            Minion minion = iter.next();
            write( "[" + minion.getID() + "," + minion.getLevel() + "," +
                   minion.getExp() + "," + trainer + "]" );
            if( iter.hasNext() )
            {
                write( "," );
            }
            index++;
        }
        write( "]," + stateNumber + ");" );
    }

    private static class IOTrainerHolder
    {
        private static final IOTrainer INSTANCE = new IOTrainer();
    }
}

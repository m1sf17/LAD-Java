package lad.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import lad.data.Modifier;
import lad.data.ModifierTarget;
import lad.data.Trainer;
import lad.data.UserExp;
import lad.data.Weapon;
import lad.db.EXPManager;
import lad.db.ModifierManager;

/**
 * Class that manages a battle between trainers.
 *
 * @author msflowers
 */
public class TrainerBattle
{
    /**
     * The array of trainers which will be populated by the two trainers
     * battling.
     */
    private ArenaTrainer trainer[] = new ArenaTrainer[ 2 ];

    /**
     * The remaining number of ticks this battle has left.
     *
     * Once this reaches 0, the trainer with the least damage taken is the
     * victor.  Each tick is 1.0 seconds.
     */
    private int ticksRemaining = 300;

    /**
     * Winning index of the battle
     */
    private int winningIndex;

    /**
     * Distance between the two combatants.
     */
    private double distance;
    
    /**
     * Basic ctor
     * 
     * @param t1 First trainer that is battling
     * @param t2 Second trainer that is battling
     * @param w1 Weapon of the first trainer
     * @param w2 Weapon of the second trainer
     * @param m1 Modifiers for the first trainer
     * @param m2 Modifiers for the second trainer
     * @param b1 User bonuses for the first trainer
     * @param b2 User bonuses for the second trainer
     */
    public TrainerBattle( Trainer t1, Trainer t2, Weapon w1, Weapon w2,
                          List< Modifier > m1, List< Modifier > m2,
                          Map< ModifierTarget, Double > b1,
                          Map< ModifierTarget, Double > b2 )
    {
        trainer[ 0 ] = new ArenaTrainer( t1, w1, m1, b1 );
        trainer[ 1 ] = new ArenaTrainer( t2, w2, m2, b2 );

        distance = Math.max( trainer[ 0 ].getAttribute( ModifierTarget.Range ),
                           trainer[ 1 ].getAttribute( ModifierTarget.Range ) ) +
                           1.0f;

        t1.setBattleState( Trainer.BattleState.InBattle );
        t2.setBattleState( Trainer.BattleState.InBattle );
        Debug.log( w1.toString() + " VS " + w2.toString(), "BATTLEMATCHES" );
    }

    /**
     * Returns if this battle is finished.
     *
     * @return true if ticksRemaining is 0, false otherwise
     */
    public boolean isFinished()
    {
        return ticksRemaining == 0;
    }

    /**
     * Returns the winner of the battle.
     *
     * @return Winning trainer of the battle
     */
    public ArenaTrainer getWinner()
    {
        if( !isFinished() )
        {
            return null;
        }
        return trainer[ winningIndex ];
    }

    /**
     * Returns the loser of the battle.
     *
     * @return Losing trainer
     */
    public ArenaTrainer getLoser()
    {
        if( !isFinished() )
        {
            return null;
        }
        return trainer[ winningIndex == 1 ? 0 : 1 ];
    }

    /**
     * Returns the number of ticks remaining.
     *
     * @return ticksRemaining
     */
    public int getTicksRemaining()
    {
        return ticksRemaining;
    }

    /**
     * Returns whether a given trainer is/was a participant in this battle.
     *
     * @param trainer Trainer to check for
     * @return True if the trainer is/was, false otherwise
     */
    public boolean hasTrainer( Trainer trainer )
    {
        if( this.trainer[ 0 ].getTrainer() == trainer ||
            this.trainer[ 1 ].getTrainer() == trainer )
        {
            return true;
        }
        return false;
    }

    /**
     * Called to run the battle.
     *
     * @param ticks For each tick, the game is advanced by one second.
     */
    public void tick( int ticks )
    {
        for( int i = 0; i < ticks; i++ )
        {
            doTick();
        }
    }

    /**
     * Performs the actual battling sequence.
     */
    private void doTick()
    {
        // Update ticks
        if( this.ticksRemaining <= 0 )
        {
            return;
        }
        this.ticksRemaining--;
        int trainerlen = trainer.length;
        // Calculate next actions
        for( int i = 0; i < trainerlen; i++ )
        {
            ArenaTrainer thisTrnr = trainer[ i ];
            ArenaTrainer otherTrnr = trainer[ i == 0 ? 1 : 0 ];
            if( thisTrnr.isBroken() )
            {
                Debug.log( "Trainer " + i + "next action: cower." );
                thisTrnr.setNextAction( ArenaTrainer.NextAction.Broken );
            }
            if( thisTrnr.isRunning() &&
                otherTrnr.getAttribute( ModifierTarget.Range ) >= distance )
            {
                Debug.log( "Trainer " + i + " next action: run." );
                thisTrnr.setNextAction( ArenaTrainer.NextAction.RunAway );
            }
            else if( thisTrnr.getReloadTimeRemain() > 0.0 )
            {
                Debug.log( "Trainer " + i + " next action: reload." );
                thisTrnr.setNextAction( ArenaTrainer.NextAction.Reload );
            }
            else if( distance > thisTrnr.getAttribute( ModifierTarget.Range ) )
            {
                Debug.log( "Trainer " + i + " next action: close." );
                thisTrnr.setNextAction( ArenaTrainer.NextAction.CloseDistance );
            }
            else
            {
                Debug.log( "Trainer " + i + " next action: fire." );
                thisTrnr.setNextAction( ArenaTrainer.NextAction.FireWeapon );
            }
        }

        // Do actions
        for( int i = 0; i < trainerlen; i++ )
        {
            ArenaTrainer thisTrnr = trainer[ i ];
            ArenaTrainer otherTrnr = trainer[ i == 0 ? 1 : 0 ];

            // If running, then run!
            // Also, break out since all other actions will not be running
            if( thisTrnr.getNextAction() == ArenaTrainer.NextAction.RunAway )
            {
                distance += thisTrnr.runAway();
                thisTrnr.stopRunning();
                continue;
            }

            // If borken, then just cower
            if( thisTrnr.getNextAction() == ArenaTrainer.NextAction.Broken )
            {
                thisTrnr.cower();
                continue;
            }

            // Not running, so don't run
            thisTrnr.stopRunning();
            if( thisTrnr.getNextAction() ==
                ArenaTrainer.NextAction.CloseDistance )
            {
                distance -= thisTrnr.moveToEnemy();
                continue;
            }
            else if( thisTrnr.getNextAction() ==
                     ArenaTrainer.NextAction.Reload )
            {
                thisTrnr.reload();
                continue;
            }

            // Trainer is attacking!!!
            int shots = thisTrnr.fireWeapon();

            for( int j = 0; j < shots; j++ )
            {
                // If flexibility hits, shot misses
                if( otherTrnr.evade() )
                {
                    continue;
                }

                // Shot hit, calculate dmaage ( dmg * ( 1 - shielding ) )
                Double preShieldDamage = thisTrnr.getDamageOutput();
                Double postShieldDamage = otherTrnr.shield( preShieldDamage );
                Debug.log( "Trainer " + i + " hit for: " + postShieldDamage );

                // Inflict the damage
                otherTrnr.addTotalDamage( postShieldDamage, thisTrnr );
            }
        }

        // If the battle is over
        if( ticksRemaining == 0 )
        {
            winningIndex = trainer[ 0 ].getTotalDamage() >
                           trainer[ 1 ].getTotalDamage() ? 1 : 0;
        }
    }
    
    /**
     * Creates the map of bonuses for the user based on the user's exp
     * bonuses and the modifiers.
     * 
     * @param trainer Trainer to evaluate for
     * @param mods    Modifiers to evaluate for
     * @param weapon  Weapon the trainer will be using
     * @return Map of bonuses
     */
    private static Map< ModifierTarget, Double > generateBonuses(
            Trainer trainer, List< Modifier > mods, Weapon weapon )
    {
        int user = trainer.getOwner();
        Map< ModifierTarget, Double > ret = new HashMap<>( 10 );
        List< UserExp > exps = EXPManager.getExpByUserID( user );
        ListIterator< UserExp > expiter = exps.listIterator();
        ListIterator< Modifier > moditer = mods.listIterator();

        // Start by adding in the bonuses for the exp
        Double prof = 0.0;
        while( expiter.hasNext() )
        {
            UserExp current = expiter.next();
            if( current.getTarget().weaponAffected( weapon ) )
            {
                ModifierTarget target = current.getType();
                Double level = (double)current.getLevel() * 0.01;
                if( target == ModifierTarget.Proficiency )
                {
                    prof += level;
                }
                else
                {
                    Double currentBonus = ret.get( target );
                    if( currentBonus == null )
                    {
                        currentBonus = 0.0;
                    }
                    ret.put( target, level + currentBonus );
                }
            }
        }
        
        // Add in proficiency bonus to all targets
        if( prof > 0.0 )
        {
            Iterator< ModifierTarget > targetIter = ret.keySet().iterator();

            while( targetIter.hasNext() )
            {
                ModifierTarget target = targetIter.next();
                ret.put( target, ret.get( target ) + prof );
            }
        }

        // Then add in the bonuses for the mods
        while( moditer.hasNext() )
        {
            Modifier current = moditer.next();
            ModifierTarget target = current.getTarget();
            Double currentBonus = ret.get( target );
            if( currentBonus == null )
            {
                currentBonus = 0.0;
            }
            ret.put( target, 0.02 + currentBonus );
        }

        return ret;
    }

    /**
     * Creates the map of bonuses for the user based on the user's exp
     * bonuses and the modifiers.
     *
     * @param trainer Trainer to evaluate for
     * @return Map of bonuses
     */
    private static Map< ModifierTarget, Double > generateNPCBonuses(
            Trainer trainer )
    {
        int user = trainer.getOwner();
        Map< ModifierTarget, Double > ret = new HashMap<>( 10 );
        List< UserExp > exps = EXPManager.getExpByUserID( user );
        ListIterator< UserExp > expiter = exps.listIterator();

        // Start by adding in the bonuses for the exp
        int count = 0;
        while( expiter.hasNext() && count < 18 )
        {
            UserExp current = expiter.next();
            ModifierTarget target = ModifierTarget.getRandom( true );
            Double userLevel = (double)current.getLevel() * 0.01;
            Double currentBonus = ret.get( target );
            Double modifier = 0.8 + ( Math.random() * 0.4 );
            if( currentBonus == null )
            {
                currentBonus = 0.0;
            }
            ret.put( target, ( userLevel * modifier ) + currentBonus );

            count++;
        }

        return ret;
    }

    /**
     * Creates the list of modifiers for the user.
     *
     * @param trainer Trainer to evaluate for
     * @return List of modifiers
     */
    public static List< Modifier > generateModifiers( Trainer trainer )
    {
        int user = trainer.getOwner();
        List< Modifier > mods =
            ModifierManager.getInstance().getAvailableSetByUserID( user, 3 );
        ListIterator< Modifier > iter = mods.listIterator();
        while( iter.hasNext() )
        {
            iter.next().setEquipped( trainer );
        }

        return mods;
    }

    /**
     * Creates a new battle between two trainers.
     *
     * @param t1 First trainer to battle
     * @param t2 Second trainer to battle
     * @param w1 Weapon of the first trainer
     * @param w2 Weapon of the second trainer
     * @return Generated battle
     */
    public static TrainerBattle battle( Trainer t1, Trainer t2,
                                        Weapon w1, Weapon w2 )
    {
        List< Modifier > mod1 = generateModifiers( t1 );
        List< Modifier > mod2 = generateModifiers( t2 );
        Map< ModifierTarget, Double > bonus1 = generateBonuses( t1, mod1, w1 ),
                                      bonus2 = generateBonuses( t2, mod2, w2 );
        return new TrainerBattle( t1, t2, w1, w2, mod1, mod2, bonus1, bonus2 );
    }

    /**
     * Creates a new battle between a trainer and a similar NPC.
     *
     * @param trainer Trainer that is battling
     * @param weapon  Weapon that the trainer is wielding
     * @return Generated battle
     */
    public static TrainerBattle battleNPC( Trainer trainer, Weapon weapon )
    {
        // Variables
        Trainer npcTrainer = new Trainer( true );
        Weapon npcWeapon = Weapon.getRandom();
        List< Modifier > npcMods = new ArrayList<>( 0 ), userMods;
        Map< ModifierTarget, Double > npcBonus, userBonus;

        // Pull user info
        userMods = generateModifiers( trainer );
        userBonus = generateBonuses( trainer, userMods, weapon );

        // Obfuscate the user bonuses and copy them to the NPC's
        npcBonus = generateNPCBonuses( trainer );

        // Return the generated battle
        return new TrainerBattle( trainer, npcTrainer, weapon, npcWeapon,
                                  userMods, npcMods, userBonus, npcBonus );
    }
}

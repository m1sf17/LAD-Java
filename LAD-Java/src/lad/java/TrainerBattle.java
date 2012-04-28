package lad.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        Debug.log( w1.toString() + " VS " + w2.toString() );
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
            if( thisTrnr.running &&
                otherTrnr.getAttribute( ModifierTarget.Range ) >= distance )
            {
                Debug.log( "Trainer " + i + " next action: run." );
                thisTrnr.nextAction = ArenaTrainer.NextAction.RunAway;
            }
            else if( thisTrnr.reloadTimeRemain > 0.0 )
            {
                Debug.log( "Trainer " + i + " next action: reload." );
                thisTrnr.nextAction = ArenaTrainer.NextAction.Reload;
            }
            else if( distance > thisTrnr.getAttribute( ModifierTarget.Range ) )
            {
                Debug.log( "Trainer " + i + " next action: close." );
                thisTrnr.nextAction = ArenaTrainer.NextAction.CloseDistance;
            }
            else
            {
                Debug.log( "Trainer " + i + " next action: fire." );
                thisTrnr.nextAction = ArenaTrainer.NextAction.FireWeapon;
            }
        }

        // Update running
        for( int i = 0; i < trainerlen; i++ )
        {
            trainer[ i ].running =
                    trainer[ i ].nextAction == ArenaTrainer.NextAction.RunAway;
        }

        // Do actions
        for( int i = 0; i < trainerlen; i++ )
        {
            ArenaTrainer thisTrnr = trainer[ i ];
            ArenaTrainer otherTrnr = trainer[ i == 0 ? 1 : 0 ];

            // If running, then run!
            // Also, break out since all other actions will not be running
            if( thisTrnr.nextAction == ArenaTrainer.NextAction.RunAway )
            {
                distance += thisTrnr.getAttribute( ModifierTarget.Mobility );
                thisTrnr.running = true;
                continue;
            }

            // Not running, so don't run
            thisTrnr.running = false;
            if( thisTrnr.nextAction == ArenaTrainer.NextAction.CloseDistance )
            {
                distance -= thisTrnr.getAttribute( ModifierTarget.Mobility );
                continue;
            }
            else if( thisTrnr.nextAction == ArenaTrainer.NextAction.Reload )
            {
                thisTrnr.reloadTimeRemain -= 1.0;
                if( thisTrnr.reloadTimeRemain <= 0.0 )
                {
                    thisTrnr.timeToReload +=
                            thisTrnr.getAttribute( ModifierTarget.ReloadRate );
                }
                continue;
            }

            // Trainer is attacking!!!
            // Update reload times
            thisTrnr.timeToReload -= 1.0;
            if( thisTrnr.timeToReload < 0.0f )
            {
                thisTrnr.reloadTimeRemain = Weapon.getReloadTime();
            }

            // Because there are only so many shots per second, we use a
            // "left-over" attack speed.
            // Initial attack speed = user's attack speed
            Double atkSpd =
                    thisTrnr.getAttribute( ModifierTarget.AttackSpeed );
            // Time per shot = 1.0 / user atk speed
            Double timePerShot = 1.0 / atkSpd;
            // Total time to shoot = 1.0 + left over
            Double timeToShoot = 1.0 + thisTrnr.leftOverAtkSpd;
            // # of shots = Atk Speed * Total Time
            Double maxShots = Math.floor( atkSpd * timeToShoot );
            // Total time shot = Time per shot * shots
            Double totalTime = timePerShot * maxShots;
            // Next turn's left over = Total time to shoot - total
            //                         time shooting
            thisTrnr.leftOverAtkSpd = timeToShoot - totalTime;

            Integer shots = maxShots.intValue();

            Debug.log( "Trainer " + i + " shoots " + shots + " times." );
            for( int j = 0; j < shots; j++ )
            {
                // If accuracy misses or flexibility hits, shot misses
                if( Math.random() >
                    thisTrnr.getAttribute( ModifierTarget.Accuracy ) ||
                    Math.random() <
                    otherTrnr.getAttribute( ModifierTarget.Flexibility ) )
                {
                    continue;
                }

                // Shot hit, calculate dmaage ( dmg * ( 1 - shielding ) )
                Double damage =
                    thisTrnr.getAttribute( ModifierTarget.Damage );
                damage *= ( 1.0 -
                    otherTrnr.getAttribute( ModifierTarget.Shielding ) );

                // Critical will deal double damage
                if( Math.random() <
                    thisTrnr.getAttribute( ModifierTarget.Aim ) )
                {
                    damage *= 2.0;
                }


                Debug.log( "Trainer " + i + " hit for: " + damage );
                // Inflict the damage
                otherTrnr.totalDamage += damage;
                if( otherTrnr.totalDamage > (double)otherTrnr.timesRan *
                                            Weapon.getRunawayDamage() )
                {
                    // Morale broke, run away!
                    otherTrnr.running = true;
                    otherTrnr.timesRan++;
                }
            }
        }

        // If the battle is over
        if( ticksRemaining == 0 )
        {
            winningIndex = trainer[ 0 ].totalDamage >
                           trainer[ 1 ].totalDamage ? 1 : 0;
        }
    }
}

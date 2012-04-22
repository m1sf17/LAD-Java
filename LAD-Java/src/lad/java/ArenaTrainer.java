package lad.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension of trainer to support extra variables needed for battling in
 * the arena.
 *
 * @author msflowers
 */
public class ArenaTrainer
{
    /**
     * Trainer that is battling
     */
    private Trainer trainer;

    /**
     * Weapon that the trainer is wielding
     */
    private Weapon weapon;

    /**
     * Whether this trainer is currently running
     */
    public boolean running;

    /**
     * A count of how many times this trainer has ran away
     */
    public int timesRan;

    /**
     * The total damage that has been inflicted upon this trainer
     */
    public double totalDamage;

    /**
     * Amount of time remaining to reload
     */
    public double reloadTimeRemain;

    /**
     * Left over attack speed for attacking the next round.
     *
     * If this value is negative then something else cut it off.
     */
    public double leftOverAtkSpd;
    /**
     * Next action the trainer will take in the battle
     */
    public NextAction nextAction;

    /**
     * Time left before this unit has to reload
     */
    public double timeToReload;

    /**
     * Modifiers this trainer has equipped
     */
    private ArrayList< Modifier > modifiers;

    /**
     * All of the calculated attributes of the trainer
     */
    private HashMap< ModifierTarget, Double > attributes = new HashMap<>( 10 );

    /**
     * Basic ctor
     * @param t Trainer that is battling
     * @param w Weapon that the trainer is wielding
     */
    ArenaTrainer( Trainer trainer, Weapon weapon,
                  ArrayList< Modifier > modifiers,
                  Map< ModifierTarget, Double > userMult )
    {
        this.trainer = trainer;
        this.weapon = weapon;
        running = false;
        timesRan = 0;
        totalDamage = reloadTimeRemain = leftOverAtkSpd = 0.0;
        this.modifiers = new ArrayList<>( modifiers );

        for( int i = 1; i <= 9; i++ )
        {
            ModifierTarget target = ModifierTarget.fromInt( i );
            Double mult = userMult.get( target );

            Double value = 1.0;
            if( mult != null )
            {
                value = value.doubleValue() * mult;
            }

            attributes.put( target, value );
        }

        timeToReload = attributes.get( ModifierTarget.ReloadRate );
    }

    /**
     * @return the trainer
     */
    public Trainer getTrainer()
    {
        return trainer;
    }

    /**
     * @return modifiers
     */
    public List< Modifier > getModifiers()
    {
        return Collections.unmodifiableList( modifiers );
    }

    /**
     * @return attributes
     */
    public Map< ModifierTarget, Double > getAttributes()
    {
        return Collections.unmodifiableMap( attributes );
    }

    /**
     * @param target Attribute to look up the value for
     * @return Specific attribute
     */
    public Double getAttribute( ModifierTarget target )
    {
        return attributes.get( target );
    }

    /**
     * @return the weapon
     */
    public Weapon getWeapon()
    {
        return weapon;
    }

    /**
     * Possible actions the trainer can take during the coming tick
     */
    public enum NextAction
    {
        /**
         * Close the distance between the combatants (out of range)
         */
        CloseDistance,
        /**
         * Fire the weapon!
         */
        FireWeapon,
        /**
         * Reload the weapon
         */
        Reload,
        /**
         * After too much damage has been taken the unit is broken
         * and will run.
         */
        RunAway
    }
}

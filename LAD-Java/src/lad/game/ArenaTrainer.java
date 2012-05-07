package lad.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lad.data.Modifier;
import lad.data.ModifierTarget;
import lad.data.Trainer;
import lad.data.Weapon;

/**
 * Extension of trainer to support extra variables needed for battling in
 * the arena.
 *
 * Many of the basic elements of this class are public fields to make it easier
 * for the TrainerBattle class to make rapid changes to them.
 *
 * @see lad.java.TrainerBattle
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
    private boolean running = false;

    /**
     * A count of how many times this trainer has ran away
     */
    private int timesRan = 0;

    /**
     * The total damage that has been inflicted upon this trainer
     */
    private double totalDamage = 0.0;

    /**
     * Amount of time remaining to reload
     */
    private double reloadTimeRemain = 0.0;

    /**
     * Left over attack speed for attacking the next round.
     *
     * If this value is negative then something else cut it off.
     */
    private double leftOverAtkSpd = 0.0;

    /**
     * Next action the trainer will take in the battle
     */
    private NextAction nextAction;

    /**
     * Time left before this unit has to reload
     */
    private double timeToReload;

    /**
     * Modifiers this trainer has equipped
     */
    private List< Modifier > modifiers;

    /**
     * All of the calculated attributes of the trainer
     */
    private HashMap< ModifierTarget, Double > attributes = new HashMap<>( 10 );

    /**
     * Basic ctor
     *
     * @param t Trainer that is battling
     * @param w Weapon that the trainer is wielding
     */
    ArenaTrainer( Trainer trainer, Weapon weapon,
                  List< Modifier > modifiers,
                  Map< ModifierTarget, Double > userMult )
    {
        this.trainer = trainer;
        this.weapon = weapon;
        this.modifiers = new ArrayList<>( modifiers );

        for( int i = 1; i <= 9; i++ )
        {
            ModifierTarget target = ModifierTarget.fromInt( i );
            Double mult = userMult.get( target );

            Double value = weapon.getAttributeWithMult( target, mult );
            attributes.put( target, value );
        }

        timeToReload = attributes.get( ModifierTarget.ReloadRate );
    }

    /**
     * Returns the trainer object
     *
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
     * @return the running
     */
    public boolean isRunning()
    {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning( boolean running )
    {
        this.running = running;
    }

    /**
     * @return the timesRan
     */
    public int getTimesRan()
    {
        return timesRan;
    }

    /**
     * Increments the number of times ran
     */
    public void incrementTimesRan()
    {
        timesRan++;
    }

    /**
     * @return the totalDamage
     */
    public double getTotalDamage()
    {
        return totalDamage;
    }

    /**
     * Adds to the amount of total damage taken
     *
     * @param damage Amount to add to the total damage
     */
    public void addTotalDamage( double damage )
    {
        totalDamage += damage;
    }

    /**
     * @return the reloadTimeRemain
     */
    public double getReloadTimeRemain()
    {
        return reloadTimeRemain;
    }

    /**
     * Subtracts from the amount of reload time remaining
     *
     * @param time Amount to subtract from the time remaining
     */
    public void reduceReloadTimeRemain( double time )
    {
        reloadTimeRemain -= time;
    }

    /**
     * @param reloadTimeRemain the reloadTimeRemain to set
     */
    public void setReloadTimeRemain( double reloadTimeRemain )
    {
        this.reloadTimeRemain = reloadTimeRemain;
    }

    /**
     * @return the leftOverAtkSpd
     */
    public double getLeftOverAtkSpd()
    {
        return leftOverAtkSpd;
    }

    /**
     * @return the nextAction
     */
    public NextAction getNextAction()
    {
        return nextAction;
    }

    /**
     * @param nextAction the nextAction to set
     */
    public void setNextAction( NextAction nextAction )
    {
        this.nextAction = nextAction;
    }

    /**
     * @return the timeToReload
     */
    public double getTimeToReload()
    {
        return timeToReload;
    }

    /**
     * Subtracts the time to reload by the given amount
     *
     * @param amount Amount to subtract the time to reload by
     */
    public void reduceTimeToReload( double amount )
    {
        timeToReload -= amount;
    }

    /**
     * "Reload"s by increasing the time to reload by the reload time.
     */
    public void reload()
    {
        timeToReload += getAttribute( ModifierTarget.ReloadRate );
    }

    /**
     * @param leftOverAtkSpd the leftOverAtkSpd to set
     */
    public void setLeftOverAtkSpd( double leftOverAtkSpd )
    {
        this.leftOverAtkSpd = leftOverAtkSpd;
    }

    /**
     * Possible actions the trainer can take during the coming tick.
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

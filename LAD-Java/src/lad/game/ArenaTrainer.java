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
    
    // Fields that are stored in the DB
    /**
     * Number of times gun was fired
     */
    private int shotsFired;

    /**
     * Amount of damage dealt
     */
    private double damageDealt;

    /**
     * Amount of damage taken
     */
    private double damageTaken;

    /**
     * Number of times gun was reloaded
     */
    private int reloads;

    /**
     * Number of times gun shot hit the enemy
     */
    private int shotsHit;

    /**
     * Distance moved
     */
    private double distanceMoved;

    /**
     * Number of shots evaded to prevent damage
     */
    private int shotsEvaded;

    /**
     * Amount of damage reduced by shielding
     */
    private double damageReduced;

    /**
     * Number of critical hits landed
     */
    private int criticalsHit;

    /**
     * Number of times shot without being shot at
     */
    private int safelyShot;

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
        return damageTaken;
    }

    /**
     * Adds to the amount of total damage taken
     *
     * @param damage   Amount to add to the total damage
     * @param attacker Trainer that inflicted the damage
     */
    public void addTotalDamage( double damage, ArenaTrainer attacker )
    {
        this.damageTaken += damage;
        attacker.damageDealt += damage;

        if( getTotalDamage() >
            (double)getTimesRan() * Weapon.getRunawayDamage() )
        {
            setRunning( true );
            incrementTimesRan();
        }
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
        setReloadTimeRemain( this.reloadTimeRemain - 1.0 );
        if( this.reloadTimeRemain <= 0.0 )
        {
            timeToReload += getAttribute( ModifierTarget.ReloadRate );
            this.reloads++;
        }
    }

    /**
     * @param leftOverAtkSpd the leftOverAtkSpd to set
     */
    public void setLeftOverAtkSpd( double leftOverAtkSpd )
    {
        this.leftOverAtkSpd = leftOverAtkSpd;
    }

    /**
     * "Fire"s the weapon.
     *
     * @return Number of times the weapon hit the target
     */
    public int fireWeapon()
    {
        // Update reload times
        reduceTimeToReload( 1.0 );
        if( getTimeToReload() < 0.0f )
        {
            setReloadTimeRemain( Weapon.getReloadTime() );
        }

        // Because there are only so many shots per second, we use a
        // "left-over" attack speed.
        // Initial attack speed = user's attack speed
        Double atkSpd = getAttribute( ModifierTarget.AttackSpeed );
        // Time per shot = 1.0 / user atk speed
        Double timePerShot = 1.0 / atkSpd;
        // Total time to shoot = 1.0 + left over
        Double timeToShoot = 1.0 + getLeftOverAtkSpd();
        // # of shots = Atk Speed * Total Time
        Double maxShots = Math.floor( atkSpd * timeToShoot );
        // Total time shot = Time per shot * shots
        Double totalTime = timePerShot * maxShots;
        // Next turn's left over = Total time to shoot - total
        //                         time shooting
        setLeftOverAtkSpd( timeToShoot - totalTime );

        Integer shots = maxShots.intValue();
        int shotsHitTarget = 0;

        for( int j = 0; j < shots; j++ )
        {
            // If accuracy misses, shot misses
            if( Math.random() <= getAttribute( ModifierTarget.Accuracy ) )
            {
                shotsHitTarget++;
            }
        }

        // Update statistics
        this.shotsFired += shots;
        this.shotsHit += shotsHitTarget;

        return shotsHitTarget;
    }

    /**
     * Try to "Evade" a bullet
     *
     * @return True if the bullet is evaded, false otherwise
     */
    public boolean evade()
    {
        if( Math.random() < getAttribute( ModifierTarget.Flexibility ) )
        {
            this.shotsEvaded++;
            return true;
        }
        return false;
    }

    /**
     * "Shield"s a certain amount of the damage
     *
     * @param amount Amount of damage that hits the shield
     * @return Amount of damage that penetrates the shield
     */
    public double shield( double amount )
    {
        double postShield = amount *
                            ( 1.0 - getAttribute( ModifierTarget.Shielding ) );
        this.damageReduced += amount - postShield;

        return postShield;
    }

    /**
     * Gets the amount of damage a bullet that hit will deal.
     *
     * If a critical hit lands this will return double the damage.  Otherwise,
     * the damage attribute is simply returned.
     *
     * @return Damage to inflict on the opposing trainer
     */
    public double getDamageOutput()
    {
        Double damage = getAttribute( ModifierTarget.Damage );
        if( Math.random() < getAttribute( ModifierTarget.Aim ) )
        {
            damage *= 2;
            this.criticalsHit++;
        }

        return damage;
    }

    /**
     * "Move"s the trainer towards the enemy to get in range.
     *
     * @return Amount to move forward
     */
    public double moveToEnemy()
    {
        double movement = getAttribute( ModifierTarget.Mobility );
        this.distanceMoved += movement;
        return movement;
    }

    /**
     * Causes the trainer to run away from the enemy until out of the enemy's
     * range.
     *
     * @return Distance ran away
     */
    public double runAway()
    {
        setRunning( true );
        return getAttribute( ModifierTarget.Mobility );
    }
    
    /**
     * Gets a list of integers that deal with storable statistics.
     * 
     * Includes: Shots Fired, Reloads, Shots Hit, Shots Evaded, Criticals Hit,
     * Times Safely Shot, 1 (Battles), 0(Battles Won)
     * 
     * @return List of integers
     */
    public int[] getStorableIntStatistics()
    {
        return new int[]{
            this.shotsFired, this.reloads, this.shotsHit, this.shotsEvaded,
            this.criticalsHit, this.safelyShot, 1, 0
        };
    }

    /**
     * Gets a list of doubles that deal with storable statistics.
     *
     * Includes : Damage Dealt, Damage Taken, Distance Moved, Damage Reduced
     * 
     * @return List of doubles
     */
    public double[] getStorableDoubleStatistics()
    {
        return new double[]{
            this.damageDealt, this.damageTaken, this.distanceMoved,
            this.damageReduced
        };
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

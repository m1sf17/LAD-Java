package lad.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Data class for a weapon.
 *
 * @author msflowers
 */
public enum Weapon
{
    /**
     * Off hand ( High attack, low accuracy )
     */
    Generator ( 1, 3.0, 3.0, 2.0, 1.0, 2.0, 2.0, 3.0, 1.0, 2.0 ),
    /**
     * Off hand ( High accuracy, low reload )
     */
    Amplifier ( 2, 2.0, 2.0, 1.0, 3.0, 3.0, 2.0, 2.0, 3.0, 1.0 ),
    /**
     * Two hand ( High Defense, Low Attack Speed )
     */
    Launcher  ( 3, 1.0, 3.0, 1.0, 2.0, 2.0, 3.0, 3.0, 2.0, 2.0 ),
    /**
     * Two hand ( High Defense, Low Range )
     */
    Bombarder ( 4, 2.0, 2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 1.0, 1.0 ),
    /**
     * Two hand ( High Defense, Low Damage )
     */
    Lancer    ( 5, 3.0, 1.0, 2.0, 1.0, 2.0, 3.0, 2.0, 2.0, 3.0 ),
    /**
     * Main/Off ( High Accuracy, Low Defense )
     */
    Pistol    ( 6, 3.0, 2.0, 2.0, 3.0, 3.0, 1.0, 1.0, 2.0, 2.0 ),
    /**
     * Main/Off ( High Damage, Low Defense )
     */
    Propeller ( 7, 2.0, 3.0, 2.0, 3.0, 1.0, 1.0, 2.0, 2.0, 3.0 ),
    /**
     * Main Hand ( High Reload Rate, Low Damage )
     */
    Projector ( 8, 1.0, 1.0, 3.0, 2.0, 2.0, 3.0, 2.0, 3.0, 2.0 ),
    /**
     * Main Hand ( High Range, Low Defense )
     */
    Catapulter( 9, 2.0, 2.0, 3.0, 2.0, 1.0, 2.0, 1.0, 3.0, 3.0 );

    /**
     * Attack Speed Base/Multiplier
     */
    private final double atkSpdBase = 1.6, atkSpdMult = 0.4;

    /**
     * Damage Speed Base/Multiplier
     */
    private final double damageBase = 24, damageMult = 5.1;

    /**
     * Reload Rate Base/Multiplier
     */
    private final double reloadBase = 2.0, reloadMult = 2.4;

    /**
     * Accuracy Base/Multiplier
     */
    private final double accuracyBase = 0.31, accuracyMult = 0.1;

    /**
     * Mobility Base/Multiplier
     */
    private final double mobilityBase = 2.7, mobilityMult = 1.3;

    /**
     * Flexibility Base/Multiplier
     */
    private final double flexibilityBase = 0.1, flexibilityMult = 0.13;

    /**
     * Shielding Base/Multiplier
     */
    private final double shieldingBase = 0.1, shieldingMult = 0.1;

    /**
     * Aim Base/Multiplier
     */
    private final double aimBase = 0.03, aimMult = 0.18;

    /**
     * Range Base/Multiplier
     */
    private final double rangeBase = 10, rangeMult = 6.5;

    /**
     * Internal type
     */
    private int type;
    
    /**
     * Attack speed
     */
    private double atkSpd;
    
    /**
     * Damage
     */
    private double damage;
    
    /**
     * Reload rate
     */
    private double reloadRate;
    
    /**
     * Accuracy
     */
    private double accuracy;
    
    /**
     * Mobility
     */
    private double mobility;
    
    /**
     * Flexibility
     */
    private double flexibility;
    
    /**
     * Shielding
     */
    private double shielding;
    
    /**
     * Aim
     */
    private double aim;
    
    /**
     * Range
     */
    private double range;
    
    /**
     * Basic ctor
     * 
     * @param type Internal type of the weapon
     * @param atkSpd Internal attack speed of the weapon
     * @param damage Internal damage of the weapon
     * @param reloadRate Internal reload rate of the weapon
     * @param accuracy Internal accuracy of the weapon
     * @param mobility Internal mobility of the weapon
     * @param flexibility Internal flexibility of the weapon
     * @param shielding Internal shielding of the weapon
     * @param aim Internal aim of the weapon
     * @param range Internal range of the weapon
     */
    Weapon( int type, double atkSpd, double damage, double reloadRate,
            double accuracy, double mobility, double flexibility,
            double shielding, double aim, double range )
    {
        this.type = type;
        this.atkSpd = atkSpdBase + ( atkSpd * atkSpdMult );
        this.damage = damageBase + ( damage * damageMult );
        this.reloadRate = reloadBase + ( reloadRate * reloadMult );
        this.accuracy = accuracyBase + ( accuracy * accuracyMult );
        this.mobility = mobilityBase + ( mobility * mobilityMult );
        this.flexibility = flexibilityBase + ( flexibility * flexibilityMult );
        this.shielding = shieldingBase + ( shielding * shieldingMult );
        this.aim = aimBase + ( aim * aimMult );
        this.range = rangeBase + ( range * rangeMult );
    }

    /**
     * Returns the integer type
     *
     * @return type
     */
    public int getType()
    {
        return this.type;
    }

    /**
     * Returns the attack speed
     *
     * @return atkSpd
     */
    public double getAtkSpd()
    {
        return this.atkSpd;
    }

    /**
     * Returns the damage
     *
     * @return damage
     */
    public double getDamage()
    {
        return this.damage;
    }

    /**
     * Returns the amount of damage a trainer can take before it must run.
     *
     * @return 500.0
     */
    public static double getRunawayDamage()
    {
        return 500.0;
    }

    /**
     * Returns the reload rate
     *
     * @return reloadRate
     */
    public double getReloadRate()
    {
        return this.reloadRate;
    }

    /**
     * Returns the time a trainer must reload for
     *
     * @return 4.0
     */
    public static double getReloadTime()
    {
        return 4.0;
    }

    /**
     * Returns the accuracy
     *
     * @return accuracy
     */
    public double getAccuracy()
    {
        return this.accuracy;
    }

    /**
     * Returns the mobility
     *
     * @return mobility
     */
    public double getMobility()
    {
        return this.mobility;
    }
    /**
     * Returns the flexibility
     *
     * @return flexibility
     */
    public double getFlexibility()
    {
        return this.flexibility;
    }
    /**
     * Returns the shielding
     *
     * @return shielding
     */
    public double getShielding()
    {
        return this.shielding;
    }
    /**
     * Returns the aim
     *
     * @return aim
     */
    public double getAim()
    {
        return this.aim;
    }
    /**
     * Returns the range
     *
     * @return range
     */
    public double getRange()
    {
        return this.range;
    }

    /**
     * Returns a map of all the attributes
     *
     * @return map of attributes
     */
    public Map< ModifierTarget, Double > getAttributes()
    {
        // Create the map, populate it, return it
        Map< ModifierTarget, Double > ret = new HashMap<>( 9 );
        ret.put( ModifierTarget.Accuracy, getAccuracy() );
        ret.put( ModifierTarget.Aim, getAim() );
        ret.put( ModifierTarget.AttackSpeed, getAtkSpd() );
        ret.put( ModifierTarget.Damage, getDamage() );
        ret.put( ModifierTarget.Flexibility, getFlexibility() );
        ret.put( ModifierTarget.Mobility, getMobility() );
        ret.put( ModifierTarget.Range, getRange() );
        ret.put( ModifierTarget.ReloadRate, getReloadRate() );
        ret.put( ModifierTarget.Shielding, getShielding() );
        return ret;
    }

    /**
     * Looks up an attribute adding a multiplier in the process.
     *
     * @param target Attribute to look up
     * @param value  Multiplier to multiply in
     * @return Resulting value
     */
    public double getAttributeWithMult( ModifierTarget target, Double value )
    {
        // Validate the multiplier object
        double mult = value == null ? 0.0 : value;

        // Return the appropriate value with the multiplier added in
        if( target == ModifierTarget.Accuracy )
        {
            return getAccuracy() + ( accuracyMult * mult );
        }
        if( target == ModifierTarget.Aim )
        {
            return getAim() + ( aimMult * mult );
        }
        if( target == ModifierTarget.AttackSpeed )
        {
            return getAtkSpd() + ( atkSpdMult * mult );
        }
        if( target == ModifierTarget.Damage )
        {
            return getDamage() + ( damageMult * mult );
        }
        if( target == ModifierTarget.Flexibility )
        {
            return getFlexibility() + ( flexibilityMult * mult );
        }
        if( target == ModifierTarget.Mobility )
        {
            return getMobility() + ( mobilityMult * mult );
        }
        if( target == ModifierTarget.Range )
        {
            return getRange() + ( rangeMult * mult );
        }
        if( target == ModifierTarget.ReloadRate )
        {
            return getReloadRate() + ( reloadMult * mult );
        }
        if( target == ModifierTarget.Shielding )
        {
            return getShielding() + ( shieldingMult * mult );
        }
        throw new IndexOutOfBoundsException( "Invalid target: " + target );
    }

    /**
     * Gets a random weapon.
     *
     * @return Random weapon
     */
    public static Weapon getRandom()
    {
        Weapon values[] = Weapon.values();
        int index = (int)(Math.floor( Math.random() * values.length ));

        return values[ index ];
    }
}

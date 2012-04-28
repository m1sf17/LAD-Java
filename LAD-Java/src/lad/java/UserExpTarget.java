package lad.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enum of the targets for user EXP bonuses.
 *
 * @author msflowers
 */
public enum UserExpTarget
{
    /**
     * General target for off hand weapons
     */
    OffHand( 1, new Weapon[]{ Weapon.Amplifier, Weapon.Generator } ),
    /**
     * General target for two handed weapons
     */
    TwoHand( 2, new Weapon[]{ Weapon.Bombarder, Weapon.Lancer,
                                                Weapon.Launcher }),
    /**
     * General target for either hand weapons
     */
    EitherHand( 3, new Weapon[]{ Weapon.Pistol, Weapon.Propeller } ),
    /**
     * General target for main hand weapons
     */
    MainHand( 4, new Weapon[]{ Weapon.Projector, Weapon.Catapulter }),
    /**
     * Specific target for the amplifier
     */
    Amplifier( 5, new Weapon[]{ Weapon.Amplifier }),
    /**
     * Specific target for the generator
     */
    Generator( 6, new Weapon[]{ Weapon.Generator }),
    /**
     * Specific target for the bombarder
     */
    Bombarder( 7, new Weapon[]{ Weapon.Bombarder }),
    /**
     * Specific target for the lancer
     */
    Lancer( 8, new Weapon[]{ Weapon.Lancer }),
    /**
     * Specific target for the launcher
     */
    Launcher( 9, new Weapon[]{ Weapon.Launcher }),
    /**
     * Specific target for the pistol
     */
    Pistol( 10, new Weapon[]{ Weapon.Pistol }),
    /**
     * Specific target for the propeller
     */
    Propeller( 11, new Weapon[]{ Weapon.Propeller }),
    /**
     * Specific target for the projector
     */
    Projector( 12, new Weapon[]{ Weapon.Projector }),
    /**
     * Specific target for the catapulter
     */
    Catapulter( 13, new Weapon[]{ Weapon.Catapulter });

    /**
     * Numerical value of the target
     */
    private int value;

    /**
     * List of the weapons this will affect
     */
    private ArrayList< Weapon > weaponAffectors;

    /**
     * Basic ctor
     *
     * @param value Internal integer value of the target
     * @param targets Array of targets that this EXP affects
     */
    UserExpTarget( int value, Weapon[] targets )
    {
        this.value = value;
        this.weaponAffectors = new ArrayList<>( 3 );
        this.weaponAffectors.addAll( Arrays.asList( targets ) );
    }

    /**
     * @return the value
     */
    public int getValue()
    {
        return value;
    }

    /**
     * @return the weaponAffectors
     */
    public List< Weapon > getWeaponAffectors()
    {
        return Collections.unmodifiableList( weaponAffectors );
    }

    /**
     * Checks if a given weapon is affected by this target.
     *
     * @param weapon Weapon to check if this exp affects
     * @return True if the weapon is affected by this exp, false otherwise
     */
    public boolean weaponAffected( Weapon weapon )
    {
        return getWeaponAffectors().contains( weapon );
    }

    /**
     * Returns the appropriate target for a given integer
     *
     * @param val Value to lookup the target for
     * @return Appropriate target for the given integer
     */
    public static UserExpTarget fromInt( int val )
    {
        switch( val )
        {
            case 1:
                return OffHand;
            case 2:
                return TwoHand;
            case 3:
                return EitherHand;
            case 4:
                return MainHand;
            case 5:
                return Amplifier;
            case 6:
                return Generator;
            case 7:
                return Bombarder;
            case 8:
                return Lancer;
            case 9:
                return Launcher;
            case 10:
                return Pistol;
            case 11:
                return Propeller;
            case 12:
                return Projector;
            case 13:
                return Catapulter;
        }
        throw new IndexOutOfBoundsException( "Invalid target: " + val );
    }

    /**
     * Returns a string representation of the target
     *
     * @returns The target with an additional space in a couple of cases
     */
    @Override
    public String toString()
    {
        switch( this.value )
        {
            case 1:
                return "Off Hand";
            case 2:
                return "Two Hand";
            case 3:
                return "Either Hand";
            case 4:
                return "Main Hand";
            case 5:
                return "Amplifier";
            case 6:
                return "Generator";
            case 7:
                return "Bombarder";
            case 8:
                return "Lancer";
            case 9:
                return "Launcher";
            case 10:
                return "Pistol";
            case 11:
                return "Propeller";
            case 12:
                return "Projector";
            case 13:
                return "Catapulter";
        }
        throw new IndexOutOfBoundsException( "Invalid target: " + this.value );
    }

    /**
     * Returns the appropriate general experience for the given weapon
     *
     * @param weapon Weapon to lookup the target for
     * @return Appropriate general target
     */
    public static UserExpTarget generalFromWeapon( Weapon weapon )
    {
        if( weapon == Weapon.Amplifier )
        {
            return OffHand;
        }
        if( weapon == Weapon.Bombarder )
        {
            return TwoHand;
        }
        if( weapon == Weapon.Catapulter )
        {
            return MainHand;
        }
        if( weapon == Weapon.Generator )
        {
            return OffHand;
        }
        if( weapon == Weapon.Lancer )
        {
            return TwoHand;
        }
        if( weapon == Weapon.Launcher )
        {
            return TwoHand;
        }
        if( weapon == Weapon.Pistol )
        {
            return EitherHand;
        }
        if( weapon == Weapon.Projector )
        {
            return MainHand;
        }
        if( weapon == Weapon.Propeller )
        {
            return EitherHand;
        }
        throw new IndexOutOfBoundsException( "Invalid weapon: " + weapon );
    }

    /**
     * Returns the appropriate specific experience for the given weapon
     *
     * @param weapon Weapon to lookup the target for
     * @return Appropriate specific target
     */
    public static UserExpTarget specificFromWeapon( Weapon weapon )
    {
        if( weapon == Weapon.Amplifier )
        {
            return Amplifier;
        }
        if( weapon == Weapon.Bombarder )
        {
            return Bombarder;
        }
        if( weapon == Weapon.Catapulter )
        {
            return Catapulter;
        }
        if( weapon == Weapon.Generator )
        {
            return Generator;
        }
        if( weapon == Weapon.Lancer )
        {
            return Lancer;
        }
        if( weapon == Weapon.Launcher )
        {
            return Launcher;
        }
        if( weapon == Weapon.Pistol )
        {
            return Pistol;
        }
        if( weapon == Weapon.Projector )
        {
            return Projector;
        }
        if( weapon == Weapon.Propeller )
        {
            return Propeller;
        }
        throw new IndexOutOfBoundsException( "Invalid weapon: " + weapon );
    }
}

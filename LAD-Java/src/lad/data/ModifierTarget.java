package lad.data;

/**
 * Enumerator for a modifier.
 *
 * @author msflowers
 */
public enum ModifierTarget
{
    /**
     * Modifies proficiency attribute
     */
    Proficiency( 0 ),
    /**
     * Modifies attack speed attribute
     */
    AttackSpeed( 1 ),
    /**
     * Modifies damage attribute
     */
    Damage( 2 ),
    /**
     * Modifies reload rate attribute
     */
    ReloadRate( 3 ),
    /**
     * Modifies accuracy attribute
     */
    Accuracy( 4 ),
    /**
     * Modifies mobility attribute
     */
    Mobility( 5 ),
    /**
     * Modifies flexibility attribute
     */
    Flexibility( 6 ),
    /**
     * Modifies shielding attribute
     */
    Shielding( 7 ),
    /**
     * Modifies aim attribute
     */
    Aim( 8 ),
    /**
     * Modifies range attribute
     */
    Range( 9 );
    
    /**
     * Holds the actual target.
     */
    private int target;

    /**
     * Ctor from int
     *
     * @param targ The target ( in range -1 - 9 )
     * @throws GameException Thrown if the update fails
     */
    ModifierTarget( int targ )
    {
        target = targ;
    }

    /**
     * @return Value of the modifier target
     */
    public int getValue()
    {
        return target;
    }

    /**
     * Creates a ModifierTarget from an integer (0-9).
     *
     * @param target The desired target
     * @return The created target
     * @throws GameException Thrown if the parameter is out of range
     */
    public static ModifierTarget fromInt( int target )
    {
        switch( target )
        {
            case 0:
                return Proficiency;
            case 1:
                return AttackSpeed;
            case 2:
                return Damage;
            case 3:
                return ReloadRate;
            case 4:
                return Accuracy;
            case 5:
                return Mobility;
            case 6:
                return Flexibility;
            case 7:
                return Shielding;
            case 8:
                return Aim;
            case 9:
                return Range;
        }
        throw new GameException( 4, "Invalid target: " + target );
    }

    /**
     * String representation
     *
     * @return Name of the modifier, plus a space in two cases
     * @throws GameException Thrown if this is out of range
     */
    @Override
    public String toString()
    {
        switch( this )
        {
            case Proficiency:
                return "Proficiency";
            case AttackSpeed:
                return "Attack Speed";
            case Damage:
                return "Damage";
            case ReloadRate:
                return "Reload Rate";
            case Accuracy:
                return "Accuracy";
            case Mobility:
                return "Mobility";
            case Flexibility:
                return "Flexibility";
            case Shielding:
                return "Shielding";
            case Aim:
                return "Aim";
            case Range:
                return "Range";
        }

        throw new GameException( 4, "Invalid modifier to string." );
    }

    /**
     * Converts a string into a modifier target.
     *
     * @param str String to translate from
     * @return Corresponding modifier target
     * @throws GameException Thrown if an invalid string is given
     */
    public static ModifierTarget fromString( String str )
    {
        for( ModifierTarget possible : values() )
        {
            if( possible.toString().compareToIgnoreCase( str ) == 0 )
            {
                return possible;
            }
        }

        throw new GameException( 3, "Invalid modifier target from string: " +
                                 str );
    }

    /**
     * Returns the number of valid modifier targets.
     *
     * Valid targets are any target except for the InvalidTarget value.  Thus,
     * the returned value is the length of this enum minus one.
     *
     * @return Length of this enum - 1
     */
    public static int getLength()
    {
        return values().length - 1;
    }

    /**
     * Gets a random target.
     *
     * @param incProf Set to true to allow proficiency as a possible return
     * @return Random target
     * @throws GameException Thrown if the calculated random is out of range
     */
    public static ModifierTarget getRandom( boolean incProf )
    {
        if( !incProf )
        {
            int index = (int)(Math.floor( Math.random() * getLength() ));
            return fromInt( index );
        }

        int index = (int)(Math.floor( Math.random() * ( getLength() - 1 ) ));
        return fromInt( index + 1 );
    }

}

package lad.game;

/**
 * Holds a simple variable/value pair.
 * @author msflowers
 */
public class MessagePiece
{
    /**
     * The variable (key) for the message piece
     */
    private String var = "";
    /**
     * The value for the message piece
     */
    private String val = "";

    /**
     * Default ctor that leaves the variable/value as blank strings
     */
    public MessagePiece()
    {
    }

    /**
     * Quick ctor with a '*' value
     *
     * @param n_var Initial Variable
     */
    public MessagePiece( String n_var )
    {
        setVariable( n_var );
        setValue( "*" );
    }

    /**
     * ctor that sets both the variable and the value.
     *
     * @param n_var Initial variable
     * @param n_val Initial value
     */
    public MessagePiece( String n_var, String n_val )
    {
        setVariable( n_var );
        setValue( n_val );
    }

    /**
     * ctor from another message piece (copy ctor)
     *
     * @param other Piece to copy data from
     */
    public MessagePiece( MessagePiece other )
    {
        setVariable( other.var );
        setValue( other.val );
    }

    /**
     * Sets the variable
     *
     * @param n_var Sets var to this value
     */
    public final void setVariable( String n_var )
    {
        var = n_var;
    }

    /**
     * Sets the value
     *
     * @param n_val Sets val to this value
     */
    public final void setValue( String n_val )
    {
        val = n_val;
    }

    /**
     * Simply returns the variable
     *
     * @return var
     */
    public String getVariable( )
    {
        return var;
    }

    /**
     * Simply returns the value
     *
     * @return val
     */
    public String getValue()
    {
        return val;
    }

    /**
     * Overriden to provide a unique hashcode
     *
     * @return Composition of all the variables to generate a unique hashcode
     */
    @Override
    public int hashCode()
    {
        if( val.compareTo( "*" ) != 0 )
        {
            return var.hashCode() * val.hashCode();
        }
        return var.hashCode();
    }

    /**
     * Overriden to provide specific equals instructions. Will return true if
     * one of the following conditions are met:
     * A. this == other
     * B. other is a string that equals the variable
     * C. other is a message piece with the same variable and one of the pieces
     *    has '*' as their value
     * D. other is a message piece with the same variable/value
     *
     * @param o Object to compare this message piece against
     * @return True if the two objects are the same
     */
    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }

        if( !( o instanceof MessagePiece ) )
        {
            if( !( o instanceof String ) )
            {
                return false;
            }
            return o.toString().compareToIgnoreCase( var ) == 0;
        }

        MessagePiece other = (MessagePiece)o;
        if( val.compareTo( "*" ) == 0 ||
            other.val.compareTo( "*" ) == 0 )
        {
            return var.compareTo( other.var ) == 0;
        }

        return var.compareTo( other.var ) == 0 &&
               val.compareTo( other.val ) == 0;
    }
}

package lad.java;

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
     * Sets the variable
     *
     * @param n_var Sets @see var to this value
     */
    public final void setVariable( String n_var )
    {
        var = n_var;
    }

    /**
     * Sets the value
     *
     * @param n_val Sets @see val to this value
     */
    public final void setValue( String n_val )
    {
        val = n_val;
    }

    /**
     * Simply returns the variable
     *
     * @return @see var
     */
    public String getVariable( )
    {
        return var;
    }

    /**
     * Simply returns the value
     *
     * @return @see val
     */
    public String getValue( )
    {
        return val;
    }
}

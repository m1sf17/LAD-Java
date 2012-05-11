package lad.game;

/**
 * Utility class for a pair
 *
 * @param <E> First object class
 * @param <F> Second object class
 * @author msflowers
 */
public class Pair< E, F >
{
    /**
     * Internal value of the first object
     */
    private E e = null;
    /**
     * Internal value of the second object
     */
    private F f = null;
    
    /**
     * Basic constructor.
     * 
     * Sets both values to null
     */
    public Pair()
    {
    }

    /**
     * Constructor with parameters
     *
     * @param e Value of the first object
     * @param f Value of the second object
     */
    public Pair( E e, F f )
    {
        this.e = e;
        this.f = f;
    }

    /**
     * Copy constructor
     *
     * @param other Other pair to copy objects from
     */
    public Pair( Pair< E, F > other )
    {
        this.e = other.e;
        this.f = other.f;
    }

    /**
     * Returns the first object
     *
     * @return First object
     */
    public E get1()
    {
        return this.e;
    }

    /**
     * Returns the second object
     *
     * @return Second object
     */
    public F get2()
    {
        return this.f;
    }

    /**
     * Sets the first object
     *
     * @param e New value for the first object
     */
    public void set1( E e )
    {
        this.e = e;
    }

    /**
     * Sets the second object
     *
     * @param f New value for the second object
     */
    public void set2( F f )
    {
        this.f = f;
    }

    /**
     * Sets both objects for this pair
     *
     * @param e New value for the first object
     * @param f New value for the second object
     */
    public void set( E e, F f )
    {
        this.e = e;
        this.f = f;
    }

    /**
     * Returns a JS representation of this pair.
     *
     * Both objects have toString called on them to get their value.  If those
     * values have the ' character in them, it is replaced with \'.
     *
     * @return String like [ '1', '2' ]
     */
    public String toJSString()
    {
        StringBuilder builder = new StringBuilder( 20 );
        String first = get1().toString().replace( "\'", "\\\'" );
        String second = get2().toString().replace( "\'", "\\\'" );
        builder.append( "['" );
        builder.append( first );
        builder.append( "','" );
        builder.append( second );
        builder.append( "]" );
        return builder.toString();
    }
}

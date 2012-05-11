package lad.game;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import lad.data.GameException;

/**
 * Utility class for a list of pairs
 *
 * @param <E> First object class
 * @param <F> Second object class
 * @author msflowers
 */
public class PairList< E, F > extends ArrayList< Pair< E, F > >
{
    private static final long serialVersionUID = 3L;

    /**
     * Basic constructor
     */
    public PairList()
    {
        super();
    }

    /**
     * Constructor from two lists
     *
     * @param eList List of the first object class
     * @param fList list of the second object class
     */
    public PairList( List< E > eList, List< F > fList )
    {
        super();
        if( eList.size() != fList.size() )
        {
            throw new GameException( 4, "Pair list sizes do not match: e(" +
                                     eList.size() + "), f(" + fList.size() +
                                     ")" );
        }

        ListIterator< E > eIter = eList.listIterator();
        ListIterator< F > fIter = fList.listIterator();

        while( eIter.hasNext() )
        {
            this.add( new Pair<>( eIter.next(), fIter.next() ) );
        }
    }

    /**
     * Constructor from two arrays
     *
     * @param eArray Array of the first object class
     * @param fArray Array of the second object class
     */
    public PairList( E[] eArray, F[] fArray )
    {
        super();

        if( eArray.length != fArray.length )
        {
            throw new GameException( 4, "Pair list sizes do not match: e(" +
                                     eArray.length + "), f(" + fArray.length +
                                     ")" );
        }

        for( int i = 0; i < eArray.length; i++ )
        {
            this.add( new Pair<>( eArray[ i ], fArray[ i ] ) );
        }
    }

    /**
     * Copy constructor
     *
     * @param other List to copy from
     */
    public PairList( PairList< E, F > other )
    {
        super( other );
    }

    /**
     * Get a list of all the first object class
     *
     * @return 1-D list of the first object class
     */
    public List< E > get1()
    {
        List< E > ret = new ArrayList<>( this.size() );
        ListIterator< Pair< E, F > > iter = this.listIterator();

        while( iter.hasNext() )
        {
            ret.add( iter.next().get1() );
        }

        return ret;
    }

    /**
     * Get a list of all the second object class
     *
     * @return 1-D list of the second object class
     */
    public List< F > get2()
    {
        List< F > ret = new ArrayList<>( this.size() );
        ListIterator< Pair< E, F > > iter = this.listIterator();

        while( iter.hasNext() )
        {
            ret.add( iter.next().get2() );
        }

        return ret;
    }

    /**
     * Returns a JS representation of this pair list
     *
     * @return String like [ [ '1', '2' ], [ '1', '2' ] ]
     */
    public String toJSString()
    {
        StringBuilder builder = new StringBuilder( 1000 );
        ListIterator< Pair< E, F > > iter = this.listIterator();

        builder.append( "[" );

        while( iter.hasNext() )
        {
            builder.append( iter.next().toJSString() );

            if( iter.hasNext() )
            {
                builder.append( "," );
            }
        }

        builder.append( "]" );
        return builder.toString();
    }
}

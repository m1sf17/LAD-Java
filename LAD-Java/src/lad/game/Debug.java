package lad.game;

import java.util.ArrayList;

/**
 * Provides debugging capabilities with areas that can be toggled.
 *
 * @author msflowers
 */
public class Debug
{
    /**
     * Array of logs that are enabled.
     */
    private static ArrayList< String > enabledDebugs = new ArrayList<>( 20 );

    /**
     * Attempts to log based on the class name.
     *
     * Uses an exception to lookup the name of the calling class.  That class
     * name is passed on to the other function.
     *
     * @param msg Message to print if the log is enabled
     */
    public static void log( String msg )
    {
        Exception exception = new Exception( "E" );
        exception.fillInStackTrace();

        StackTraceElement elem[] = exception.getStackTrace();

        log( msg, elem[ 1 ].getClassName() );
    }

    /**
     * Attempts to log based on the log name.
     *
     * If the region is in the debug array then the message is printed.  If it
     * is not then this function return silently.
     *
     * @param msg Message to attempt to print
     * @param region Region that is required to be enabled to print
     */
    public static void log( String msg, String region )
    {
        if( enabledDebugs.contains( region ) )
        {
            System.out.println( region.toUpperCase() + ": " + msg );
        }
    }

    /**
     * Enables a region so that debug messages are printed from it.
     *
     * @param region Region to enable
     */
    public static void enableLog( String region )
    {
        enabledDebugs.remove( region );
        enabledDebugs.add( region );
    }

    /**
     * Disables a region so that future debug messages are no longer printed
     * from it.
     *
     * @param region Region to disable
     */
    public static void disableLog( String region )
    {
        enabledDebugs.remove( region );
    }
}

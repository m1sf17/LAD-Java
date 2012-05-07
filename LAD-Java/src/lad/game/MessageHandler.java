package lad.game;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import lad.data.GameException;

/**
 * Interface for handling messages with the PHP server.
 *
 * To implement, return a list of handleable messages with getPieces,
 * implement the handle to handle any of the given message, and finally call
 * super from the constructor.
 *
 * @author msflowers
 */
public abstract class MessageHandler
{
    /**
     * Simply writes a string to the buffer in the manager.
     *
     * @param str String to write
     */
    protected void write( String str )
    {
        MessageManager.getInstance().write( str );
    }

    /**
     * Overwrites the string in the manager.
     *
     * @param str String to write
     */
    protected void writeReplace( String str )
    {
        MessageManager.getInstance().writeReplace( str );
    }

    /**
     * Read a packaged file and output it.
     *
     * Reads the given location inside this JAR package and outputs its contents
     * to the stream.
     *
     * @param location Location the file is inside the JAR package
     * @throws GameException Thrown if an error occurs while reading the file
     */
    protected void writePackagedFile( String location )
    {
        try
        {
            // Find the file and get its stream
            URL file = ClassLoader.getSystemClassLoader().
                    getResource( location );
            BufferedInputStream stream = (BufferedInputStream)file.getContent();

            // Get the number of bytes available
            int avail = stream.available();

            // Create/fill the buffer
            byte buff[] = new byte[ avail ];
            stream.read( buff );

            // Write out the buffer
            String output = new String( buff );
            write( output );
        }
        catch( IOException e )
        {
            throw new GameException( 3, "Internal file not parsing:" +
                                        e.getMessage() );
        }
    }
    
    /**
     * Implemented by the subclass to return handleable pieces.
     * 
     * Set the value to '*' to handle the key but not require the value to be
     * set.
     * 
     * @return List of handleable pieces
     */
    public abstract MessageList getPieces();

    /**
     * Called by the manager to call the handler.
     *
     * Placeholder in case handler needs specific pre/post processing later on.
     *
     * @param pieces List of pieces sent by the user.
     * @param userid ID of the user issuing the request.
     * @throws InterruptedException Possibly thrown if a data accessor gets
     *                              interrupted while trying to acquire the lock
     *                              on the game loop's data.
     */
    public void doHandle( MessageList pieces, int userid )
            throws InterruptedException
    {
        handle( pieces, userid );
    }

    /**
     * Implemented by the subclass to handle the message pieces.
     *
     * @param pieces List of pieces sent by the user.
     * @param userid ID of the user issuing the request.
     * @throws InterruptedException Possibly thrown if a data accessor gets
     *                              interrupted while trying to acquire the lock
     *                              on the game loop's data.
     */
    public abstract void handle( MessageList pieces, int userid )
            throws InterruptedException;

    /**
     * Ctor.
     *
     * Simply sets the instance.
     *
     * @see lad.game.MessageHandler#setInstance()
     */
    protected MessageHandler()
    {
        setInstance();
    }

    /**
     * Simply returns the singleton.
     *
     * @return Singleton
     */
    public static MessageHandler getInstance()
    {
        return MessageHandlerHolder.INSTANCE;
    }

    /**
     * Sets the singleton.
     *
     * Called during the constructor to set the constructor.  Also adds itself
     * to the manager's list.
     *
     * @see lad.game.MessageManager#addHandler(lad.game.MessageList, lad.game.MessageHandler)
     */
    private void setInstance( )
    {
        MessageHandlerHolder.INSTANCE = this;
        MessageManager.addHandler( getPieces(), this );
    }

    private static class MessageHandlerHolder
    {
        private static MessageHandler INSTANCE = null;
    }
}

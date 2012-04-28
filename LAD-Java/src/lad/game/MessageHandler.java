package lad.game;

/**
 * Interface for handling messages with the PHP server.
 *
 * To implement, return a list of handleable messages with @see getPieces,
 * implement @see handle to handle any of the given message, and finally call
 * super from the constructor.
 *
 * @author msflowers
 */
public abstract class MessageHandler
{

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
     * @return Output to be sent to the user
     */
    public String doHandle( MessageList pieces, int userid )
    {
        return handle( pieces, userid );
    }

    /**
     * Implemented by the subclass to handle the message pieces.
     *
     * @param pieces List of pieces sent by the user.
     * @param userid ID of the user issuing the request.
     * @return Output to be sent to the user
     */
    public abstract String handle( MessageList pieces, int userid );

    /**
     * Calls @see setInstance
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
     * Called during the constructor to set the constructor.  Also calls
     * @see MessageHandler.addHandler.
     */
    private void setInstance( )
    {
        MessageHandlerHolder.INSTANCE = this;
        MessageManager.getInstance().addHandler( getPieces(), this );
    }

    private static class MessageHandlerHolder
    {
        private static MessageHandler INSTANCE = null;
    }
}

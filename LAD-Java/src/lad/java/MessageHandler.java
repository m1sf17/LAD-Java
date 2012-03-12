/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lad.java;

import java.util.List;

/**
 *
 * @author msflowers
 */
public abstract class MessageHandler
{
    protected MessageHandler()
    {
        setInstance();
    }

    public static MessageHandler getInstance()
    {
        return MessageHandlerHolder.INSTANCE;
    }

    private void setInstance( )
    {
        MessageHandlerHolder.INSTANCE = this;
        MessageManager.getInstance().addHandler( getPieces(), this );
    }

    public abstract List< MessagePiece > getPieces();
    public abstract String handle( List< MessagePiece > pieces );

    private static class MessageHandlerHolder
    {

        private static MessageHandler INSTANCE = null;

    }

}

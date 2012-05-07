package lad.data;

/**
 * Thrown to indicate something went wrong with the game logic.
 *
 * Game exceptions have three levels of severity.  The first level(Access) means
 * the user is trying to access resources that they do not own.  The second
 * level(Existence) means the user is trying to access resources that do not
 * exist.  The third level(Critical) denotes a severe internal error that
 * typically means something has failed to update.  The final level(Enum)
 * is restricted to enumeration failures and typically denotes a typo.
 *
 * @author msflowers
 */
public class GameException extends RuntimeException
{
    private static final long serialVersionUID = 2030111L;
    
    /**
     * Basic constructor taking the severity and the reason of the exception
     * as parameters
     * 
     * @param severity 1-4 Value of Severity
     * @param reason   Reason why the exception is being thrown
     */
    public GameException( int severity, String reason )
    {
        super( ( severity == 1 ? "EXISTENCE ERROR: " :
                 severity == 2 ? "ACCESS ERROR: " :
                 severity == 3 ? "CRITICAL ERROR: " :
                                 "ENUM ERROR: ") + reason );
    }
}
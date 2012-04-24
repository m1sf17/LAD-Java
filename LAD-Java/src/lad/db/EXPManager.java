package lad.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lad.java.ModifierTarget;
import lad.java.UserExp;
import lad.java.UserExpTarget;

/**
 * Manages all of the User EXP
 *
 * @author msflowers
 */
public class EXPManager extends DBManager
{
    /**
     * Internal list of exp
     */
    private LinkedList< UserExp > exps = new LinkedList<>();

    /**
     * Private ctor
     */
    private EXPManager()
    {
    }

    /**
     * Returns the table profile to load.
     *
     * The table is different from most in that it does not have an ID but
     * uses a 3-part key.  The three parts consist of the owner (user), target
     * (weapon) and the type (stat).
     *
     * @return A list with only the EXP table to load
     */
    @Override
    public TableProfile[] profiles()
    {
        return new TableProfile[]{
            new TableProfile(){
                @Override
                public String tableName()
                {
                    return "USEREXP";
                }
                @Override
                public String createString()
                {
                    return
                        "CREATE TABLE `USEREXP` (" +
                        "`owner` int(10) unsigned NOT NULL," +
                        "`target` int(10) unsigned NOT NULL," +
                        "`type` int(10) unsigned NOT NULL," +
                        "`level` int(10) unsigned NOT NULL," +
                        "`exp` int(10) unsigned NOT NULL," +
                        "PRIMARY KEY (`owner`,`target`,`type`)" +
                        ") ENGINE = MyISAM DEFAULT CHARSET=latin1";
                }
                @Override
                public String[] tableHeaders()
                {
                    return new String[]{ "owner", "target", "type",
                                         "level", "exp" };
                }
                @Override
                public void loadRow( ResultSet rs ) throws SQLException
                {
                    int owner = rs.getInt( 1 );
                    int target = rs.getInt( 2 );
                    int type = rs.getInt( 3 );
                    int level = rs.getInt( 4 );
                    int exp = rs.getInt( 5 );

                    UserExp userexp = new UserExp( owner, target, type,
                                                   level, exp );
                    exps.add( userexp );
                }
                @Override
                public void postinit() throws SQLException
                {
                    UserExp.prepareStatements();
                }
                @Override
                public boolean loadData()
                {
                    return true;
                }
            }
        };
    }

    /**
     * Gets all exp for the specified owner.
     *
     * If no modifiers are found the list returned will simply be empty.
     *
     * @param owner Owner to get exp for
     * @return List of found exp
     */
    public List< UserExp > getModifiersByUserID( int owner )
    {
        LinkedList< UserExp > ret = new LinkedList<>();
        ListIterator< UserExp > iter = exps.listIterator();

        while( iter.hasNext() )
        {
            UserExp current = iter.next();

            if( current.getOwner() == owner )
            {
                ret.add( current );
            }
        }

        return ret;
    }

    /**
     * Grants a user a certain type of EXP.
     *
     * @param user User to get the EXP
     * @param type Type of EXP the user is getting
     * @param target Target of the EXP the user is getting
     * @param amount Amount of EXP the user is getting
     */
    public static void grantUserEXP( int user, UserExpTarget target,
                                     ModifierTarget type, int amount )
    {
        ListIterator< UserExp > iter = getInstance().exps.listIterator();

        // Check if exp already exists and only needs to be added to
        while( iter.hasNext() )
        {
            UserExp exp = iter.next();
            if( exp.getOwner() == user && exp.getTarget() == target &&
                exp.getType() == type )
            {
                // TODO: Complete
            }
        }
    }

    /**
     * Returns the singleton
     *
     * @return singleton
     */
    public static EXPManager getInstance()
    {
        return EXPManagerHolder.INSTANCE;
    }

    private static class EXPManagerHolder
    {
        private static final EXPManager INSTANCE = new EXPManager();
    }
}

package lad.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for a profile of a MySQL table.  Useful to have quick access to
 * creating managers.
 *
 * @author msflowers
 */
public interface TableProfile
{
    /**
     * Implement to get the table name
     *
     * @return  The table name
     */
    public String tableName();

    /**
     * Implement to get the string used to create the table.
     *
     * The string is not checked for validity so errors are thrown during
     * creation of the table.
     *
     * @return  The creation string.
     */
    public String createString();

    /**
     * Implement to get the headers for the table.
     *
     * @return Array containing the table headers
     */
    public String[] tableHeaders();

    /**
     * Implement to handle each row from the table when selecting.
     *
     * During initialization each table is loaded.  Each row needs to be
     * handled accordingly.
     *
     * @param rs The row to handle.
     * @throws SQLException Thrown when a column lookup fails (usually)
     */
    public void loadRow( ResultSet rs ) throws SQLException;

    /**
     * Implement to perform any initialization required outside of the normal
     * loading of the table.
     *
     * @throws SQLException Thrown if an error occurs
     */
    public void postinit() throws SQLException;

    /**
     * Implement to determine if the table should have every row read.
     *
     * Useful to set to false if another class loads all of this table's
     * data.
     *
     * @return True if data should be loaded, false otherwise
     */
    public boolean loadData();
}

package Twitter.HW1;

import java.sql.*;

/**
 * A utility class to abstract JDBC driver code between the API and the database. Initializes a connection
 * to the relational database upon construction. Much of the code here is sourced from Professor Rachlin's DS4300
 * course -- particularly the DoctorPatient code example.
 */
public class RDBUtils {
    private final Connection con;

    /**
     * Constructs an instance of the RDBUtils class by establishing a connection to the database.
     * @param url - the URL of the relational database.
     * @param user - the username of the role that has access to the db.
     * @param password - the password of the user trying to access the db.
     */
    public RDBUtils(String url, String user, String password) {
        this.con = getConnection(url, user, password);
    }

    /**
     * Establishes a connection through specifying the url, user, and password.
     * @param url - the URL of the relational database.
     * @param user - the username of the role that has access to the db.
     * @param password - the password of the user trying to access the db.
     * @return - A connection object to the rdb.
     */
    public Connection getConnection(String url, String user, String password) {
        if (con == null) {
            try {
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        return con;
    }

    /**
     * Returns the connection object to the Relational database so queries can be sent.
     * @return - a Connection to a relational DB.
     */
    public Connection getConnection() {
        if (con == null) {
            throw new IllegalStateException("Cannot get connection that has not been initialized yet. Please initialize" +
                    "by using the other method.");
        }

        return con;
    }

    /**
     * Closes the connection to the relational DB.
     */
    public void closeConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Inserts one record into the database.
     * @param insertSQL - the query to execute
     * @return - an integer representing the new value's id in the db.
     */
    public int insertOneRecord(String insertSQL) {
        int key = -1;
        try {

            // get connection and initialize statement
            Statement stmt = con.createStatement();

            stmt.executeUpdate(insertSQL, Statement.RETURN_GENERATED_KEYS);

            // extract auto-incremented ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) key = rs.getInt(1);

            // Cleanup
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("ERROR: Could not insert record: " + insertSQL);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return key;
    }


    /**
     * For a table of terms consisting of an id and string value pair, get the id of the term
     * adding a new term if it does not yet exist in the table
     *
     * @param table The table of terms
     * @param term  The term value
     * @return The id of the term
     */
    public int getOrInsertTerm(String table, String keyColumn, String valueColumn, String term) {

        int key = -1;

        try {
            Statement stmt = con.createStatement();
            String sqlGet = "SELECT " + keyColumn + " FROM " + table + " WHERE " + valueColumn + " = '" + term.toUpperCase() + "'";
            ResultSet rs = stmt.executeQuery(sqlGet);
            if (rs.next())
                key = rs.getInt(1);
            else {
                String sqlInsert = "INSERT INTO " + table + " (" + valueColumn + ") VALUES ('" + term.toUpperCase() + "')";
                stmt.executeUpdate(sqlInsert, Statement.RETURN_GENERATED_KEYS);
                rs = stmt.getGeneratedKeys();
                if (rs.next()) key = rs.getInt(1);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return key;

    }

}

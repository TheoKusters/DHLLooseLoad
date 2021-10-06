/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.ap.dhllooseload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.*;

/**
 * Simple class responsible for handling the database, it provides two methods
 * for fetching the necessary data from the db2 database.
 */
public class DatabaseConnection {

    protected static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class.getName());
    /**
     * Driver identifier, specifies that we want to connect to DB2 database
     * (IBM), present on an AS400
     */
    private final String DRIVER_IDENTIFIER = "com.ibm.as400.access.AS400JDBCDriver";
    /**
     * location where we can find the database, could be localhost or any ip,
     * naming="..." specifies the name of the machine
     */
    private String DATABASE_PATH = "jdbc:as400://" + "192.168.1.197" + ";naming=S650DAFA;errors=full";

    /**
     * location where we can find the database, could be localhost or any ip,
     * naming="..." specifies the name of the machine
     */
    private String DATABASE_PATH_DHL = "jdbc:as400://" + "%s" + ";naming=%s;errors=full";

    /**
     * location where we can find the database, could be localhost or any ip,
     * naming="..." specifies the name of the machine
     */
    // private final String DATABASE_PATH_DHL2 = "jdbc:as400://" + "localhost" +
    // ";naming=*LOCAL;errors=full";
    /**
     * Username for server validation
     */
    private String USERNAME = "MKLA";
    /**
     * password associated with the @USERNAME
     */
    private String PASSWORD = "MKLA";

    private String schema = "";

    /**
     * SQL query for fetching package data, NOTE: only necessary columns are
     * returned.
     */
    private String SQL_FETCH_PACKAGE_DATA = "SELECT PKNPAK, PKKUIP,PKNGOO,PKDVST,PKTVST,PKNUSS,PKKREG FROM %s.PFVPKV WHERE PKKUIP='%s' "
            + "AND PKONU9 <> 'REJECT' AND PKNUSS=11 ORDER BY PKDVST DESC, PKTVST DESC";
    //private String SQL_FETCH_PACKAGE_DATA2 = "SELECT PKNPAK,PKKUIP,PKNGOO,PKDVST,PKTVST,PKNUSS,PKKREG FROM PFVPKV WHERE PKKUIP='%s' "
    // + "AND PKONU9 <> 'REJECT' AND PKNUSS=11 ORDER BY PKDVST DESC, PKTVST DESC";
    /**
     * SQL query for fetching all package ID's between two packages.
     */
    private String SQL_FETCH_BETWEEN_PACKAGES = "SELECT PKNPAK,PKKUIP,PKNGWS,PKKREG FROM %s.PFVPKV"
            + " WHERE PKNGOO=%s AND (PKNPAK BETWEEN '%s' AND '%s') AND PKONU9 <> 'REJECT' AND PKNUSS=11 ORDER BY PKNPAK ASC";
    //private String SQL_FETCH_BETWEEN_PACKAGES2 = "SELECT PKNPAK,PKKUIP,PKNGWS,PKKREG FROM PFVPKV"
//	 + " WHERE PKNGOO=%s AND (PKNPAK BETWEEN '%s' AND '%s') AND PKONU9 <> 'REJECT' AND PKNUSS=11 ORDER BY PKNPAK ASC";

    /**
     * The initialized database connection
     */
    public Connection connection;

    /**
     * Registers the driver, initializes the database connection.
     */
    public DatabaseConnection(Properties properties) {
        this.DATABASE_PATH_DHL = String.format(DATABASE_PATH_DHL, properties.get("dbip"),
                properties.getProperty("database"));
        this.USERNAME = properties.getProperty("dbusername");
        this.PASSWORD = properties.getProperty("dbpassword");
        this.schema = properties.getProperty("schema");
        try {
            Class.forName(DRIVER_IDENTIFIER);
            connection = DriverManager.getConnection(DATABASE_PATH_DHL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.error("something went wrong: " + ex);
        }
    }

    /**
     * Constructor only used for testing, sub class @AbstractDatabaseConnection
     * calls this to avoid an initialization (and DB connection) to the AS400
     * server present at AP-Zeist.
     *
     * @param something nothing done with the variable, just so the
     * @AbstractDatabaseConnection does not call the default super constructor.
     */
    public DatabaseConnection(String something) {

    }

    /**
     * Method used to fetch all Packaged Identifiers (PKKUIP) scanned in between
     * two packages.
     *
     *
     * @param identifierFirst PKKUIP String identifier associated with the first
     * scanned package.
     * @param identifierLast PKKUIP String identifier associated with the last
     * scanned package.
     * @return ResultSet, containing the result of the query (Only PKKUIP
     * column) NOTE: cursor not set.
     * @throws SQLException when there is an error with the query execution or
     * db connection error.
     */
    public ResultSet fetchAllPackagesInBetween(String identifierFirst, String identifierLast) throws SQLException {
        ResultSet first = fetchSinglePackageInfo(identifierFirst);
        ResultSet last = fetchSinglePackageInfo(identifierLast);
        first.next();
        last.next();

        return getStatement().executeQuery(String.format(SQL_FETCH_BETWEEN_PACKAGES, schema, first.getString("PKNGOO"),
                first.getString("PKNPAK"), last.getString("PKNPAK")));

        //return getStatement().executeQuery(String.format(SQL_FETCH_BETWEEN_PACKAGES2,
        //first.getString("PKNGOO"),
        //first.getString("PKNPAK"), last.getString("PKNPAK")));
    }

    /**
     * Method used to return the (known) necessary columns associated with the
     * supplied package identifier (PKKUIP).
     *
     * @param identifier PKKUIP String identifier associated with the scanned
     * package.
     * @return ResultSet, containing the result of the query NOTE: cursor not
     * set.
     * @throws SQLException when there is an error with the query execution or
     * db connection error.
     */
    public ResultSet fetchSinglePackageInfo(String identifier) throws SQLException {
        return getStatement().executeQuery(String.format(SQL_FETCH_PACKAGE_DATA, schema, identifier));
        //return getStatement().executeQuery(String.format(SQL_FETCH_PACKAGE_DATA2,
        // identifier));
    }

    /**
     * Called to initialize the Driver if a connection is not present, not
     * calling it from anywhere, present whenever we want to adjust something.
     *
     * @param properties
     *
     * @throws SQLException when there is an error with the query execution or
     * db connection error.
     */
    public synchronized void init() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(DATABASE_PATH, USERNAME, PASSWORD);
        }
    }

    /**
     * Fetches a new statement which can be used to execute SQL queries. Checks
     * if a DB connection is present, if not initializes the connection.
     *
     * @return
     * @throws SQLException
     */
    private Statement getStatement() throws SQLException {
        if (connection == null) {
            init();
        }
        return connection.createStatement();
    }

    /*
	 * Main used to test some basic functionalities.
     */
    public static void main(String[] args) throws SQLException {
        DatabaseConnection db = new DatabaseConnection((new PropertiesReader()).read());
        Statement s = db.connection.createStatement();
        ResultSet z = s.executeQuery(
                "SELECT PKNPAK,PKKUIP,PKNGOO,PKDVST,PKTVST,PKNUSS,PKONU9,PKKREG FROM PFVPKV WHERE PKONU9 <> 'REJECT' AND PKNUSS=11");
        while (z.next()) {
            System.out.println(
                    z.getString("PKKUIP") + "PACK ID: " + z.getString("PKNPAK") + " and: " + z.getString("PKNGOO"));
            System.out.println(z.getString("PKKREG"));

        }
        System.out.println("did work");

        ResultSet set = db.fetchSinglePackageInfo("3SNN2732208201");
        System.out.println(set.getMetaData().toString());
        while (set.next()) {

            System.out.println(" row: " + set.getRow() + set.getString("PKKUIP") + " and: " + set.getString("PKNGOO")
                    + set.getString("PKDVST") + ":" + set.getString("PKTVST") + set.getString("PKNUSS"));
            System.out.println(set.getString("PKKREG"));

        }

        ResultSet set2 = db.fetchAllPackagesInBetween("JVGL17614175472", "3SKKD0000152396");

        while (set2.next()) {
            System.out.println("contains: " + set2.getString("PKKUIP"));
        }

        ResultSet set5 = db.fetchSinglePackageInfo(null);
        while (set5.next()) {
            System.out.println("contains: " + set5.getString("PKKUIP"));
        }

    }

    public void close() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
        }
    }

}

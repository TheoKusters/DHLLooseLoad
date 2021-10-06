package test.java.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import main.java.ap.dhllooseload.DatabaseConnection;


/**
 * still establishes a db connection
 * 
 * @author Micha Klamer
 *
 */
public class AbstractDatabaseConnection extends DatabaseConnection {

	public AbstractResultSet first, second;

	public AbstractDatabaseConnection(AbstractResultSet first, AbstractResultSet second) {
		super("");
		this.first = first;
		this.second = second;

	}

	@Override
	public ResultSet fetchAllPackagesInBetween(String identifierFirst, String identifierLast) throws SQLException {
		return first;
	}

	@Override
	public ResultSet fetchSinglePackageInfo(String identifier) throws SQLException {
		return second;
	}

	@Override
	public void init() {

	}

}

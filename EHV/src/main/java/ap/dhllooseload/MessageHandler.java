/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.ap.dhllooseload;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.slf4j.*;
/**
 * Responsible for handling an incoming JSON message, fetching the necessary
 * data from @DatabaseConnection, handling the query results and sending an
 * appropriate response message. After which it terminates it's parent.
 */
public class MessageHandler {

	/** Message received from @SocketHandler. */
	private String message;
	/**
	 * The parent which initialized us, reference kept so we can send an outgoing
	 * message
	 */
	private SocketHandler parent;
	/** Identifier of a type 2 message, a request for all packages in between. */
	private static final String MESSAGE_TYPE2_IDENTIFIER = "FirstPackageId";

protected static Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class.getName());
	private DatabaseConnection dataSource;

	/**
	 * Constructor used by @SocketHandler
	 * 
	 * @param message
	 *            the message @SocketHandler received
	 * @param parent
	 *            The SocketHandler instance which initializes us.
	 */
	public MessageHandler(String message, SocketHandler parent, DatabaseConnection source) {
		this.message = message;
		this.parent = parent;
		this.dataSource = source;
	}

	/**
	 * Handle method called by @SocketHandler, it attempts to parse the message into
	 * a JSONObject, fetches the PacakgeId, executes the query corresponding with
	 * the message type (either single request, or all packages in between)
	 * 
	 * @throws ParseException
	 *             whenever we were unable to parse the incoming message into a
	 *             valid JSONObject.
	 */
	public void handle() throws ParseException {
		// We respond with a validation message.
		try {
			JSONObject obj = (JSONObject) JSONValue.parseWithException(message);
			// check if the generated object contains a key similar to MESSAGE_TYPE_2
			if (!obj.keySet().contains(MessageHandler.MESSAGE_TYPE2_IDENTIFIER)) {
				this.parent.writeOutput(simpleRequest(obj).toJSONString());
			} else {
				this.parent.writeOutput(allPackagesInBetween(obj).toJSONString());
			}
		} catch (SQLException | ClassCastException | NumberFormatException e) {
			LOGGER.error("Error whilst handling the message + " + message, e);
		}
		close();
	}

	// clean up all references.
	private void close() {
		this.dataSource = null;
		this.parent = null;
		message = null;

	}

	/**
	 * Validate that a package is in the correct location (Goot).
	 * 
	 * @param source
	 *            the received JSON message, as JSONObject
	 * @return a JSONObject, containing the response message
	 * @throws SQLException
	 *             whenever something went wrong while executing the query
	 * @throws NumberFormatException
	 *             whenever something went wrong while parsing the Goot number.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject simpleRequest(JSONObject source) throws SQLException, NumberFormatException {
		JSONObject result = new JSONObject();
		// since it was a single validation request, we execute the query with the
		// PackageId value.
		if (source.containsKey("PackageId")) {
			ResultSet set = dataSource.fetchSinglePackageInfo(((String) source.get("PackageId")));
			// iterate towards the first element in the ResultSet
			if (set.next()) {
				// Since we (the reader) have no idea of the writer, we read the incoming int as
				// a long.
				if (source.get("Terminal") == null
						|| !(source.get("Terminal").equals(PackageValidationServer.TERMINAL_ID)
								&& set.getString("PKKREG").equals(PackageValidationServer.TERMINAL_ID))) {
					result.put("PackageSortedCorrectly", false);
					result.put("Error",
							"Terminal identifier unknown or does not correspond with the database entry. Example of expected value: utrecht = UTX");
					return result;
				}
				try {
					if (Long.parseLong(set.getString("PKNGOO").trim()) == (Long) source.get("Goot")) {
						result.put("PackageSortedCorrectly", true);
						result.put("Error", "");
					} else {
						result.put("PackageSortedCorrectly", false);
						result.put("Error", "Supplied Goot number: " + (Long) source.get("Goot")
								+ " does not correspond with: " + Integer.parseInt(set.getString("PKNGOO").trim()));
					}
				} catch (NumberFormatException e) {
					LOGGER.warn("Goot number was not known in the DB" + System.currentTimeMillis()
							+ " Received message: " + source.toJSONString());
					result.put("PackageSortedCorrectly", false);
					result.put("Error", "Goot number was not known in the DB" + System.currentTimeMillis()
							+ " Received message: " + source.toJSONString());
				}

			} else {
				// error state, we did not get any items in our ResultSet, so perhaps an Invalid
				// packageId.
				LOGGER.warn( "PackageId was not found, time: " + System.currentTimeMillis()
						+ " Received message: " + source.toJSONString());
				result.put("PackageSortedCorrectly", false);
				result.put("Error",
						"PackageId could not be found in the database, please make sure your message is valid. Sorry for the inconvenience.");
			}
		} else {
			// invalid message
			result.put("PackageSortedCorrectly", false);
			result.put("Error", "Message did not contain key value PackageId, please make sure your message is valid.");
		}
		return result;
	}

	/**
	 * Fetch all packages in between two supplied package Identifiers.
	 * 
	 * @param source
	 *            the received JSON message, as JSONObject
	 * @return a JSONObject, containing the response message
	 * @throws SQLException
	 *             whenever something went wrong while executing the query
	 */
	@SuppressWarnings("unchecked")
	private JSONObject allPackagesInBetween(JSONObject source) throws SQLException {
		JSONObject result = new JSONObject();
		if (!source.containsKey("FirstPackageId") || !source.containsKey("LastPackageId")) {
			LOGGER.warn("JSON Message did not contain either first/last key: " + source.toJSONString());
			return result;
		}

		// query for all PackageId's received in between two packages.
		ResultSet set = dataSource.fetchAllPackagesInBetween((String) source.get("FirstPackageId"),
				(String) source.get("LastPackageId"));

		List<String> identifiers = new ArrayList<>();
		int sum = 0;

		// iterate through the items in the ResultSet
		while (set.next()) {
			if (source.get("Terminal") == null || !(source.get("Terminal").equals(PackageValidationServer.TERMINAL_ID)
					&& set.getString("PKKREG").equals(PackageValidationServer.TERMINAL_ID))) {

				result.put("PackageSortedCorrectly", false);
				result.put("Error",
						"Terminal identifier unknown or does not correspond with the database entry. Example of expected value: utrecht = UTX");
				return result;
			}
			// add identifier.
			identifiers.add(set.getString("PKKUIP").trim());
			// add item weight to total weight
			sum += Integer.parseInt(set.getString("PKNGWS"));
		}
		// build the result message
		JSONArray array = new JSONArray();
		array.addAll(identifiers);
		result.put("PackagesWeight", sum);
		result.put("Packages", array);
		return result;
	}

}

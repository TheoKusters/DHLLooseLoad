/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.ap.dhllooseload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.*;
import org.json.simple.parser.ParseException;




/**
 * Class responsible for handling the established connection. We expect to
 * handle some network delays. Implements Runnable so it can be handed over to
 * the Executor service.
 */
public class SocketHandler implements Runnable {

	/** The socket for which we are responsible. */
	public Socket socket;
	/** Wrapper of the Socket InputStream, to ease reading from it. */
	public BufferedReader reader;
	/** StringBuilder which contains all received data. */
	private StringBuilder buffer;

	private DatabaseConnection source;

	private AtomicBoolean closed = new AtomicBoolean(false);

	protected static Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class.getName());

	/**
	 * Constructor used by @PackageValidationServer.
	 * 
	 * @param the
	 *            socket for which we will be responsible
	 * @throws IOException
	 *             when unable to open the socket InputStream.
	 */
	public SocketHandler(Socket socket, DatabaseConnection source) throws IOException {
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.socket = socket;
		this.source = source;
		buffer = new StringBuilder();
	}

	/**
	 * ONLY FOR TESTING!!
	 */
	protected SocketHandler() {
		// empty constructor

	}

	/**
	 * Runnable method, we read from the InputStream until we have a full JSON
	 * object, we do not expect a JSON Array since it is not part of the
	 * specification. After the message is received we hand the message over to the
	 * MessageHandler. NOTE: we wait for a max TIMEOUT_PERIOD specified in the
	 * PackageValidationServer class, if exceeded we terminate the connection.
	 */
	@Override
	public void run() {
		int curr;
		int open = 0;
		int closed = 0;
		try {

			while ((curr = reader.read()) != -1) {
				buffer.append((char) curr);
				/*
				 * bracer counting to know when we can stop waiting for input. NOTE: we do not
				 * expect consecutive messages, therefore the logic below should suffice.
				 */

				if (curr == '{' || curr == '[') {
					open++;
				} else if (curr == '}' || curr == ']') {
					closed++;
				}
				if (open > 0 && open == closed) {
					break;
				}
			}
			MessageHandler msg = new MessageHandler(buffer.toString(), this, source);
			if (open > 0 && open == closed) {
				try {
					// attempt to read JSON object, generate a response accordingly.
					msg.handle();
					if (!this.closed.get()) {
						close();
					}

				} catch (ParseException ex) {
					// received an invalid message, NEVER THROWN since we catch the Exception on the
					// message level.
					LOGGER.warn("Parse exception thrown, something wrong with the JSON message: " + buffer.toString(), ex);
					close();
				}
			} else {
				// not a full message, should not try to handle the message. terminate.
				LOGGER.warn("Did not receive a full JSON message, we did receive: " + buffer.toString());
				close();
			}
		} catch (SocketTimeoutException e) {
			// is an IOException, however we want different logic.
			LOGGER.warn("Socket timed out current msg: " + buffer.toString(), e);
			close();

		} catch (IOException ex) {
			LOGGER.warn("IOException in SocketHandler current msg: " + buffer.toString(), ex);
			close();
		}

	}

	private void close() {
		try {
			socket.close();
			reader.close();
		} catch (IOException e) {
			LOGGER.warn("exception while closing:"+ e);
		}
		buffer = null;
		source = null;
		closed.set(true);
		LOGGER.info("Closed SocketHandler instance");
	}

	/**
	 * Method called from @MessageHandler to send a response message if applicable.
	 * 
	 * @param string
	 *            the outgoing data.
	 * @throws IOException
	 *             whenever something goes wrong writing the data to the output
	 *             stream.
	 */
	public void writeOutput(String string) {
		System.out.println("writing output: " + string);
		try {
			socket.getOutputStream().write(string.getBytes("UTF-8"));
			socket.getOutputStream().flush();
			close();
		} catch (IOException e) {
			LOGGER.warn("Unable to write data to the output stream: " + string, e);
		}

	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.ap.dhllooseload;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.slf4j.*;


/**
 * Class which will be responsible for accepting and closing communication
 * channels on a specific port number (TCP/IP).
 */
public class PackageValidationServer implements Runnable {

	/** The ServerSocket instance which will listen on ports. */
	protected ServerSocket server;
	/** Executor service which will run the tasks. */
	protected ThreadPoolExecutor executor;
	/** The expected String encoding */
	public static final String ENCODING = "UTF-8";
	/** Port number on which we will listen for incoming connections */
	public static final int PORT_NUMBER = 1200;
	/** TIMEOUT period, after which the Socket will spot waiting for data. */
	private final int TIMEOUT_PERIOD = 5000;

	private final ArrayList<String> knownAddresses;

	private AtomicBoolean closed = new AtomicBoolean(false);

	protected static Logger LOGGER = LoggerFactory.getLogger(PackageValidationServer.class.getName());

	public static String TERMINAL_ID = "";

	protected DatabaseConnection source;

	/**
	 * Default constructor. Starting point of the executable jar, we initialize the
	 * ServerSocket, initialize the Database Connection and only if all succeed,
	 * will we start listening on the specified port number for incoming
	 * connections.
	 * 
	 * @throws Exception
	 *             Whenever something happens which we cannot restore, such as
	 *             unable to initialize database, unable to initialize ServerSocket.
	 * 
	 */
	public PackageValidationServer(DatabaseConnection source) throws Exception {
            LOGGER.info("Starting up sequence");
            System.out.println(PackageValidationServer.class.getName());
		try {
			Properties properties = (new PropertiesReader()).read();
			PackageValidationServer.TERMINAL_ID = properties.getProperty("terminal");
			this.server = new ServerSocket(Integer.parseInt(properties.getProperty("port")));
			this.executor = (ThreadPoolExecutor) Executors
					.newFixedThreadPool(Integer.parseInt(properties.getProperty("threads")));
			this.source = source;
			this.source.init();
			this.knownAddresses = new IPReader().read();
			LOGGER.warn(
					"Package Validation Server started, listening on port : " + PackageValidationServer.PORT_NUMBER);
		} catch (NullPointerException | NumberFormatException | IOException | SQLException ex) {
			LOGGER.error(ex.toString());
			try {
				// we attempt to close all resources.. Might throw exceptions which we will
				// ignore.
				this.executor.shutdownNow();
				this.server.close();
				this.source.close();
			
			} catch (Throwable e) {
				// do nothing
			}
			throw ex;
		}
	}

	/**
	 * Run method, infinite loop in which we wait for incoming connections, when a
	 * connection is established, the initialized socket is handed over
	 * to @SocketHandler which performs the logic. The @SocketHandler is in turn
	 * handed over to the executor service which can schedule it according to it's
	 * queue.
	 * 
	 */
	@Override
	public void run() {
		while (!this.closed.get()) {
			try {
				Socket socket = this.server.accept();
				socket.setSoTimeout(TIMEOUT_PERIOD);
				if (this.knownAddresses.contains(socket.getInetAddress().getHostAddress())) {
					this.executor.execute(new SocketHandler(socket, source));
					LOGGER.info(
							"Accepted and handling a connection from: " + socket.getInetAddress().toString()
									+ " current active: " + executor.getActiveCount() + " completed: "
									+ executor.getCompletedTaskCount() + " pool size: " + executor.getPoolSize()
									+ " task count: " + executor.getTaskCount());
				} else {
					LOGGER.warn("Connection from uknown address!: " + socket.getInetAddress().toString());
				}

			} catch (SocketException e) {
				if (this.closed.get()) {
					// we were closed, we can ignore the error.
					break;
				} else {
					LOGGER.warn("ServerSocket was unexpectedly closed", e);
				}
			} catch (RejectedExecutionException e) {

			} catch (Throwable ex) {
				// catches all possible errors.
				LOGGER.error("unexpected throwable caught: " + ex);
			}
		}
	}

	public void close() throws IOException {
		executor.shutdownNow();
		this.server.close();
		this.closed.set(true);
	}

	// Main used for local, basic tests
	public static void main(String[] args) {
        
             LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    StatusPrinter.print(lc);

            System.setProperty("log.dir", CurrentDirectory.getCurrentDirectory());
		System.setProperty("log.name", "ALOGFILE");
               
		PackageValidationServer serv;
		try {
			serv = new PackageValidationServer(new DatabaseConnection((new PropertiesReader()).read()));
			serv.run();
		} catch (Exception e) {
			//
		}

	}
}

package test.java.test;

import java.io.IOException;
import java.net.Socket;
import main.java.ap.dhllooseload.DatabaseConnection;
import main.java.ap.dhllooseload.PackageValidationServer;
import main.java.ap.dhllooseload.SocketHandler;



/**
 * very basic mock, closes socket before handing it over so we get an IO
 * exception.
 *
 */
public class PackageValidationServerMock extends PackageValidationServer {

	public PackageValidationServerMock(DatabaseConnection source) throws Exception {
		super(source);
	}

	@Override
	public void run() {
		try {
			System.out.println("here");
			Socket s = server.accept();
			SocketHandler h = new SocketHandler(s, source);
			h.socket.close();
			h.reader.close();
			super.executor.execute(h);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

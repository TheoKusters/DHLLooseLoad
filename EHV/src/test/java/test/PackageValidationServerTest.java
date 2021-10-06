package test.java.test;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.TestCase;
import main.java.ap.dhllooseload.PackageValidationServer;

public class PackageValidationServerTest extends TestCase {

	/**
	 * Very basic test, what happens when we start the @PackageValidationServer on a
	 * port already in use.
	 * 
	 */
	public void testIO() {
		try {
			ServerSocket s = new ServerSocket(PackageValidationServer.PORT_NUMBER);
			try {
				new PackageValidationServer(null);
			} catch (Exception e) {
				s.close();
				return;
			}
			s.close();
			fail();
		} catch (IOException e) {
			// all good!

		}
	}

}

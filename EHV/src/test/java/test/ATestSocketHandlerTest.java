package test.java.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import main.java.ap.dhllooseload.PackageValidationServer;



public class ATestSocketHandlerTest extends TestCase {

	Column[] cols1 = new Column[] { new Column("PKNPAK"), new Column("PKKUIP"), new Column("PKNGOO"),
			new Column("PKDVST"), new Column("PKTVST"), new Column("PKNUSS"), new Column("PKKREG") };
	Column[] cols2 = new Column[] { new Column("PKNPAK"), new Column("PKKUIP"), new Column("PKNGWS"),
			new Column("PKNUSS"), new Column("PKKREG") };

	public void testHalfMessage() {
		String obj1 = "{\n" + "	\"Goot\": 1,\n" + "	\"Terminal\": \"UTX\",";
		Row row1 = new Row(new String[] { null, "JVGL17614175472", "1", null, null, null, "UTX" });
		Row row2 = new Row(new String[] { null, "JVGL17614175473", "23", null, null, null, "UTX" });
		AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols1);
		PackageValidationServer serv;
		try {
			serv = new PackageValidationServer(new AbstractDatabaseConnection(set, set));

			Thread z = new Thread(serv);
			z.start();
			try {
				Client client = new Client("localhost", 1200);
				client.write(obj1.getBytes(), true);
				client.write("-1".getBytes(), true);
				Thread.sleep(10000);
				z.interrupt();
				serv.close();
			} catch (IOException e) {
				fail();
			}
		} catch (Exception e1) {
			fail();
		}
	}

	public void testWeirdMessage() {
		String obj1 = "{" + "dit zou fout moeten gaan" + "}";
		Row row1 = new Row(new String[] { null, "JVGL17614175472", "1", null, null, null, "UTX" });
		Row row2 = new Row(new String[] { null, "JVGL17614175473", "23", null, null, null, "UTX" });
		AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols1);
		PackageValidationServer serv;
		try {
			serv = new PackageValidationServer(new AbstractDatabaseConnection(set, set));

			Thread z = new Thread(serv);
			z.start();
			try {
				Client client = new Client("localhost", 1200);
				client.write(obj1.getBytes(), true);
				Thread.sleep(500);
				System.out.println("slept");
				client.write("a".getBytes(), true);
				client.write("b".getBytes(), true);
				fail();
			} catch (IOException e) {
				// then it is valid
			} catch (InterruptedException e) {
				fail();
			}
			z.interrupt();
			try {
				serv.close();
			} catch (IOException e) {
				fail();
			}
		} catch (Exception e1) {
			fail();
		}
	}

	public void testIOException() throws IOException {
		Row row1 = new Row(new String[] { null, "JVGL17614175472", "1", null, null, null, "UTX" });
		Row row2 = new Row(new String[] { null, "JVGL17614175473", "23", null, null, null, "UTX" });
		AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols1);
		PackageValidationServerMock mock;
		try {
			mock = new PackageValidationServerMock(new AbstractDatabaseConnection(set, set));

			Thread z = new Thread(mock);
			z.start();
			z.interrupt();
			mock.close();

		} catch (Exception e) {
			fail();
		}
	}

	// public SocketHandler(Socket socket, DatabaseConnection source)
	public void testValidMessage() {
		String obj1 = "{\n" + "	\"Goot\": 1,\n" + "	\"Terminal\": \"UTX\",\n"
				+ "	\"PackageId\": \"JVGL17614175472\"\n" + "}";
		Row row1 = new Row(new String[] { null, "JVGL17614175472", "1", null, null, null, "UTX" });
		Row row2 = new Row(new String[] { null, "JVGL17614175473", "23", null, null, null, "UTX" });
		AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols1);
		PackageValidationServer serv;
		try {
			serv = new PackageValidationServer(new AbstractDatabaseConnection(set, set));

			Thread z = new Thread(serv);
			z.start();
			try {
				Client client = new Client("localhost", 1200);
				client.write(obj1.getBytes(), true);

				assertEquals("{\"PackageSortedCorrectly\":true,\"Error\":\"\"}", client.poll());
				z.interrupt();
				serv.close();
			} catch (IOException e) {
				fail();
			}
		} catch (Exception e1) {
			fail();
		}
	}

	public void testSuddenConnectionInterrupt() {
		String obj1 = "{\n" + "	\"Goot\": 1,\n" + "	\"Terminal\": \"EHX\",\n"
				+ "	\"PackageId\": \"JVGL17614175472\"\n" + "}";
		Row row1 = new Row(new String[] { null, "JVGL17614175472", "1", null, null, null });
		Row row2 = new Row(new String[] { null, "JVGL17614175473", "23", null, null, null });
		AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols1);
		PackageValidationServer serv;
		try {
			serv = new PackageValidationServer(new AbstractDatabaseConnection(set, set));

			Thread z = new Thread(serv);
			z.start();
			try {
				Client client = new Client("localhost", 1200);
				client.write(obj1.substring(0, (int) Math.floor(obj1.length() / 2)).getBytes(), true);
				client.socket.close();

			} catch (IOException e) {
				fail();
			}
			z.interrupt();
			try {
				serv.close();
			} catch (IOException e1) {
				fail();
			}
		} catch (Exception e2) {
			fail();
		}
	}

	class Client {
		Socket socket;

		Client(String host, int port) throws UnknownHostException, IOException {
			socket = new Socket(host, port);
		}

		public String poll() throws IOException {
			String res = "";
			int var;
			while ((var = socket.getInputStream().read()) != -1) {
				res += (char) var;
			}
			return res;
		}

		public void write(byte[] data, boolean flush) throws IOException {
			System.out.println(socket.isClosed());
			System.out.println(socket.isOutputShutdown());
			System.out.println(socket.isInputShutdown());
			System.out.println(socket.isConnected());
			socket.getOutputStream().write(data);
			if (flush) {
				socket.getOutputStream().flush();
			}
		}
	}
}

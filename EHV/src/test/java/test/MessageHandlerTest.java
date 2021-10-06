package test.java.test;

import org.json.simple.parser.ParseException;

import junit.framework.TestCase;
import main.java.ap.dhllooseload.MessageHandler;
import main.java.ap.dhllooseload.PackageValidationServer;
import main.java.ap.dhllooseload.SocketHandler;


public class MessageHandlerTest extends TestCase {

	Column[] cols1 = new Column[] { new Column("PKNPAK"), new Column("PKKUIP"), new Column("PKNGOO"),
			new Column("PKDVST"), new Column("PKTVST"), new Column("PKNUSS"), new Column("PKKREG") };
	Column[] cols2 = new Column[] { new Column("PKNPAK"), new Column("PKKUIP"), new Column("PKNGWS"),
			new Column("PKKREG") };

	public void testValidMessagesInBetween() {
		String obj1 = "{\n" + "	\"Goot\": 23,\n" + "	\"Terminal\": \"UTX\",\n"
				+ "	\"FirstPackageId\": \"JVGL0123456789\",\n" + "	\"LastPackageId\": \"JVGL0123456789\"\n" + "}";
		MessageHandler handle;
		try {
			TestSocket socket = new TestSocket();
			PackageValidationServer.TERMINAL_ID = "UTX";
			Row row1 = new Row(new String[] { null, "JVGL17614175472", "23", "UTX" });
			Row row2 = new Row(new String[] { null, "JVGL17614175473", "23", "UTX" });
			AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols2);
			handle = new MessageHandler(obj1, socket, new AbstractDatabaseConnection(set, set));
			handle.handle();
			assertEquals("{\"PackagesWeight\":46,\"Packages\":[\"JVGL17614175472\",\"JVGL17614175473\"]}",
					socket.output);
		} catch (ParseException e) {
			fail();
		}
	}

	public void testInValidMessagesInBetween() {
		String obj1 = "{\n" + "	\"Goot\": 23,\n" + "	\"Terminal\": \"UTX\",\n"
				+ "	\"FirstPackageId\": \"JVGL0123456789\",\n" + "}";
		MessageHandler handle;
		try {
			TestSocket socket = new TestSocket();

			Row row1 = new Row(new String[] { null, "JVGL17614175472", "23", "UTX" });
			Row row2 = new Row(new String[] { null, "JVGL17614175473", "23", "UTX" });
			AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols2);
			handle = new MessageHandler(obj1, socket, new AbstractDatabaseConnection(set, set));
			handle.handle();
			assertEquals("{}",
					socket.output);
		} catch (ParseException e) {
			fail();
		}
	}

	public void testSingleValidationRequest() {
		String obj1 = "{\n" + "	\"Goot\": 1,\n" + "	\"Terminal\": \"UTX\",\n"
				+ "	\"PackageId\": \"JVGL17614175472\"\n" + "}";
		MessageHandler handle;
		try {
			TestSocket socket = new TestSocket();

			Row row1 = new Row(new String[] { null, "JVGL17614175472", "1", null, null, null, "UTX" });
			Row row2 = new Row(new String[] { null, null, null, null, null, null, "UTX" });
			AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols1);
			handle = new MessageHandler(obj1, socket, new AbstractDatabaseConnection(set, set));
			handle.handle();
			System.out.println(socket.output);
		} catch (ParseException e) {
			fail();
		}

	}

	public void testSingleValidationRequestNoDB() {
		String obj1 = "{\n" + "	\"Goot\": 1,\n" + "	\"Terminal\": \"UTX\",\n"
				+ "	\"PackageId\": \"JVGL17614175472\"\n" + "}";
		MessageHandler handle;
		try {
			TestSocket socket = new TestSocket();

			AbstractResultSet set = new AbstractResultSet(new Row[] {}, cols1);
			handle = new MessageHandler(obj1, socket, new AbstractDatabaseConnection(null, set));
			handle.handle();
			assertEquals(
					"{\"PackageSortedCorrectly\":false,\"Error\":\"PackageId could not be found in the database, please make sure your message is valid. Sorry for the inconvenience.\"}",
					socket.output);
			System.out.println(socket.output);
		} catch (ParseException e) {
			fail();
		}

	}

	public void testSimpleString() {
		MessageHandler handle;
		try {
			handle = new MessageHandler("dit is een bericht", new TestSocket(),
					new AbstractDatabaseConnection(null, null));
			handle.handle();
		} catch (ParseException e) {
			assertEquals("Unexpected character (d) at position 0.", e.toString());
		}
	}

	public void testUnknownJSONMessage() {
		MessageHandler handle;
		try {
			TestSocket socket = new TestSocket();
			handle = new MessageHandler("{ \r\n" + "    \"Naam\": \"JSON\",\r\n"
					+ "    \"Type\": \"Gegevensuitwisselingsformaat\",\r\n" + "    \"isProgrammeertaal\": false,\r\n"
					+ "    \"Zie ook\": [ \"XML\", \"ASN.1\" ] \r\n" + "  }", socket,
					new AbstractDatabaseConnection(null, null));
			handle.handle();
			assertEquals(
					"{\"PackageSortedCorrectly\":false,\"Error\":\"Message did not contain key value PackageId, please make sure your message is valid.\"}",
					socket.output);
		} catch (ParseException e) {
			fail();
		}
	}

	public void testIncorrectGootNumberDB() {
		String obj1 = "{\n" + "	\"Goot\": 1,\n" + "	\"Terminal\": \"UTX\",\n"
				+ "	\"PackageId\": \"JVGL17614175472\"\n" + "}";
		MessageHandler handle;
		try {
			TestSocket socket = new TestSocket();

			Row row1 = new Row(new String[] { null, "JVGL17614175472", "12", null, null, null, "UTX" });
			Row row2 = new Row(new String[] { null, null, null, null, null, null, "UTX" });
			AbstractResultSet set = new AbstractResultSet(new Row[] { row1, row2 }, cols1);
			handle = new MessageHandler(obj1, socket, new AbstractDatabaseConnection(set, set));
			handle.handle();
			System.out.println(socket.output);
		} catch (ParseException e) {
			fail();
		}
	}

	public void testJSONArray() {
		MessageHandler handle;
		try {
			TestSocket socket = new TestSocket();
			handle = new MessageHandler("\r\n" + "\r\n" + "[ { \r\n" + "    \"Naam\": \"JSON\",\r\n"
					+ "    \"Type\": \"Gegevensuitwisselingsformaat\",\r\n" + "    \"isProgrammeertaal\": false,\r\n"
					+ "    \"Zie ook\": [ \"XML\", \"ASN.1\" ] \r\n" + "  },\r\n" + "  { \r\n"
					+ "    \"Naam\": \"JavaScript\",\r\n" + "    \"Type\": \"Programmeertaal\",\r\n"
					+ "    \"isProgrammeertaal\": true,\r\n" + "    \"Jaar\": 1995 \r\n" + "  } \r\n" + "]\r\n" + "",
					socket, new AbstractDatabaseConnection(null, null));
			handle.handle();
			assertEquals("", socket.output);

		} catch (ParseException e) {
			fail();
		}
	}

	public void testEmptyString() {
		MessageHandler handle;
		try {
			TestSocket socket = new TestSocket();
			handle = new MessageHandler("", socket, new AbstractDatabaseConnection(null, null));
			handle.handle();
		} catch (ParseException e) {
			assertEquals("Unexpected token END OF FILE at position 0.", e.toString());
		}
	}

	private class TestSocket extends SocketHandler {
		public String output = "";

		public TestSocket() {

		}

		@Override
		public void writeOutput(String string) {
			this.output = string;
		}

	}
}

package main.java.ap.dhllooseload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

	// private final String path = "C:\\Users\\Micha Klamer\\desktop\\Java
	// IDE\\eclipse\\config.properties";
	// private static final String IP_FILE_AS400 =
	// "/JSLO/Mits/LooseLoad/config.properties";

	private final String filename = "config.properties";

	public Properties read() {
		Properties prop = new Properties();
		InputStream input = null;
		try {

			input = new FileInputStream(CurrentDirectory.getCurrentDirectory() + "/" + filename);
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return prop;
	}

	public static void main(String[] args) {
		PropertiesReader rd = new PropertiesReader();
		rd.read();
		for (Object x : rd.read().keySet()) {
			System.out.println(x + " " + rd.read().getProperty((String) x));
		}
	}
}

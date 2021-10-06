/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.ap.dhllooseload;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerConfig {

	private static final String NAME = "logFile.log";
	// in bytes.
	private static final long MAX_FILE_SIZE = 100000;

	public LoggerConfig() {
		set();
	}

	public void set() {
		File file = new File(CurrentDirectory.getCurrentDirectory());
		if (!file.exists()) {
			try {
				file.createNewFile();
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (file.length() > MAX_FILE_SIZE) {
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		addLogger();
	}

	public void addLogger() {
		Logger logger = Logger.getLogger("");
	

		try {
			FileHandler hdl = new FileHandler(CurrentDirectory.getCurrentDirectory() + "/" + NAME);
			SimpleFormatter formatter = new SimpleFormatter();
			hdl.setLevel(Level.INFO);
			hdl.setFormatter(formatter);
			logger.addHandler(hdl);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void remove() {
		// nothing
	}
}

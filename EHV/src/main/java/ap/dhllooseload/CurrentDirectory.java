/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.ap.dhllooseload;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class CurrentDirectory {

	
	public static String getCurrentDirectory() {
		final Class<?> referenceClass = PackageValidationServer.class;
		final URL url = referenceClass.getProtectionDomain().getCodeSource().getLocation();

		try {
			final File jarPath = new File(url.toURI()).getParentFile().getParentFile();
			return jarPath.getAbsolutePath();
		} catch (final URISyntaxException e) {
			// etc.
		}
		return null;
	}
}

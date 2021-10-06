/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.ap.dhllooseload;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

public class IPReader {

	// private static final String IP_FILE_DIRECTORY = "IP.txt";
	// private static final String IP_FILE_AS400 = "/JSLO/Mits/LooseLoad/IP.txt";
	private final String filename = "IP.txt";

	public ArrayList<String> read() throws IOException {
		ArrayList<String> res = new ArrayList<>();
		BufferedReader reader = new BufferedReader(
				new FileReader(new File(CurrentDirectory.getCurrentDirectory() + "/" + filename)));
		Stream<String> str = reader.lines();
		Iterator<String> it = str.iterator();
		while (it.hasNext()) {
			res.add(it.next());
		}
		reader.close();
		return res;
	}

	public static void main(String[] args) throws IOException {
		IPReader r = new IPReader();
		ArrayList<String> we = r.read();
		for (String a : we) {
			System.out.println(a);
		}
	}

}

package FoldText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

public class TextInit {
	static ArrayList<String> strings;

	public static void readMap() throws IOException {
		BufferedReader inputStream = null;

		strings = new ArrayList<String>();

		try {
			// InputStream is = TextInit.class
			// .getResourceAsStream("res/FolderSave1.txt");
			InputStream is = new FileInputStream("FolderSave1.txt");

			inputStream = new BufferedReader(new InputStreamReader(is));

			String l;
			while ((l = inputStream.readLine()) != null) {
				strings.add(l);
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public static void readNewFold() throws IOException {
		// two lines, first is the string[], second is int[][]
		BufferedReader inputStream = null;

		strings = new ArrayList<String>();

		try {
			// InputStream is = TextInit.class
			// .getResourceAsStream("res/FolderSave1.txt");
			InputStream is = new FileInputStream(
					"src/FolderSave2.txt");

			inputStream = new BufferedReader(new InputStreamReader(is));

			String l;
			while ((l = inputStream.readLine()) != null) {
				strings.add(l);
				System.out.println("add:::");
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public static String getstrings() {
		return strings.get(0);
	}

	public static ArrayList<String> getStrings() {
		return strings;
	}

	public static void save(String st) {
		try {
			BufferedWriter writer = null;

			String prives = "src/FolderSave2.txt";

			OutputStream os = new FileOutputStream(prives);
			writer = new BufferedWriter(new OutputStreamWriter(os));

			writer.write(st);
			writer.newLine();
			writer.flush();

			os.close();

			// System.out.println(TextInit.class.getResource(name));

			// PrintStream out1 = new PrintStream(fileUrl);
			// out1.close();

			// File logfile = new File();

			// PrintWriter out = new PrintWriter("filename.txt");
			// out.write("halo");

			// System.out.println("c: " + (TextInit.class.getCanonicalName()));
			// System.out.println("n: " + (TextInit.class.getName()));
			// System.out.println((TextInit.class.getR));

			// OutputStream is = Text.class
			// .getResourceAsStream("res/textOne.txt");

			// System.out.println(logfile.getCanonicalFile());

			// writer = new BufferedWriter(new FileWriter(logfile));

			// writer.write("Hello world!");
			System.out.println("getAttt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("getTo");
		}
	}

	static void saveNew(String st, String in) {
		try {
			BufferedWriter writer = null;

			String prives = "FolderSave2.txt";

			OutputStream os = new FileOutputStream(prives);
			writer = new BufferedWriter(new OutputStreamWriter(os));

			writer.write(st);
			writer.newLine();
			writer.write(in);
			writer.newLine();
			writer.flush();

			os.close();
			System.out.println("saved");
		} catch (Exception ex) {
		}
	}
}

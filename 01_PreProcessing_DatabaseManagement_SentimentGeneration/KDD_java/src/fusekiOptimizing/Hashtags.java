package fusekiOptimizing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import charHandling.CharScanner;

/**
 * Klasse, um Hashtags aus den RDF-Daten zu rekonstruieren
 * 
 * @author Marius Laemmlin
 * 
 */
public class Hashtags {
	
	/**
	 * Scannt als args übergebenen Dateipfad und speichert korrigierte Version in Dokument unter angegebenem Dateipfad
	 * 
	 * @param args - Stringarray, für das Folgendes gilt: args[0] ist bestehende Datei - args[1] ist Zieldatei - args[2] ist Hashtaglist 
	 */
	public static void scan(String[] args) {
		BufferedWriter writer;
		BufferedReader reader;
		BufferedWriter hashtagWriter;
		
		ArrayList<String> hashtags;
		String save, hash;
		
		try {
			File f0 = new File(args[0]);
			File f1 = new File(args[1]);
			File f2 = new File(args[2]);
			f0.createNewFile();
			f1.createNewFile();
			f2.createNewFile();
			
			reader = new BufferedReader(new FileReader(f0));
			writer = new BufferedWriter(new FileWriter(f1));
			hashtagWriter = new BufferedWriter(new FileWriter(new File("C:/Users/Local/Documents/Universit�t/KDD Seminar/hashtaglist_.txt"))); 

			// Suchlauf
			while (CharScanner.findString("sioc:content	", writer, reader)) {

				// Liste zum Speichern der Hashtags
				hashtags = new ArrayList<>();

				while (CharScanner.findString("  ", "^^xsd:string;", writer, reader)) {
					hash = getHashtag(writer, reader);
					if (!hash.equals("#####")) {
						hashtags.add(hash);
						hashtagWriter.write(hash);
						hashtagWriter.newLine();
					}
				}

				// Zeilenende suchen
				save = reader.readLine();
				writer.write(save);
				writer.newLine();

				// sioc:has_subject Line einfuegen
				for (String hashtag : hashtags) {
					writer.write("	sioc:has_subject	\"" + hashtag + "\"^^xsd:string;");
					writer.newLine();
				}
				
			}
			// Timer
			writer.flush();
			hashtagWriter.flush();
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe);
			System.out.println(fnfe.getLocalizedMessage());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}
	
	
	/**
	 * Uebernimmt die Funktion eines StringTokenizer
	 * 
	 * @return - Hashtag-String
	 * @throws IOException
	 */
	public static String getHashtag(BufferedWriter writer, BufferedReader reader) throws IOException {
		StringBuffer sb = new StringBuffer();
		String result;
		int a;
		boolean searching = true;
		while (searching) {
			// Reader markieren, damit im Falle von mehreren Hashtags zur�ckgesprungen werden kann
			reader.mark(5);
			a = reader.read();
			writer.write(a);
			if (a == ' ' || a == ',' || a == '.' || a == '	' || a == '"' || a == '!' || a == '?' || a == ':' || a == '\\' || a == '\n' || a == '\'') {
				searching = false;
				if(a == ' ') {
					reader.reset();
				}
			} else {
				sb.append((char) a);
			}
		}
		result = sb.toString();
		if (result.equals("") || result.equals(" ") || result.startsWith("http") || result.equals("-"))
			result = "#####";
		return result;

	}
}

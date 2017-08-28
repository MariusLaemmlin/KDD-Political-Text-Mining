package charHandling;

import java.io.*;

/**
 * Hilfsklasse, um Zeichenketten zu identifizieren
 * 
 * @author Marius Laemmlin
 * 
 */
public class CharScanner {

	/**
	 * @param s
	 *            - Gesuchter String
	 * @param terminate
	 *            - String, bei dem die Suche abgebrochen werden soll
	 * @return - Gibt "false" zur�ck, wenn durch "terminate" terminiert wurde
	 * @throws IOException
	 */
	public static boolean findString(String s, String terminate, BufferedWriter writer, BufferedReader reader) throws IOException {
		int a = 0, i;
		boolean equal = false, terminated = false;

		// Umwandeln von String in char-Array
		char[] target = s.toCharArray();
		char[] runner = new char[target.length];
		char[] terminator = terminate.toCharArray();

		// Initiale Bef�llung des Test-Char-Arrays bis auf die letzte Stelle
		for (i = 1; i < target.length; i++) {
			runner[i] = (char) reader.read();
			writer.write(runner[i]);
		}

		// Suche bis Textende
		while ((!terminated) && (!equal) && ((a = reader.read()) != -1)) {

			// Umkopieren des Arrays
			for (i = 0; i < target.length - 1; i++) {
				runner[i] = runner[i + 1];
			}
			// Bef�llen des letzten Felds
			runner[runner.length - 1] = (char) a;

			// Test auf Gleichheit zu Target-Array
			equal = true;
			for (i = 0; i < target.length; i++) {
				if (runner[i] != target[i]) {
					equal = false;
				}
			}

			// Test auf Gleichheit zu Terminierungs-String
			terminated = true;
			for (i = 0; i < target.length; i++) {
				if (runner[i] != terminator[i]) {
					terminated = false;
				}
			}

			// Sicherstellung, dass terminated-Entscheidung nicht nur aufgrund
			// von ^^ getroffen wird.
			if (terminated) {
				writer.write(a);
				if ((a = reader.read()) != 'x') {
					writer.write(a);
					terminated = false;
				} else {
					writer.write(a);
					if ((a = reader.read()) != 's') {
						writer.write(a);
						terminated = false;
					}

				}
			}

			// Ende wenn Target passiert wurde

			// Writer auf aktuellem Stand halten
			writer.write(a);
		}
		return !terminated;
	}

	/**
	 * @param s
	 *            - Gesuchter String
	 * @return - Gibt "false" zur�ck, wenn Textende erreicht wurde
	 * @throws IOException
	 */
	public static boolean findString(String s, BufferedWriter writer, BufferedReader reader) throws IOException {
		int a = 0, i;
		boolean equal = false;

		// Umwandeln von String in char-Array
		char[] target = s.toCharArray();
		char[] runner = new char[target.length];

		// Initiale Bef�llung des Test-Char-Arrays bis auf die letzte Stelle
		for (i = 1; i < target.length; i++) {
			runner[i] = (char) reader.read();
			writer.write(runner[i]);
		}

		// Suche bis Textende
		while ((!equal) && ((a = reader.read()) != -1)) {

			// Umkopieren des Arrays
			for (i = 0; i < target.length - 1; i++) {
				runner[i] = runner[i + 1];
			}
			// Bef�llen des letzten Felds
			runner[runner.length - 1] = (char) a;

			// Test auf Gleichheit zu Target-Array
			equal = true;
			for (i = 0; i < target.length; i++) {
				if (runner[i] != target[i]) {
					equal = false;
				}
			}

			// Ende wenn Target passiert wurde

			// Writer auf aktuellem Stand halten
			writer.write(a);

		}
		return (a != -1);
	}
}

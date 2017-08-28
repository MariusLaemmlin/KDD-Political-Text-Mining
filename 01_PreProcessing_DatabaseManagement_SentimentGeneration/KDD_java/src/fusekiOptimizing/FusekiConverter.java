package fusekiOptimizing;

import charHandling.CharKiller;

/**
 * Klasse, um mit Hilfe der Hashtags- und CharKiller-Klassen die RDF-Daten für Fuseki zu optimieren und Hashtags zu rekonstruieren
 * 
 * @author Marius Laemmlin
 * 
 */
public class FusekiConverter {
	public static void main(String[] args) {
		
		// Erstelle File-Namen
		String[] fileNames = new String[50];
		int[] numbers = new int[2];
		numbers[0] = 0;
		numbers[1] = 0;
		
		for(int i = 0; i < fileNames.length-1; i++) {
			numbers[0] = numbers[1] + 1;
			numbers[1] = numbers [1] + 250000;
			fileNames[i] = "" + numbers[0] + "-" + numbers[1];
		}
		
		fileNames[fileNames.length-1] = "12250001-12447299";
		
		// Zeittracking
		long start, stop;
		
		// Hashtags extrahieren und annotieren. Illegale Chars entfernen
		for(int i = 0; i < fileNames.length; i++) {
			System.out.println("Beginning search...");
			start = System.currentTimeMillis();
			
			args = new String[3];
			args[0] = "C:/Users/Local/Documents/Universitï¿½t/KDD Seminar/social/xlime_social_en_" + fileNames[i] + ".ttl";
			args[1] = "C:/Users/Local/Documents/Universitï¿½t/KDD Seminar/social/hashtags/xlime_social_en_hashtags_" + fileNames[i] + ".ttl";
			args[2] = "C:/Users/Local/Documents/Universitï¿½t/KDD Seminar/social/removed_illegal_chars/xlime_social_en_hashtags_chars_" + fileNames[i] + ".ttl";
			System.out.println(args[1]);
			Hashtags.scan(args);
			
			stop = System.currentTimeMillis();
			long diff = stop-start;
			System.out.println("Time for search: " + (diff/1000.0) + "s");
			start = System.currentTimeMillis();
			
			// Entferne fehlerhafte Chars
			CharKiller.illegalCharKiller(args);
			
			stop = System.currentTimeMillis();
			diff = stop-start;
			System.out.println("Time for kill: " + (diff/1000.0) + "s");

		}
		
	}
}

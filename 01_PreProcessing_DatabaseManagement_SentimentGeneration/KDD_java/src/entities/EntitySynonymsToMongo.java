package entities;

import java.io.*;
import java.util.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

/**
 * Klasse zum Erkennen von Synonymen in einer MongoDB basierend auf einer Liste.
 * 
 * @author Marius Laemmlin
 * @version 2017-07-09
 * 
 */
public class EntitySynonymsToMongo implements Runnable {

	private MongoCollection<Document> col;
	private File file;
	private static int count = 0;
	private static Calendar timer = Calendar.getInstance();
	private String threadName;
	private static int limit;

	public EntitySynonymsToMongo(MongoCollection<Document> col, File file) {
		this.col = col;
		this.file = file;
		this.threadName = "Any thread";
		this.limit = 1000;
	}
	
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	
	public static void setLimit(int l) {
		limit = l;
	}
	
	

	public void run() {
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(this.file));
			reader.readLine(); // Überschriften ignorieren
			for(int i = 1; i <= limit; i++) { // nur die ersten 1000 Einträge betrachten
				replaceNextSynonym(reader, this.col);
				if(i%100 == 0) {
					System.out.println(this.threadName + ": " + i);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean replaceNextSynonym(BufferedReader reader, MongoCollection<Document> col) throws IOException {
		String replacement = reader.readLine(); // Einlesen aus der Entity-Liste
		if (replacement == null)
			return false; // Falls die Entity-Liste abgearbeitet wurde

		// Zerlegen der Zeile aus der Entity-Liste
		String[] decomposedReplacement = decomposeEntityString(replacement);
		String enText = decomposedReplacement[0];
		String enSyn = decomposedReplacement[1];
		String enOri = decomposedReplacement[2]; // Remain/Leave/null
		String enPol = decomposedReplacement[3]; // True/False als String oder
													// null
		
		// Suche in der MongoCollection
		Bson filter = Filters.elemMatch("sentiment", new Document("text", enText));
		FindIterable<Document> cursor = col.find(filter).projection(Projections.include("_id", "sentiment"));

		boolean changed;
		Document changedDoc;
		ArrayList<Document> changedList;
		for (Document document : cursor) {
			ArrayList<Document> sentiments = (ArrayList<Document>) document.get("sentiment");
			changed = false;
			changedList = new ArrayList<Document>();
			for (Document sentiment : sentiments) {
				changedDoc = sentiment;
				String moText = sentiment.getString("text");
				if (enText.equals(moText)) {

					if (!moText.equals(enSyn) && enSyn != null) { // Fall, dass Text und Synonym identisch sind oder enSyn null ist 
						changed = true;
						changedDoc.replace("text", enSyn);
					}

					if (enOri != null) {
						changed = true;
						if (enOri.equalsIgnoreCase("remain")) {
							changedDoc.append("orientation", "remain");
						} else if (enOri.equalsIgnoreCase("leave")) {
							changedDoc.append("orientation", "leave");
						}
					}
					if (enPol != null) {
						changed = true;
						if (enPol.equalsIgnoreCase("true")) {
							changedDoc.append("politician", "politician");
						}
					}
				}
				changedList.add(changedDoc);
			}
			if (changed) { // Falls eine Änderung eines Sentiments vorgenommen
							// wird, soll das gesamte Array ausgetauscht werden
				addCount();
				col.updateOne(new Document("_id", document.getString("_id")),
						new Document("$set", new Document("sentiment", changedList)));
				if((count % 1000) == 0) { 
					System.out.println(count);
					System.out.println((Calendar.getInstance().getTimeInMillis()-timer.getTimeInMillis()));
					timer = Calendar.getInstance();
				}
			}
		}

		return true;
	}
	
	public synchronized static void addCount() {
		EntitySynonymsToMongo.count++;
	}
	

	private static String[] decomposeEntityString(String replacement) {
		// [Name, Replacement, leave/remain/false, politician: true/false
		String[] result = new String[4];
		String puffer;
		for (String s : result) {
			s = new String();
		}
		StringTokenizer st = new StringTokenizer(replacement, ";");
		result[0] = st.nextToken();
		while(result[0].startsWith(" ")) {
			result[0] = result[0].substring(1); // Ignoriere Leerzeichen
		}
		st.nextToken(); // Ignoriere Häufigkeit
		
		try { // Falls Remain/Leave und Politician nicht gesetzt sind, wird eine
				// Exception geworfen. Diese kann ignoriert werden, da der
				// String dann null ist
			result[1] = st.nextToken();
			while(result[1].startsWith(" ")) {
				result[1] = result[1].substring(1); // Ignoriere Leerzeichen
			}
			if((puffer = st.nextToken()).equals("True") || puffer.equals("False")) {
				result[3] = puffer;
			} else {
				result[2] = puffer;
			}
		} catch (NoSuchElementException e) {

		}
//		for (String s : result) {
//			System.out.print(s + "				");
//		}
//		System.out.println();
		return result;
	}

	public static int getCount() {
		return count;
	}

}

package sentiments;

import java.io.*;
import java.util.*;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;

import org.bson.*;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;

/**
 * Ergänzt Steffens Ergebnisse der naiven Sentimentanalyse mit dem Datum zu den
 * IDs mit Hilfe der Datenbank.
 * 
 * @author Marius Laemmlin
 * @version 2017-07-11
 * 
 */
public class EnrichNaiveSentiment {

	private static final File folderpath = new File(
			"C:/Users/Local/Documents/GitHub/KIT-KDD/Dokumente/NaiveSentimentanalyse");
	private static final File outputFile = new File(
			"C:/Users/Local/Documents/Universität/KDD Seminar/exports/naiveSentimentDate.txt");
	private static final String dbName = "social";

	public static void main(String[] args) {
		String line = "";
		String id = "";
		try {
			BufferedReader br;
			PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
			StringTokenizer st;
			MongoClient mC = new MongoClient();
			MongoDatabase db = mC.getDatabase(dbName);

			for (File file : folderpath.listFiles()) {

				br = new BufferedReader(new FileReader(file));
				// Ignoriere Spaltenbeschriftung
				br.readLine();
				while ((line = br.readLine()) != null) {
					st = new StringTokenizer(line, ", ");
					id = st.nextToken();
					int sentiment = Integer.parseInt(st.nextToken());
					if (st.hasMoreTokens())
						System.err.println("Too many tokens!");
					Date date = findDateByID(id, db);
					if (Objects.isNull(date))
						System.err.print("Error: Date is null!");
					writeOutputFile(id, sentiment, date, pw);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			System.err.println("Error: " + line);
			System.err.println("id: " + id);
		}

	}

	public static Date findDateByID(String id, MongoDatabase db) {
		MongoIterable<String> colNames = db.listCollectionNames();
		for (String colName : colNames) {
			MongoCollection<Document> col = db.getCollection(colName);
			Bson filter = new Document("_id", id);
			FindIterable<Document> cursor = col.find(filter);
			if(cursor.first() != null) {
				return cursor.first().getDate("date");
			}
		}
		return null;
	}

	private static void writeOutputFile(String id, int sentiment, Date date, PrintWriter pw) {
		int year = date.getYear() + 1900;
		int month = date.getMonth() + 1;
		int day = date.getDate();
		int hour = date.getHours();
		int minute = date.getMinutes();
		int second = date.getSeconds();
		if (year == 2016) {
			pw.println(id + "; " + sentiment + "; " + year + "-0" + month + "-" + (day < 10 ? "0" : "") + day + " "
					+ (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute + ":"
					+ (second < 10 ? "0" : "") + second);
		}
	}

}

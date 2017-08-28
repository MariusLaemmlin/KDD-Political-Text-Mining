package mongoDB;

import java.util.Calendar;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.*;
import java.util.*;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

/**
 * @author Marius Laemmlin
 * @version 2017-06-30
 * 
 */
public class SelectValuableSocials implements Runnable {

	public void run() {
	}

	/**
	 * Erstellt fuer jeden Tag ein Array aus den Eintraegen und gibt eine Liste
	 * der Listen aller Tage zurueck
	 * 
	 * @return
	 */
	public static void getDailyLists(String databaseName) {

		// MongoReader
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase(databaseName);
		MongoIterable<String> collections = db.listCollectionNames();
		Bson proj = Projections.include("_id", "rating", "content", "date");

		// MongoWriter
		MongoClient mC2 = new MongoClient();
		MongoDatabase db2 = mC2.getDatabase("intermediate");
		ArrayList<MongoCollection<Document>> saveCols = new ArrayList<>();

		for (int i = 1; i <= 30; i++) {
			saveCols.add(db2.getCollection("day-" + i));
		}

		for (String colName : collections) { // Iteriere ueber alle Collections
			FindIterable<Document> cursor = db.getCollection(colName).find().projection(proj);
			for (Document x : cursor) {
				Date d = x.getDate("date"); // Lese das Datum jedes Eintrags aus
				Calendar c = Calendar.getInstance();
				c.setTime(d);
				if (c.get(Calendar.MONTH) == 5) { // Suche nur im Juni
					int date = c.get(Calendar.DATE);
					saveCols.get(date - 1).insertOne(x); // Fuege den Eintrag
															// der
															// entsprechenden
															// Liste hinzu. -1,
															// damit der 01.06.
															// den Index 0
															// erhaelt
				}
			}
		}
	}

	public static void sort() {
		// MongoReader
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("intermediate");
		MongoIterable<String> collections = db.listCollectionNames();

		// MongoWriter
		MongoClient mC2 = new MongoClient();
		MongoDatabase db2 = mC2.getDatabase("intermediate2");
		ArrayList<MongoCollection<Document>> saveCols = new ArrayList<>();

		Calendar c = Calendar.getInstance();
		System.out.println("Sorting started!");

		for (String colName : collections) {
			MongoCollection<Document> col = db.getCollection(colName);
			MongoCollection<Document> col2 = db2.getCollection(colName);

			FindIterable<Document> cursor = col.find().projection(Projections.include("_id", "rating", "content"));
			ArrayList<RatingDocument> list = new ArrayList<>();
			for (Document x : cursor) {
				list.add(new RatingDocument(x));
			}

			list.sort(null);

			for (RatingDocument x : list) {
				Document d = new Document(x);
				col2.insertOne(d);
			}
		}

		System.out.println("Sorting finished in "
				+ ((Calendar.getInstance().getTimeInMillis() - c.getTimeInMillis()) / 1000) + "s!");

	}

	public static void combineInPrioList(String folderpath) throws IOException {

		// MongoReader
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("intermediate2");
		MongoClient MCDate = new MongoClient();
		MongoDatabase dbDate = MCDate.getDatabase("social");

		boolean searching = true;

		ArrayList<Iterator<Document>> iterators = new ArrayList<>();
		MongoIterable<String> colNames = db.listCollectionNames();
		for (String colName : colNames) {
			iterators.add(db.getCollection(colName).find().iterator());
		}

		ArrayList<File> files = new ArrayList<>();
		int currentCount = 0;
		int currentIteration = 1;
		File currentFile = new File(folderpath + "social_rated_" + "001.txt");
		files.add(currentFile);
		PrintWriter pw = new PrintWriter(new FileWriter(currentFile), true);
		pw.println("id; date; content");

		while (searching) {
			searching = false; // Wenn es keine Aenderung gibt, wird abgebrochen

			for (Iterator<Document> it : iterators) {
				if (it.hasNext()) {
					searching = true;
					Document doc = it.next();
					if (currentCount < 20000) {
						Date date = null;
						int year = 0;
						int month = 0;
						int day = 0;
						int hour = 0;
						int minute = 0;
						int second = 0;
						// Suche nach Datum in der Hauptdatenbank
						MongoIterable<String> colNamesDate = dbDate.listCollectionNames();
						for (String colName : colNamesDate) {
							MongoCollection<Document> colDate = dbDate.getCollection(colName);
							FindIterable<Document> cursor = colDate.find(new Document("_id", doc.getString("_id")));
							if (cursor.first() != null) {
								date = cursor.first().getDate("date");
								year = date.getYear() + 1900;
								month = date.getMonth() + 1;
								day = date.getDate();
								hour = date.getHours();
								minute = date.getMinutes();
								second = date.getSeconds();
							}
						}
						if (Objects.isNull(date)) {
							System.err.println("Error: Date is null! Year: " + year);
							System.exit(1);
						}
						if (year == 2016) {
							pw.println(doc.getString("_id") + "; " + year + "-0" + month + "-" + (day < 10 ? "0" : "")
									+ day + " " + (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "")
									+ minute + ":" + (second < 10 ? "0" : "") + second + "; "
									+ doc.getString("content"));
						}
						currentCount++;
					} else {
						currentIteration++;
						currentFile = new File(folderpath + "social_rated_"
								+ ((currentIteration < 100)
										? (currentIteration < 10 ? ("00" + currentIteration) : ("0" + currentIteration))
										: currentIteration)
								+ ".txt");
						System.out.println("File: " + currentFile.getName());
						currentCount = 0;
						pw = new PrintWriter(new FileWriter(currentFile), true);
						pw.println("id; date; content");
					}

				}
			}
		}
	}

}

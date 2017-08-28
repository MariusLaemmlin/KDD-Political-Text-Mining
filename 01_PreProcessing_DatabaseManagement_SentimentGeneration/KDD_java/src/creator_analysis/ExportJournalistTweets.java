package creator_analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

/**
 * Klasse, um den Inhalt von Veröffentlichungen in einer Text-Datei zu exportieren
 * @author Marius Laemmlin
 * 
 */
public class ExportJournalistTweets {

	public static void main(String[] args) throws IOException {

		String databaseName = "social";

		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase(databaseName);
		BufferedReader br;
		PrintWriter pw;
		br = new BufferedReader(new FileReader(new File(
				"C:/Users/Local/Documents/Universität/KDD Seminar/news-creators/newspaper_authors_twitteraccs.txt")));
		pw = new PrintWriter(
				new FileWriter(
						new File("C:/Users/Local/Documents/Universität/KDD Seminar/news-creators/author_tweets.txt")),
				true);
		pw.println("id; author; content");
		long count = 0;
		String col;
		int length = 50;
		System.out.printf("Querying %1$d Collections.%n", length);
		String author;

		br.mark(100000);
		for (int i = 1; i <= length; i++) {
			if (i < 10) {
				col = databaseName + "_0" + i;
			} else {
				col = databaseName + "_" + i;
			}
			MongoCollection<Document> mCol = db.getCollection(col);
			count = 0;
			br.reset();
			while ((author = br.readLine()) != null) {
				Bson filter = Filters.and(Filters.regex("creator", author, "i"));
				FindIterable<Document> cursor = mCol.find(filter);

				for (Document x : cursor) {
					count++;
					pw.println(x.getString("_id") + "; " + x.getString("creator") + "; " + x.getString("content"));

				}
			}
			System.out.println("Col " + i + ": " + count);
		}
		pw.close();
		br.close();
		System.out.println("Done.");

	}

}

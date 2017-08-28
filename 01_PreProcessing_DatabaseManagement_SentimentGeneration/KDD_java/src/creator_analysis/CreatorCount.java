package creator_analysis;

import java.util.ArrayList;
import java.io.*;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

/**
 * Klasse, um Anzahl an Veröffentlichungen vordefinierter Autoren zu ermitteln
 * 
 * @author Marius Laemmlin
 * @version 2017-08-28
 * 
 */
public class CreatorCount {

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
						new File("C:/Users/Local/Documents/Universität/KDD Seminar/news-creators/authors_count.txt")),
				true);
		long count = 0;
		String col;
		int length = (databaseName.equals("social") ? 50 : 15);
		System.out.printf("Querying %1$d Collections.%n", length);
		String author;
		while ((author = br.readLine()) != null) {
			count = 0;
			for (int i = 1; i <= length; i++) {
				if (i < 10) {
					col = databaseName + "_0" + i;
				} else {
					col = databaseName + "_" + i;
				}

				Bson filter = Filters.and(Filters.regex("creator", author, "i"));

				MongoCollection<Document> mCol = db.getCollection(col);

				FindIterable<Document> cursor = mCol.find(filter).projection(Projections.include("creator"));

				for (Document x : cursor) {
					count++;
				}
			}
			pw.println(author + "; " + count);
			System.out.println(author + ": " + count);
		}
		pw.close();
		br.close();
		System.out.println("Done.");

	}

}

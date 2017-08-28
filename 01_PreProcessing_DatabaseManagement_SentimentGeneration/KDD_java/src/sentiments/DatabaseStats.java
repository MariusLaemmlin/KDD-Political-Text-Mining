package sentiments;

import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * Erstellen von MongoDB Statistiken. Beispiel: Zaehle alle Eintraege mit Sentiments und Eintraege ohne.
 * 
 * @author Marius Laemmlin
 * @version 2017-06-27
 * 
 */
public class DatabaseStats {

	public static void main(String[] args) {
		int count = 0;
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("news");
		MongoCollection<Document> col;
		FindIterable<Document> cursor;

		for(int i = 1; i <= 15; i++) {
			col = db.getCollection((i<10 ? "news_0" : "news_") + i);
			cursor = col.find().filter(Filters.exists("sentiment", false));
			for(Document doc : cursor) {
				count++;
			}
		}
		System.out.println(count);
	}

}

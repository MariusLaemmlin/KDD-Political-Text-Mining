package analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;

import entities.EntitySynonymsToMongo;

/**
 * Klasse zum Finden von divergenten SentimentScores zu einer Entität innerhalb desselben Textes
 * 
 * @author Marius Laemmlin
 * 
 */
public class SentimentAnalyzer {

	public SentimentAnalyzer() {
	}

	public static void main(String[] args) throws IOException {
		
		String databaseName = "news";

		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase(databaseName);
		PrintWriter pw;
		pw = new PrintWriter(
				new FileWriter(
						new File("C:/Users/Local/Documents/Universität/KDD Seminar/tests/findpositivenegative.txt")),
				true);
		long count = 0;

		
		Bson filter = Filters.exists("sentiment");
		
		
		String sentimentText;
		String comparedText;
		
		MongoIterable<String> list = db.listCollectionNames();
		for(String col : list) {
			MongoCollection<Document> mCol = db.getCollection(col);
			FindIterable<Document> cursor = mCol.find(filter);

			
			
			for(Document doc : cursor) {
				Document[] sentiments = ((ArrayList<Document>) doc.get("sentiment")).toArray(new Document[0]);
				
				for(int i = 0; i < sentiments.length; i++) {
					Document sentiment = sentiments[i];
					sentimentText = sentiment.getString("text");
					if(sentimentText == null) System.out.println(doc.getString("_id"));
					
					for(int j = 0; j < sentiments.length; j++) {
						Document compared = sentiments[j];
						comparedText = compared.getString("text");
						if(i != j && sentimentText.equals(comparedText)) {
							double sentimentScore = sentiment.getDouble("sentimentscore");
							double comparedScore= compared.getDouble("sentimentscore");
							if(sentimentScore < -0.6 && comparedScore > 0.6) {
								count++;
								System.out.println(doc.getString("_id") + ": " + sentimentScore + " / " + comparedScore + " | Text: " + sentimentText + " | Col: " + col);
								
							}
						}
					}
					
					
					
				}
				
			}
			
		}
		System.out.println("Count: " + count);
		pw.close();
		System.out.println("Done.");
	}
	
}

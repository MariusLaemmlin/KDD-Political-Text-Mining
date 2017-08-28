package analysis;

import org.bson.Document;
import java.util.*;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;

import java.io.*;

/**
 * Klasse zur Sentiment Bewertung mit Hilfe von Stanford NLP
 * 
 * @author Marius Laemmlin
 * 
 */
public class NLPToMongo implements Runnable {
	
	private static final boolean fileContainsDate = true;

	public static void main(String[] args) {
		new Thread(new NLPToMongo()).start();
	}

	@Override
	public void run() {
		try {
			String fileName = "C:/Users/Local/Documents/Universität/KDD Seminar/big7-content/content.txt";
			String databaseName = "news_big7";
			MongoClient mC = new MongoClient();
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			MongoDatabase db = mC.getDatabase(databaseName);
			MongoDatabase dbWrite = mC.getDatabase(databaseName + "_sents");
//			 dbWrite.drop();
			MongoIterable<String> cols = db.listCollectionNames();
			MongoCollection<Document> col;
			MongoCollection<Document> colWrite;
			SimpleNLP.init();
			Calendar start = Calendar.getInstance();
			String read;
			colWrite = dbWrite.getCollection("sentiments");
			br.readLine();
			StringTokenizer st;
			int count = 0;
			
			// Für Unterbrechungen
			for(int i = 1; i < 3330; i++) {
				br.readLine();
			}
			
			while ((read = br.readLine()) != null) {
				if (count % 1000 == 0) {
					System.out.println(count);
				}
				count++;
				st = new StringTokenizer(read, ";");
				String id = st.nextToken();
				if(fileContainsDate) st.nextToken();
				String content = st.nextToken();
				Date date = null;
				
				// Filter \t und \n
				content = content.replace("\\t", "");
				content = content.replace("\\n", "");
				content = content.replace("\\xa0BST", "");
				content = content.replace("â€™", "'");
				double sentimentScore = SimpleNLP.findSentiment(content);
				System.out.println(sentimentScore + " " + id + content);
				
				for (String colName : cols) {
					col = db.getCollection(colName);
					FindIterable<Document> cursor = col.find(new Document("_id", id))
							.projection(Projections.include("date"));
					for (Document x : cursor) {
						date = x.getDate("date");
					}
				}
				Document document = new Document();
				document.append("_id", id);
				document.append("date", date);
				document.append("sentiment-score", sentimentScore);
				try {
					colWrite.insertOne(document);
				}
				catch (com.mongodb.MongoWriteException e) {
					System.err.println("Ignore duplicate!");
				}

			}
		} 

		catch (IOException e) {
			e.printStackTrace();
		}
	}

}

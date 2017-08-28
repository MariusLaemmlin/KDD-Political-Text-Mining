package validation;

import java.io.*;
import java.util.*;

import org.bson.*;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class ValidateNewsExport {

	public static void main(String[] args) throws IOException {

		final String PATH = "C:/Users/Local/Documents/Universität/KDD Seminar/validate/";
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("news");
		 PrintWriter contentWriter = new PrintWriter(new FileWriter(PATH
		 + "news-content.txt"));

		String col;
		for (int i = 1; i <= 15; i++) {
			if (i < 10) {
				col = "news_0" + i;
			} else {
				col = "news_" + i;
			}

			MongoCollection<Document> mCol = db.getCollection(col);
			AggregateIterable<Document> cursor = mCol.aggregate(Arrays.asList(new Document("$sample", new Document("size", 50))));
//			FindIterable<Document> cursor = mCol.find();

			for (Document x : cursor) {
				
				String _id = x.getString("_id");
				String content = x.getString("content");
				ArrayList<Document> sentiment = (ArrayList<Document>) x.get("sentiment");
				if(sentiment!=null) {
					addContent(contentWriter, _id, sentiment, content);
				}
			}

		}
		System.out.println("Done.");

	}

	public static void addContent(PrintWriter writer, String _id, ArrayList<Document> sentiment, String content) throws IOException {
		for(Document x: sentiment) {
			if(x.getString("orientation") != null) {
				writer.println(_id + "|" + x.getString("text") + "|" + x.getString("orientation") + "|" + content);
			}
			
		}
	}

}

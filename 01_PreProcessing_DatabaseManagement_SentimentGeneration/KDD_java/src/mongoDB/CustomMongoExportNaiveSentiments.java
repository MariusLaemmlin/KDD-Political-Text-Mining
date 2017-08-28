package mongoDB;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class CustomMongoExportNaiveSentiments {

	public static void main(String[] args) throws IOException {
		String databaseName = "news_big7_sents";

		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase(databaseName);
		MongoCollection<Document> col = db.getCollection("sentiments");
		PrintWriter pw = new PrintWriter(new FileWriter("C:/Users/Local/Documents/Universität/KDD Seminar/exports/naive-sents/export.txt"));
		Bson filter = Filters.exists("sentiment-score");
		
		FindIterable<Document> cursor = col.find(filter);
		
		for(Document x : cursor) {
			Date date = x.getDate("date");
			int year = date.getYear()+1900;
			int month = date.getMonth()+1;
			int day = date.getDate();
			int hour = date.getHours();
			int minute = date.getMinutes();
			int second = date.getSeconds();
			if(year == 2016) {
				pw.println(x.getString("_id") + ";" + year + "-0" + month + "-" + (day<10 ? "0" : "") + day + " " + (hour<10?"0":"") + hour + ":" + (minute<10?"0":"") + minute + ":" + (second<10?"0":"") + second + ";" + x.getDouble("sentiment-score"));
			}
		}
		
		
	}
}

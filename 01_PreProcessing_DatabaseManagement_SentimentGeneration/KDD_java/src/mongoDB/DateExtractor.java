package mongoDB;

import java.util.ArrayList;
import java.util.Date;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import frequencies.DateCount;

public class DateExtractor {

	public static void main(String[] args) {
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("social_test_sentiments");

		ArrayList<DateCount> datelist = new ArrayList<>();

		String col;
		for (int i = 1; i <= 50; i++) {
			if (i < 10) {
				col = "social_0" + i;
			} else {
				col = "social_" + i;
			}

			Document filter = new Document("_id", "<http://xlime/0332933f-f9b2-348b-8edb-0f920968a1b7>");
			MongoCollection<Document> mCol = db.getCollection(col);
			Bson proj = Projections.include("date");
			FindIterable<Document> cursor = mCol.find().projection(proj);

			for (Document x : cursor) {
				Date d = x.getDate("date");
				Date current;
				current = x.getDate("date");
				boolean found = false;
				for (DateCount date : datelist) {
					if (date.date.getYear() == current.getYear() && date.date.getMonth() == current.getMonth()
							&& date.date.getDate() == current.getDate()) {
						date.count++;
						found = true;
					}
				}
				if (found == false) {
					datelist.add(new DateCount(current));
				}
			}
		}
		for (DateCount x : datelist) {
			System.out.print(x.date.getDate() + "." + (x.date.getMonth()+1) + "." + (x.date.getYear() +1900));
			System.out.println(": " + x.count);
		}
	}

}

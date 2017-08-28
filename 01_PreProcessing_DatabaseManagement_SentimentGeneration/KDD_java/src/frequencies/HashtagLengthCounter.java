package frequencies;

import java.util.ArrayList;
import java.util.Date;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;

/**
 * Hilfsklasse, um den Social Media Post mit den meisten Hashtags zu ermitteln 
 * 
 * @author Marius Laemmlin
 * 
 */
public class HashtagLengthCounter {
	
	// <http://xlime/4e494f8a-d3c2-3ec1-ab01-b8344dda3c0c>

	public static void main(String[] args) {
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("social");

		ArrayList<DateCount> datelist = new ArrayList<>();
		int maxlength = 0;
		String maxid ="";
		String col;
		for (int i = 1; i <= 50; i++) {
			if (i < 10) {
				col = "social_0" + i;
			} else {
				col = "social_" + i;
			}

			Document filter = new Document("_id", "<http://xlime/4e494f8a-d3c2-3ec1-ab01-b8344dda3c0c>");
			MongoCollection<Document> mCol = db.getCollection(col);
			Bson proj = Projections.include("_id", "hashtags");
			FindIterable<Document> cursor = mCol.find(filter);//.projection(proj);
			
			
			for (Document x : cursor) {
				System.out.println(x);
				ArrayList<String> list = (ArrayList<String>)x.get("hashtags");
				for(String s : list) {
					System.out.println(s);
					
				}
				System.out.println(x.getString("content"));
				
				if(list != null && list.size() > maxlength) {
					maxlength = list.size();
					maxid = x.getString("_id");
				}
			}
		}
		System.out.println(maxlength);
		System.out.println(maxid);
	}

}

package validation;

import java.util.*;
import java.io.*;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

public class FindValidationObsMongo {

	public static void main(String[] args) throws IOException {

		String databaseName = "news";

		BufferedReader br = new BufferedReader(
				new FileReader("C:/Users/Local/Documents/Universität/KDD Seminar/tests/id_entity_news.txt"));

		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase(databaseName);
		PrintWriter pw;
		pw = new PrintWriter(
				new FileWriter(
						new File("C:/Users/Local/Documents/Universität/KDD Seminar/tests/standardqueryexport.txt")),
				true);

		long count = 0;

		Bson filter;// = Filters.and(Filters.regex("publisher", new
					// BufferedReader(new
					// InputStreamReader(System.in)).readLine(),"i"));
		Calendar c = Calendar.getInstance();
		MongoIterable<String> colNames = db.listCollectionNames();
		String s;
		String searchID, searchEnt;
		pw.println("id; entity; sentiment-score");
		while ((s = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(s, ";");
			searchID = st.nextToken();
			searchEnt = st.nextToken();
			
			filter = new Document("_id", searchID);
			for (String col : colNames) {

				
				MongoCollection<Document> mCol = db.getCollection(col);
				FindIterable<Document> cursor = mCol.find(filter);

				for (Document x : cursor) {
					count++;
					if(!x.getString("_id").equals(searchID)) System.out.println("Error: ID different!");
					
					pw.print(x.getString("_id") + ";" + searchEnt + ";");


					 ArrayList<Document> list = (ArrayList<Document>) x.get("sentiment");
					 for(Document y : list) {
						 if(y.getString("text").equals(searchEnt)) {
							 pw.println(y.getDouble("sentimentscore"));
							 break;
						 }
					 }
					 
				}
			}
		}
		System.out.println("Count: " + count);
		pw.close();
		System.out.println("Done.");
		System.out.println("Time: " + (Calendar.getInstance().getTimeInMillis() - c.getTimeInMillis()));
	}

}

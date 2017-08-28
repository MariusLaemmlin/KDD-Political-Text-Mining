package sentiments;

import java.io.*;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import javax.json.*;

public class SentimentsToMongoGeneral implements Runnable {

	private File filepath;
	private static int count;
	private static int acknowledgedcount;
	private static int modifiedcount;
	private static int multiplecount;

	public static void main(String[] args) {
		File folderpath = new File("C:/Users/Local/Documents/Universität/KDD Seminar/sentiment-json-big7-general");
		ArrayList<Thread> threads = new ArrayList<>();
		for (File x : folderpath.listFiles()) {
			threads.add(new Thread(new SentimentsToMongoGeneral(x)));
		}
		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Add: " + count);
		System.out.println("Acknowledged: " + acknowledgedcount);
		System.out.println("Modified: " + modifiedcount);
		System.out.println("Multiple: " + multiplecount);
	}

	public SentimentsToMongoGeneral(File f) {
		this.filepath = f;
	}

	private synchronized void addCount(int count, int acknowledgedcount, int modifiedcount, int multiplecount) {
		SentimentsToMongoGeneral.count = SentimentsToMongoGeneral.count + count;
		SentimentsToMongoGeneral.acknowledgedcount = SentimentsToMongoGeneral.acknowledgedcount + acknowledgedcount;
		SentimentsToMongoGeneral.modifiedcount = SentimentsToMongoGeneral.modifiedcount + modifiedcount;
		SentimentsToMongoGeneral.multiplecount = SentimentsToMongoGeneral.multiplecount + multiplecount;
	}

	public void run() {
		int count = 0;
		int acknowledgedcount = 0;
		int modifiedcount = 0;
		int multiplecount = 0;
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("news_big7");
		JsonReader jsonReader = null;
		ArrayList<JsonObject> sentiments = new ArrayList<>();
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filepath));
			JsonObject jsonObject;
			String jsonString = "", s;
			while ((s = fileReader.readLine()) != null) {
				if (s.equals("}{")) {
					jsonString += "}";
					jsonReader = Json.createReader(new StringReader(jsonString));
					jsonObject = jsonReader.readObject();
					sentiments.add(jsonObject);
					System.out.println(jsonObject);
					jsonString = "{";
				} else {
					jsonString += s;
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Start: " + filepath);
		boolean searching;
		int collectionNo;
		MongoCollection<Document> col;
		UpdateResult result;

		for (int i = 0; i < sentiments.size(); i++) {
			searching = true;
			collectionNo = 1;
			while (searching) {
				if (collectionNo > 5) {
					System.out.println("Entry Not Found!");
					searching = false;
					break;
				}
				JsonObject currentSentiment = sentiments.get(i);
				col = db.getCollection("news_big7");
				FindIterable<Document> cursor = col.find(new Document("_id", currentSentiment.getString("id")));
				if (cursor.first() != null) {
					// System.out.println("Entry Found in Collection " +
					// collectionNo);
					Document add = new Document();// "sentiment-language",
													// currentSentiment.getLanguage());
					if (currentSentiment != null && currentSentiment.getJsonObject("sentiment").getJsonObject("document") != null) {
						add = new Document("general-sentiment",
								currentSentiment.getJsonObject("sentiment").getJsonObject("document").getJsonNumber("score").doubleValue());
						count++;

						Bson filter = Filters.and(Filters.exists("general-sentiment"),
								new Document("_id", currentSentiment.getString("id")));
						if (col.find(filter).first() != null) {
							multiplecount++;
							System.out.println(
									col.find(filter).first().get("_id") + " - File:" + this.filepath.getName());

						}
						result = col.updateOne(new Document("_id", currentSentiment.getString("id")),
								new Document("$set", add));
						if (result.wasAcknowledged())
							acknowledgedcount++;
						if (result.isModifiedCountAvailable())
							modifiedcount++;
					}
					searching = false;
				}

				collectionNo++;
			}
		}
		addCount(count, acknowledgedcount, modifiedcount, multiplecount);
		mongoClient.close();
	}

}

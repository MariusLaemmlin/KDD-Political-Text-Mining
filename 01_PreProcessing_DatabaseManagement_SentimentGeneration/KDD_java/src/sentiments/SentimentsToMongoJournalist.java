package sentiments;

import java.io.File;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

public class SentimentsToMongoJournalist implements Runnable {

	private File filepath;
	private static int count;
	private static int acknowledgedcount;
	private static int modifiedcount;
	private static int multiplecount;
	

	public static void main(String[] args) {
		File folderpath = new File("C:/Users/Local/Documents/Universität/KDD Seminar/sentiment-json-journalist");
		ArrayList<Thread> threads = new ArrayList<>();
		for (File x : folderpath.listFiles()) {
			threads.add(new Thread(new SentimentsToMongoJournalist(x)));
		}
		for(Thread t : threads) {
			t.start();
		}
		
		for(Thread t: threads) {
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

	public SentimentsToMongoJournalist(File f) {
		this.filepath = f;
	}
	
	private synchronized void addCount(int count, int acknowledgedcount, int modifiedcount, int multiplecount) {
		SentimentsToMongoJournalist.count = SentimentsToMongoJournalist.count + count;
		SentimentsToMongoJournalist.acknowledgedcount = SentimentsToMongoJournalist.acknowledgedcount + acknowledgedcount;
		SentimentsToMongoJournalist.modifiedcount = SentimentsToMongoJournalist.modifiedcount + modifiedcount;
		SentimentsToMongoJournalist.multiplecount = SentimentsToMongoJournalist.multiplecount + multiplecount;
	}

	public void run() {
		int count = 0;
		int acknowledgedcount = 0;
		int modifiedcount = 0;
		int multiplecount = 0;
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("journalist");

		ArrayList<Sentiment> sentiments = JsonSentimentReader.readSentiments(filepath, true);
		System.out.println("Start: " + filepath);
		boolean searching;
		int collectionNo;
		MongoCollection<Document> col;
		UpdateResult result;

		for (int i = 0; i < sentiments.size(); i++) {
			searching = true;
			collectionNo = 1;
			while (searching) {
				if (collectionNo > 1) {
					System.out.println("Entry Not Found!");
					searching = false;
					break;
				}
				Sentiment currentSentiment = sentiments.get(i);
				col = db.getCollection("journalist");
				FindIterable<Document> cursor = col.find(new Document("_id", currentSentiment.getId()));
				if (cursor.first() != null) {
					// System.out.println("Entry Found in Collection " +
					// collectionNo);
					Document add = new Document();// "sentiment-language",
													// currentSentiment.getLanguage());
					ArrayList<Document> entities = new ArrayList<>();
					Document entity;
					if (currentSentiment.getEntities() != null) {
						for (Entity x : currentSentiment.getEntities()) {
							entity = new Document();
							entity.append("type", x.getType());
							entity.append("text", x.getText());
							entity.append("sentimentscore", x.getSentiment_score());
							entity.append("relevance", x.getRelevance());
							entity.append("name", x.getName());
							entity.append("dbpedia_resource", x.getDbPedia());
							entity.append("count", x.getCount());
							entities.add(entity);
						}

					}

					count++;
					add.append("sentiment", entities);

					Bson filter = Filters.and(Filters.exists("sentiment"),
							new Document("_id", currentSentiment.getId()));
					if (col.find(filter).first() != null) {
						multiplecount++;
						System.out.println(col.find(filter).first().get("_id"));

					}
					result = col.updateOne(new Document("_id", currentSentiment.getId()), new Document("$set", add));
					if (result.wasAcknowledged())
						acknowledgedcount++;
					if (result.isModifiedCountAvailable())
						modifiedcount++;

					searching = false;
				}

				collectionNo++;
			}
		}
		addCount(count, acknowledgedcount, modifiedcount, multiplecount);
		mongoClient.close();
	}

}

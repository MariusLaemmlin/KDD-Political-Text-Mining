package sentiments;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

public class SentimentsToMongoNews implements Runnable {

	private File filepath;
	private static int count;
	private static int notfound;
	private static int acknowledgedcount;
	private static int modifiedcount;
	private static int multiplecount;
	private static File folderpath = new File("C:/Users/Local/Documents/Universität/KDD Seminar/sentiment-json-big7");
	private static String databaseName = "news_big7";
	

	public static void main(String[] args) {
		ArrayList<Thread> threads = new ArrayList<>();
		for (File x : folderpath.listFiles()) {
			threads.add(new Thread(new SentimentsToMongoNews(x)));
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
		System.out.println("Not found: " + notfound);
		System.out.println("Acknowledged: " + acknowledgedcount);
		System.out.println("Modified: " + modifiedcount);
		System.out.println("Multiple: " + multiplecount);
	}

	public SentimentsToMongoNews(File f) {
		this.filepath = f;
	}
	
	private synchronized void addCount(int count, int notfound, int acknowledgedcount, int modifiedcount, int multiplecount) {
		SentimentsToMongoNews.count = SentimentsToMongoNews.count + count;
		SentimentsToMongoNews.notfound = SentimentsToMongoNews.notfound + notfound;
		SentimentsToMongoNews.acknowledgedcount = SentimentsToMongoNews.acknowledgedcount + acknowledgedcount;
		SentimentsToMongoNews.modifiedcount = SentimentsToMongoNews.modifiedcount + modifiedcount;
		SentimentsToMongoNews.multiplecount = SentimentsToMongoNews.multiplecount + multiplecount;
	}

	public void run() {
		int count = 0;
		int acknowledgedcount = 0;
		int modifiedcount = 0;
		int multiplecount = 0;
		int notfound = 0;
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(databaseName);

		ArrayList<Sentiment> sentiments = JsonSentimentReader.readSentiments(filepath, true);
		System.out.println("Start: " + filepath);
		boolean searching;
		int collectionNo;
		MongoCollection<Document> col;
		UpdateResult result;

		for (int i = 0; i < sentiments.size(); i++) {
			searching = true;
			collectionNo = 1;
			Iterator<String> colNames = db.listCollectionNames().iterator();
			while (searching) {
				if (!colNames.hasNext()) {
					System.out.println("Entry Not Found!");
					searching = false;
					notfound++;
					break;
				}
				Sentiment currentSentiment = sentiments.get(i);
				col = db.getCollection(colNames.next());
//				col = db.getCollection((collectionNo < 10) ? "news_0" + collectionNo : "news_" + collectionNo);
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
		addCount(count, notfound, acknowledgedcount, modifiedcount, multiplecount);
		System.out.println();
		mongoClient.close();
	}

}

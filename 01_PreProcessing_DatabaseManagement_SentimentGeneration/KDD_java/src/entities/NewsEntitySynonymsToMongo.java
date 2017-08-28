package entities;

import java.io.File;
import java.util.ArrayList;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

/**
 * Klasse, um eine News Konfiguration einer EntitySynonymsToMongo Klasse zu erstellen
 * 
 * @author Marius Laemmlin
 * 
 */
public class NewsEntitySynonymsToMongo {

	public static void main(String[] args) {
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("journalist");
		EntitySynonymsToMongo.setLimit(1000);
		
		
		MongoIterable<String> list = db.listCollectionNames();
		File file = new File("C:/Users/Local/Documents/Universität/KDD Seminar/entitySyn/Entitäten-Liste.csv");
		ArrayList<Thread> threads = new ArrayList<>();
		for(String s : list) {
			Thread thread = new Thread(new EntitySynonymsToMongo(db.getCollection(s), file));
			thread.start();
			threads.add(thread);
		}
		for(Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Additions: " + EntitySynonymsToMongo.getCount());
		
		
		
	}

}

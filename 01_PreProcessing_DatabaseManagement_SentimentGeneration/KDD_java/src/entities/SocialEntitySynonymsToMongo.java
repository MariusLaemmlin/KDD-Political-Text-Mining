package entities;

import java.io.File;
import java.util.ArrayList;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

/**
 * Klasse, um eine Social Konfiguration einer EntitySynonymsToMongo Klasse zu erstellen
 * 
 * @author Marius Laemmlin
 * 
 */
public class SocialEntitySynonymsToMongo {

	public static void main(String[] args) {
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("social");
		MongoIterable<String> list = db.listCollectionNames();

		EntitySynonymsToMongo.setLimit(400);

		File file = new File("C:/Users/Local/Documents/Universität/KDD Seminar/entitySyn/Entitäte_Twitter.csv");
		ArrayList<Thread> threads = new ArrayList<>();

		// TODO: Künstliches Beschränken der Threads
		int i = 0;
		for (String s : list) {
			mC = new MongoClient();
			db = mC.getDatabase("social");
			EntitySynonymsToMongo estm = new EntitySynonymsToMongo(db.getCollection(s), file);
			Thread thread = new Thread(estm);
			thread.start();
			estm.setThreadName(thread.getName());
			threads.add(thread);
		}
		for (Thread thread : threads) {
			try {
				thread.join();
				System.out.println(thread.getName() + " finished!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Additions: " + EntitySynonymsToMongo.getCount());

		mC.close();

	}

}

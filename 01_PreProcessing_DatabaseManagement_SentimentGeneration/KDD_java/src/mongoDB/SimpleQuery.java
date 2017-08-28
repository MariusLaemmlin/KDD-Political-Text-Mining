package mongoDB;

import java.util.*;

import org.bson.*;
import org.bson.conversions.Bson;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;

public class SimpleQuery {

	private MongoClient mongoClient;
	private MongoDatabase db;
	private String saveToPath;
	private String[] collections;
	private Bson filter;
	private Bson projection;
	private OutputType outputType;
	
	public SimpleQuery(String saveToPath, int[] collections, String[] keys, Bson filter,
			OutputType outputType) {
		super();
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase("social");
		this.saveToPath = saveToPath;
		this.filter = filter;
		this.outputType = outputType;
		this.collections = getCollectionNames(collections);
		this.projection = getProjections(keys);
		run();
	}
	
	private long getCount() {
		long result = 0;
		for(String collectionName : collections) {
				MongoCollection<Document> col = db.getCollection(collectionName);
				FindIterable<Document> cursor;
				if(filter == null) {
					cursor = col.find();
				} else {
					cursor = col.find(filter);
				}
				for(Document x : cursor) {
					result++;
				}
		}
		return result;
	}
	
	private void print() {
		for(String collectionName : collections) {
			MongoCollection<Document> col = db.getCollection(collectionName);
			FindIterable<Document> cursor = col.find(filter).projection(projection);
			for(Document x : cursor) {
				System.out.println(x);
			}
		}
	}
	
	private void save() {
		System.out.println("*** UNDER DEVELOPMENT ***");
	}
	
	private String[] getCollectionNames(int[] collections) {
		String[] list;
		if(collections == null) {
			list = new String[50];
			for(int i = 0; i < 50; i++) {
				list[i] = (i<10) ? "social_0" + i : "social_" + i;
			}
			
		} else {
			list = new String[collections.length];
			for(int i = 0; i < collections.length; i++) {
				list[i] = (i<10) ? "social_0" + i : "social_" + i;
			}
		}
		return list;
	}
	
	private Bson getProjections(String[] keys) {
		return (keys==null) ? null : Projections.include(keys);
	}


	public void run() {
		switch(outputType) {
		case COUNT:
			System.out.println(getCount());
			break;
		case PRINT:
			print();
			break;
		case SAVE:
			save();
			break;
		default: break;
		}
	}
	
	
	
	
}

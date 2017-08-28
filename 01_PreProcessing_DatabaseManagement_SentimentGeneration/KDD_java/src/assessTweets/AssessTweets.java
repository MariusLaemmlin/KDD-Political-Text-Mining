package assessTweets;

import java.io.*;
import java.util.*;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.*;
import com.mongodb.client.*;

/**
 * Klasse zum Bewerten von Social Daten auf Basis des Inhalts. 
 * 
 * @author Marius Laemmlin, Steffen Roehrig
 * 
 */
public class AssessTweets {

	/*
	 * drei Kategorien 1. Politiker und parteien 2. Aussagekräftige Entitäten 3.
	 * Institutionen und kleinere Entitäten
	 */

	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
//		File zielFile = new File("C:/Users/Local/Documents/Universität/KDD Seminar/tests/tweet_bewertungen.txt");

		String hoch[] = { "brexit", "remain", "leav", "johnson", "borisjohnson", "farage", "nigel_farage", "gove",
				"michaelgove", "cameron", "corbyn", "ukip", "tory", "labour", "osborne", "theresa may", "cox", "jo_cox1"};

		String mittel[] = { "eu breakup", "eu regulations", "eu spending", "immigrat", "migrant",
				"housing", "independence day", "sovereignty", "nhs", "single market" };

		String klein[] = { "pound", "crime", "euro", "eurocrats", "investment", "job", "public service",
				"negotiation", "security", "tv debate" };

//		FileWriter as;
		try {
//			as = new FileWriter(zielFile);
//			PrintWriter pw = new PrintWriter(as);
			MongoClient mC = new MongoClient();
			MongoDatabase db = mC.getDatabase("social");
			MongoCollection<Document> col;
			Document doc;
			int anzahl = 0;
			
//			pw.println("_id; valueScore");

			MongoIterable<String> toppings = db.listCollectionNames(); // gibt alle Collections in einer liste zurück über die dann iteriert werden kann
			for (String s : toppings) {
				System.out.println(s);

				col = db.getCollection(s);

				FindIterable<Document> findIterable = col.find();

				for (Document doc2 : findIterable) {
					int hashtagCount = 0;
					if(doc2.get("hashtags") != null)
						hashtagCount = ((ArrayList<Document>)(doc2.get("hashtags"))).size();
					double bewertung = assessTweet(doc2.get("content").toString(), hoch, mittel, klein, hashtagCount);
					writeResult((String) doc2.get("_id"), bewertung, col);
					anzahl++;
				}

			}
			System.out.println("Die Anzahl der geschreibenen Zeilen beträgt: " + anzahl);
			System.out.println("Benötigte Zeit: " + ((Calendar.getInstance().getTimeInMillis() - c.getTimeInMillis())/1000) + "s" );

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double hashtagPunitiveFunction(int hashtagCount) {
		return 0.01 * Math.pow(hashtagCount, 2);
	}

	public static double assessTweet(String content, String[] hoch, String[] mittel, String[] klein, int hashtagCount) {

		Pattern p = null;
		Matcher m = null;

		Double wert = 0.0;

		if (content.length() > 40) {
			wert = wert + 5;
		}

		for (String test : hoch) {

			p = Pattern.compile("." + test + "\b* *");
			m = p.matcher(content.toLowerCase());

			if (m.find()) {
				wert = wert + 3;
			}
		}

		for (String test : mittel) {
			p = Pattern.compile("." + test + "\b* *");
			m = p.matcher(content.toLowerCase());
			if (m.find()) {
				wert = wert + 2;
			}
			
		}

		for (String test : klein) {
			p = Pattern.compile("." + test + "\b* *");
			m = p.matcher(content.toLowerCase());
			if (m.find()) {
				wert = wert + 1;
			}
			
		}
		
		// Strafterm fuer viele Hashtags
		wert = wert - hashtagPunitiveFunction(hashtagCount);
		
		return wert;

	}

	public static void writeResult(String id, Double value, MongoCollection<Document> col) throws IOException {
		Document add = new Document("rating", value);
		col.updateOne(new Document("_id", id), new Document("$set", add));
	}
}

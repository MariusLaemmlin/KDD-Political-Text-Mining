package mongoDB;

import java.io.*;
import java.util.*;

import org.bson.*;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;

public class CustomMongoExportGeneral {

	public static void main(String[] args) throws IOException {

		final String PATH = "C:/Users/Local/Documents/Universität/KDD Seminar/exports/sentiments/general/";
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("social");
		// BufferedWriter hashtagWriter = new BufferedWriter(new FileWriter(PATH
		// + "hashtags.txt"));
		// BufferedWriter entityWriter = new BufferedWriter(new FileWriter(PATH
		// + "entities.txt"));
		// BufferedWriter categoryWriter = new BufferedWriter(new
		// FileWriter(PATH + "categories.txt"));
		BufferedWriter sentimentWriter = new BufferedWriter(new FileWriter(PATH + "sentiments.txt"));
		BufferedWriter noZeroSentimentWriter = new BufferedWriter(new FileWriter(PATH + "sentiments-exclude-zeros.txt"));
		// BufferedWriter contentWriter = new BufferedWriter(new FileWriter(PATH
		// + "content.txt"));

		// __id,date,language,publisher,hashtags,creator,content

		// Ueberschriften
		// entityWriter.write("_id; date; entity; confidence");
		// entityWriter.newLine();
		// categoryWriter.write("_id; date; category; confidence");
		// categoryWriter.newLine();
		sentimentWriter.write("_id; date; general-sentiment");
		sentimentWriter.newLine();
		noZeroSentimentWriter.write("_id; date; general-sentiment");
		noZeroSentimentWriter.newLine();
		// hashtagWriter.write("_id; date; hashtag");
		// hashtagWriter.newLine();
		// contentWriter.write("_id; content");
		// contentWriter.newLine();

		MongoIterable<String> colNames = db.listCollectionNames();
		for(String col : colNames) {
			

			/*
			 * Auskommentieren, wenn nur eine Datei erstellt werden soll /
			 * hashtagWriter = new BufferedWriter(new FileWriter(PATH +
			 * "hashtags_" + i + ".txt")); entityWriter = new BufferedWriter(new
			 * FileWriter(PATH + "entities_" + i + ".txt")); categoryWriter =
			 * new BufferedWriter(new FileWriter(PATH + "categories_" + i +
			 * ".txt")); sentimentWriter = new BufferedWriter(new
			 * FileWriter(PATH + "sentiments_" + i + ".txt"));
			 * 
			 * entityWriter.write("_id; date; entity; confidence");
			 * entityWriter.newLine();
			 * categoryWriter.write("_id; date; category; confidence");
			 * categoryWriter.newLine(); sentimentWriter.
			 * write("_id; date; type; text; sentimentscore; relevance; count");
			 * sentimentWriter.newLine();
			 * hashtagWriter.write("_id; date; hashtag");
			 * hashtagWriter.newLine();
			 */

			MongoCollection<Document> mCol = db.getCollection(col);

			FindIterable<Document> cursor = mCol.find();

			for (Document x : cursor) {
				String _id = x.getString("_id");
				Date date = x.getDate("date");
				String content = x.getString("content");
				boolean isRetweet = x.getBoolean("is_retweet", false);
				// ArrayList<String> hashtags = (ArrayList<String>)
				// x.get("hashtags");
				// ArrayList<Document> entities = (ArrayList<Document>)
				// x.get("entities");
				// ArrayList<Document> categories = (ArrayList<Document>)
				// x.get("categories");
				ArrayList<Document> sentiment = (ArrayList<Document>) x.get("sentiment");
				double gensent = (x.getDouble("general-sentiment") != null) ? x.getDouble("general-sentiment") : 0.0;

				// addHashtags(hashtagWriter, _id, date, hashtags);
				// addEntities(entityWriter, _id, date, entities);
				// addCategories(categoryWriter, _id, date, categories);
				addSentiments(sentimentWriter, _id, date, gensent, false);
				addSentiments(noZeroSentimentWriter, _id, date, gensent, true);
				// addContent(contentWriter, _id, content);
			}

		}
		// entityWriter.flush();
		// categoryWriter.flush();
		sentimentWriter.flush();
		noZeroSentimentWriter.flush();
		// hashtagWriter.flush();
		System.out.println("Done.");

	}

	public static String DateToString(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DATE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		return "" + year + "-" + (month < 10 ? "" + 0 + month : month) + "-" + (day < 10 ? "" + 0 + day : day) + " "
				+ (hour < 10 ? "" + 0 + hour : hour) + ":" + (minute < 10 ? "" + 0 + minute : minute) + ":"
				+ (second < 10 ? "" + 0 + second : second);
	}


	public static void addSentiments(BufferedWriter writer, String _id, Date date, double gensent, boolean suppressZeroSentiments)
			throws IOException {
		boolean add;
		add = true;
		// Unterdruecke 0.0 Sentimentscores
		if (suppressZeroSentiments && gensent == 0.0) {
			add = false;
		}
		if (add) {
			writer.write(_id + "; " 
					+ DateToString(date) + "; " 
					+ gensent);
			writer.newLine();
		}

		writer.flush();

	}


}

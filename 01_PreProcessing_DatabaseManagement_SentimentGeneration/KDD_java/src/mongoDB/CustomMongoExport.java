package mongoDB;

import java.io.*;
import java.util.*;

import org.bson.*;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class CustomMongoExport {

	public static void main(String[] args) throws IOException {

		final String PATH = "C:/Users/Local/Documents/Universität/KDD Seminar/exports/";
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
		sentimentWriter.write("_id; date; isRetweet; type; text; sentimentscore; relevance; count; orientation; politician");
		sentimentWriter.newLine();
		noZeroSentimentWriter.write("_id; date; isRetweet; type; text; sentimentscore; relevance; count; orientation; politician");
		noZeroSentimentWriter.newLine();
		// hashtagWriter.write("_id; date; hashtag");
		// hashtagWriter.newLine();
		// contentWriter.write("_id; content");
		// contentWriter.newLine();

		String col;
		for (int i = 1; i <= 50; i++) {
			if (i < 10) {
				col = "social_0" + i;
			} else {
				col = "social_" + i;
			}

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

				// addHashtags(hashtagWriter, _id, date, hashtags);
				// addEntities(entityWriter, _id, date, entities);
				// addCategories(categoryWriter, _id, date, categories);
				addSentiments(sentimentWriter, _id, date, isRetweet, sentiment, false);
				addSentiments(noZeroSentimentWriter, _id, date, isRetweet, sentiment, true);
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

	public static void addHashtags(BufferedWriter writer, String _id, Date date, ArrayList<String> hashtags)
			throws IOException {
		if (hashtags != null && !hashtags.isEmpty()) {
			for (String x : hashtags) {
				writer.write(_id + "; " + DateToString(date) + "; " + x);
				writer.newLine();
			}
			writer.flush();
		}
	}

	public static void addEntities(BufferedWriter writer, String _id, Date date, ArrayList<Document> entities)
			throws IOException {
		if (entities != null && !entities.isEmpty()) {
			for (Document x : entities) {
				writer.write(_id + "; " + DateToString(date) + "; " + x.getString("entity") + "; "
						+ x.getDouble("confidence"));
				writer.newLine();
			}
			writer.flush();
		}
	}

	public static void addCategories(BufferedWriter writer, String _id, Date date, ArrayList<Document> categories)
			throws IOException {
		if (categories != null && !categories.isEmpty()) {
			for (Document x : categories) {
				writer.write(_id + "; " + DateToString(date) + "; " + x.getString("category") + "; "
						+ x.getDouble("confidence"));
				writer.newLine();
			}
			writer.flush();
		}
	}

	public static void addSentiments(BufferedWriter writer, String _id, Date date, boolean isRetweet, ArrayList<Document> sentiments, boolean suppressZeroSentiments)
			throws IOException {
		boolean add;
		if (sentiments != null && !sentiments.isEmpty()) {
			for (Document x : sentiments) {

				add = true;
				// Unterdruecke 0.0 Sentimentscores
				if (suppressZeroSentiments && x.getDouble("sentimentscore") == 0.0) {
					add = false;
				}
				if (add) {
					writer.write(_id + "; " 
							+ DateToString(date) + "; " 
							+ isRetweet + "; "
							+ x.getString("type") + "; "
							+ x.getString("text") + "; " 
							+ x.getDouble("sentimentscore") + "; "
							+ x.getDouble("relevance") + "; " 
							+ x.getInteger("count") + "; ");
					if(x.containsKey("orientation")) {
						writer.write(x.getString("orientation") + "; "); 
					} else {
						writer.write("; ");
					}
					if(x.containsKey("politician")) {
						writer.write(x.getString("politician"));
					}
					writer.newLine();
				}
			}
			writer.flush();
		}
	}

	public static void addContent(BufferedWriter writer, String _id, String content) throws IOException {
		writer.write(_id + "; " + content);
		writer.newLine();
		writer.flush();
	}

}

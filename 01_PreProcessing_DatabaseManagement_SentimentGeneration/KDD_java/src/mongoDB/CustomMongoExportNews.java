package mongoDB;

import java.io.*;
import java.util.*;

import org.bson.*;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class CustomMongoExportNews {

	// SETTINGS:
	static boolean oneFile = true; // Alles in eine Datei legen
	static boolean suppressZeroSentiments = true; // 0.0 Sentimentscore ignorieren
	
	public static void main(String[] args) throws IOException {

		

		final String PATH = "C:/Users/Local/Documents/Universität/KDD Seminar/news-exports/";
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("news_extension");
		BufferedWriter entityWriter = new BufferedWriter(new FileWriter(PATH + "entities.txt"));
		BufferedWriter categoryWriter = new BufferedWriter(new FileWriter(PATH + "categories.txt"));
		BufferedWriter sentimentWriter = new BufferedWriter(new FileWriter(PATH + (suppressZeroSentiments ? "sentiments-exclude-zeros.txt" : "sentiments.txt")));
		PrintWriter contentWriter = new PrintWriter(new FileWriter(PATH + "content.txt"), true);

		entityWriter.write("_id; date; publisher; entity; confidence");
		entityWriter.newLine();
		categoryWriter.write("_id; date; publisher; category; confidence");
		categoryWriter.newLine();
		
		//TODO Add additional Parameters!
		sentimentWriter.write("_id; date; publisher; type; text; sentimentscore; relevance; count; orientation; politician");
		sentimentWriter.newLine();
		contentWriter.println("_id; date; content");
		
		MongoIterable<String> colNames = db.listCollectionNames();
		int i = 1;
		for(String col : colNames) {

			// Entferne Folgendes, wenn alles in eine Datei gelegt werden soll

			if (!oneFile) {

				entityWriter = new BufferedWriter(new FileWriter(PATH + "entities_" + i + ".txt"));
				categoryWriter = new BufferedWriter(new FileWriter(PATH + "categories_" + i + ".txt"));
				sentimentWriter = new BufferedWriter(new FileWriter(PATH + "sentiments_" + i + ".txt"));

				entityWriter.write("_id; date; entity; confidence");
				entityWriter.newLine();
				categoryWriter.write("_id; date; category; confidence");
				categoryWriter.newLine();
				// TODO: Deprecated
				sentimentWriter.write("_id; date; type; text; sentimentscore; relevance; count");
				sentimentWriter.newLine();
				i++;
			}

			// Document filter = new Document("_id",
			// "<http://xlime/0332933f-f9b2-348b-8edb-0f920968a1b7>");
			MongoCollection<Document> mCol = db.getCollection(col);
			FindIterable<Document> cursor = mCol.find();

			for (Document x : cursor) {
				String _id = x.getString("_id");
				Date date = x.getDate("date");
				String publisher = x.getString("publisher");
				String content = x.getString("content");
				ArrayList<Document> entities = (ArrayList<Document>) x.get("entities");
				ArrayList<Document> categories = (ArrayList<Document>) x.get("categories");
				ArrayList<Document> sentiment = (ArrayList<Document>) x.get("sentiment");

				addEntities(entityWriter, _id, date, publisher, entities);
				addCategories(categoryWriter, _id, date, publisher, categories);
				addSentiments(sentimentWriter, _id, date, publisher, sentiment);
				addContent(contentWriter, _id, date, content);

			}

		}
		entityWriter.flush();
		categoryWriter.flush();
		sentimentWriter.flush();
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

	public static void addEntities(BufferedWriter writer, String _id, Date date, String publisher,
			ArrayList<Document> entities) throws IOException {
		if (entities != null && !entities.isEmpty()) {
			for (Document x : entities) {
				writer.write(_id + "; " + DateToString(date) + "; " + publisher + "; " + x.getString("entity") + "; "
						+ x.getDouble("confidence"));
				writer.newLine();
			}
			writer.flush();
		}
	}

	public static void addCategories(BufferedWriter writer, String _id, Date date, String publisher,
			ArrayList<Document> categories) throws IOException {
		if (categories != null && !categories.isEmpty()) {
			for (Document x : categories) {
				writer.write(_id + "; " + DateToString(date) + "; " + publisher + "; " + x.getString("category") + "; "
						+ x.getDouble("confidence"));
				writer.newLine();
			}
			writer.flush();
		}
	}

	public static void addSentiments(BufferedWriter writer, String _id, Date date, String publisher,
			ArrayList<Document> sentiments) throws IOException {
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
							+ publisher + "; " 
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
	
	public static void addContent(PrintWriter writer, String _id, Date date, String content) {
		if(content != null && !content.equals("")) {
			int year = date.getYear()+1900;
			int month = date.getMonth()+1;
			int day = date.getDate();
			int hour = date.getHours();
			int minute = date.getMinutes();
			int second = date.getSeconds();
			if(year == 2016) {
				writer.print(_id + "; ");
				writer.print(year + "-0" + month + "-" + (day<10 ? "0" : "") + day + " " + (hour<10?"0":"") + hour + ":" + (minute<10?"0":"") + minute + ":" + (second<10?"0":"") + second + "; ");
				writer.println(content);
			}
		}
	}

}

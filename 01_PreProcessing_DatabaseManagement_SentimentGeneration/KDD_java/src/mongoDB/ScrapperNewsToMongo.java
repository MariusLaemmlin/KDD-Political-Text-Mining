package mongoDB;

import java.io.*;
import java.util.*;

import org.junit.Test;
import org.bson.*;

import com.mongodb.MongoClient;
import com.mongodb.client.*;

import org.junit.Assert;

public class ScrapperNewsToMongo {

	private static final String databaseName = "news_big7";
	private static final String inputFile = "C:/Users/Local/Documents/Universität/KDD Seminar/origins/news1_15_big7.txt";
	private static final String separator = ";";
	private static final boolean includesDate = true;
	private static final boolean dropDB = false;

	public static void main(String[] args) throws IOException {
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase(databaseName);
		if(dropDB) db.drop();
		MongoCollection<Document> col = db.getCollection(databaseName);
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		int count = 0;
		ArrayList<String> list;
		while ((list = readNextString(br)) != null) {
			if(includesDate) objectToMongoWithDate(createScrapperObjectWithDate(list), col);
			else objectToMongo(createScrapperObject(list), col);
			count++;
		}
		System.out.println(count + " Elements added!");
		mC.close();
		br.close();
	}

	public static ArrayList<String> readNextString(BufferedReader br) throws IOException {
		String s = br.readLine();
		if (s == null) {
			return null;
		}
		if (s.equals("")) {
			s = br.readLine();
			if (s == null) {
				return null;
			}
		}
		return divideString(s);
	}

	private static ArrayList<String> divideString(String s) {
		ArrayList<String> divided = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(s, separator);
		while (st.hasMoreTokens()) {
			divided.add(st.nextToken());
			if(divided.size()>4) {
				divided.set(3, divided.get(3) + divided.get(4));
				divided.remove(4);
			}
		}
		// Fehlerbehandlung
		if (divided.size() != 4) {
			System.err.println(
					"Error: To many elements in Array. Some parts of the text may get lost. Check input file! Nr.: "
							+ divided.size());
			divided.add("empty");
		}
		return divided;
	}

	public static ScrapperObject createScrapperObject(ArrayList<String> list) {
		return new ScrapperObject(list.get(0), list.get(1), list.get(2), list.get(3));
	}

	public static ScrapperObject createScrapperObjectWithDate(ArrayList<String> list) {
		Date d = getDateFromMDDYY(list.get(0));
		System.out.println(d);
		return new ScrapperObject(list.get(1), getDateFromMDDYY(list.get(0)), list.get(2), list.get(3));
	}

	public static void objectToMongo(ScrapperObject obj, MongoCollection<Document> col) {
		Document add = new Document();
		add.append("_id", obj.getId());
		add.append("publisher", obj.getPublisher());
		add.append("author", obj.getAuthor());
		add.append("content", obj.getText());
		col.insertOne(add);

	}

	public static void objectToMongoWithDate(ScrapperObject obj, MongoCollection<Document> col) {
		Document add = new Document();
		add.append("_id", obj.getId());
		add.append("date", obj.getDate());
		add.append("publisher", obj.getPublisher());
		add.append("content", obj.getText());
		try {
			col.insertOne(add);
		} catch(com.mongodb.MongoWriteException e) {
			System.err.println("MongoWriteExcpetion: Duplicate!");
		}

	}

	public static Date getDateFromMDDYY(String s) {
		int year = 0;
		int month = 0;
		int day = 0;
		if (s.length() == 5) {
			year = Integer.parseInt(s.substring(3, 5)) + 2000;
			month = Integer.parseInt(s.substring(0, 1)) - 1;
			day = Integer.parseInt(s.substring(1, 3));
		} else if (s.length() == 4) {
			year = Integer.parseInt(s.substring(2, 4)) + 2000;
			month = Integer.parseInt(s.substring(0, 1)) - 1;
			day = Integer.parseInt(s.substring(1, 2));
		} else {
			System.err.println("Error while parsing dates: " + year + month + day);
		}

		Calendar c = Calendar.getInstance();

		c.set(year, month, day, 0, 0, 0);
		Date date = new Date(c.getTimeInMillis());
		return date;
	}

}

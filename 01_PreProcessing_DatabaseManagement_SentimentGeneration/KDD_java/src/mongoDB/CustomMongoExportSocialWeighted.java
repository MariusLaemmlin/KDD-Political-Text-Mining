package mongoDB;

import java.io.*;
import java.util.*;

import org.bson.*;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class CustomMongoExportSocialWeighted {

	public static void main(String[] args) throws IOException, InterruptedException {

		final String PATH = "C:/Users/Local/Documents/Universität/KDD Seminar/exports/social rated date/";
		
		Calendar start = Calendar.getInstance();
		System.out.println("Beginning...");
//		SelectValuableSocials.getDailyLists("social");
//		System.out.println("Superlist created @ " + ((Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis())/1000.0));
//		ArrayList<Thread> threads = new ArrayList<>();
//		for(ArrayList<RatingDocument> list : superList) {
//			SelectValuableSocials s = new SelectValuableSocials(list);
//			threads.add(new Thread(s));
//		}
//		for(Thread t : threads) {
//			t.start();
//		}
//		for(Thread t : threads) {
//			t.join();
//		}
//		SelectValuableSocials.sort();
		
		SelectValuableSocials.combineInPrioList(PATH);
		System.out.println("Saved @ " + ((Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis())/1000.0));
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

	
	public static void addContent(BufferedWriter writer, String _id, String content) throws IOException {
			writer.write(_id + "; " + content);
			writer.newLine();
			writer.flush();
	}

}

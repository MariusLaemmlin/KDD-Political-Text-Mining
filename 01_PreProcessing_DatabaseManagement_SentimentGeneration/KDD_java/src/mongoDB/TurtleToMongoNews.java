package mongoDB;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.bson.Document;

import com.mongodb.*;
import com.mongodb.client.*;


public class TurtleToMongoNews implements Runnable {

	// Dateispezifisch
	private static final String LOCAL_PATH = "C:/Users/Local/Documents/Universität/KDD Seminar/news/";
	private int startFile;
	private int endFile;
	
	public void setStartFile(int startFile) {
		this.startFile = startFile;
	}

	public void setEndFile(int endFile) {
		this.endFile = endFile;
	}
	
	public static void main(String[] args) {
		TurtleToMongoNews[] col = new TurtleToMongoNews[15];
		int startFile;
		int endFile;
		for(int i = 1; i <= col.length; i++) {
			startFile = i;
			endFile = i;
			col[i-1] = new TurtleToMongoNews(startFile, endFile);
		}
		for(TurtleToMongoNews x : col) {
			new Thread(x).start();
		}
	}
	
	public TurtleToMongoNews(int startFile, int endFile) {
		super();
		this.startFile = startFile;
		this.endFile = endFile;
	}
	
	@Override
	public void run() {
		
		MongoClient mC = new MongoClient();
		MongoDatabase db = mC.getDatabase("news");
		MongoCollection<Document> col;
		Document doc;
		
		BufferedReader reader;
		
		String file;
		String id;
		int activeFileNo[] = new int[2];
		long start;
		long stop;
		
		try {
			for(int i = startFile; i <= endFile; i++) {
				start = System.currentTimeMillis();
				activeFileNo[0] = 1+(i-1)*2000; 
				activeFileNo[1] = i* 2000;
				file = getFileName(activeFileNo);
				reader = createBufferedReader(getFile(file));
				if(i < 10) {
					col = db.getCollection("news_0" + i);
				} else {
					col = db.getCollection("news_" + i);
				}
				System.out.println("Begin with " + file + "...");
				while((id = getNextID(reader)) != null) {
					doc = new Document("_id", id);
					doc.append("date", getNextCreated(reader));
					doc.append("language", getNextLanguage(reader));
					doc.append("content", getNextContent(reader));
					doc.append("publisher", getNextPublisher(reader));
					ArrayList<Document> categories = getNextCategoryAnnotations(reader);
					if(categories != null) {
						doc.append("categories", categories);
					}
					ArrayList<Document> entities = getNextEntityAnnotations(reader);
					if(entities != null) {
						doc.append("entities", entities);
					}
					col.insertOne(doc);
				}
				System.out.println("Transfered " + file + "!");
				
				stop = System.currentTimeMillis();
				long diff = stop-start;
				System.out.println("Time for conversion: " + (diff/1000.0) + "s");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Erstellt ein File-Objekt aus dem Dateinamen und dem als Konstante definierten Pfad LOCAL_PATH
	 * @param filename
	 * @return
	 */
	public static File getFile(String filename) {
		return new File(LOCAL_PATH + filename);
	}
	
	
	public static BufferedReader createBufferedReader(File file) throws FileNotFoundException {
		return new BufferedReader(new FileReader(file));
	}
	
	
	public static String getNextID(BufferedReader reader) throws IOException {
		String s;
		while((s = reader.readLine()) != null) {
			if(s.startsWith("<http://xlime")) {
				return s;
			}
		}
		return null;
	}
	
	public static Date getNextCreated(BufferedReader reader) throws IOException {
		String s;
		while((s = reader.readLine()) != null) {
			if(s.startsWith("	dcterms:created")) {
				int year = Integer.parseInt(s.substring(18,22));
				int month = Integer.parseInt(s.substring(23, 25))-1;
				int date = Integer.parseInt(s.substring(26, 28));
				int hourOfDay = Integer.parseInt(s.substring(29, 31));
				int minute = Integer.parseInt(s.substring(32, 34));
				int second = Integer.parseInt(s.substring(35, 37));
				Calendar c = Calendar.getInstance();
				c.set(year, month, date, hourOfDay, minute, second);
				Date d = new Date(c.getTimeInMillis());
				return d;
			}
		}
		return null;
	}
	
	//TODO
	public static String getNextLanguage(BufferedReader reader) throws IOException {
		reader.readLine();
		return "en"; 
	}
	
	public static String getNextPublisher(BufferedReader reader) throws IOException {
		String s;
		while((s = reader.readLine()) != null) {
			if(s.startsWith("	dcterms:publisher")) {
				reader.readLine();
				return s.substring(20, s.length()-2);
			}
		}
		return null;
	}
	
	private static String getNextContentLine(BufferedReader reader) throws IOException {
		String s;
		reader.mark(1000);
		boolean running = true;
		while(running && (s = reader.readLine()) != null) {
			if(s.startsWith("	sioc:content") || !s.startsWith("	dcterms:") && !s.startsWith("	xlime")) {
				reader.mark(1000);
				if(s.startsWith("	sioc:content	\"\"\"")) {
					s = s.substring(17); 
				}
				if(s.endsWith("\"\"\"^^xsd:string;")) {
					s = s.substring(0, s.length()-16);
				}
				if(s.startsWith("	sioc:content	\"")) {
					s = s.substring(15); 
				}
				if(s.endsWith("\"^^xsd:string;")) {
					s = s.substring(0, s.length()-14);
					if(s.endsWith("</p>")) {
						s = s.substring(0, s.length()-4);
					}
				} else {
					if(s.endsWith("</p>")) {
						s = s.substring(0, s.length()-4) + " ";
					} else {
						s = s + " ";
					}
				}
				if(s.startsWith("<p>")) {
					s = s.substring(3);
				}
				
				return s;
			} else {
				// Reader zurücksetzen, um keine Information zu verlieren
				reader.reset();
				running = false;
			}
		}
		return null;
	}
	
	public static String getNextContent(BufferedReader reader) throws IOException {
		ArrayList<String> contentLines = new ArrayList<>();
		String s;
		while((s = getNextContentLine(reader)) != null) {
			if(!s.equals(""))
				contentLines.add(s);
		}
		if(contentLines.isEmpty())
			contentLines = null;
		
		String result = "";
		for(String x : contentLines)
			result = result + x;
		result.replaceAll("<p>", " ");
		return result;
	}
	
	private static String getNextHashtag(BufferedReader reader) throws IOException {
		String s;
		reader.mark(1000);
		boolean running = true;
		while(running && (s = reader.readLine()) != null) {
			if(s.startsWith("	sioc:has_subject")) {
				reader.mark(1000);
				return s.substring(19, s.length()-14);
			} else {
				// Reader zurücksetzen, um keine Information zu verlieren
				reader.reset();
				running = false;
			}
		}
		return null;
	}
	
	public static ArrayList<String> getNextHashtags(BufferedReader reader) throws IOException {
		ArrayList<String> hashtags = new ArrayList<>();
		String s;
		while((s = getNextHashtag(reader)) != null) {
			hashtags.add(s);
		}
		if(hashtags.isEmpty())
			hashtags = null;
		
		return hashtags;
	}
	
	public static String getNextCreator(BufferedReader reader) throws IOException {
		String s;
		while((s = reader.readLine()) != null) {
			if(s.startsWith("	sioc:has_creator")) {
				
				return s.substring(19, s.length()-2);
			}
		}
		return null;
	}
	
	public static ArrayList<Document> getNextCategoryAnnotations(BufferedReader reader) throws IOException {
		ArrayList<Document> annotations = new ArrayList<>();
		Document d;
		while((d = getNextCategoryAnnotation(reader)) != null) {
			annotations.add(d);
		}
		if(annotations.isEmpty())
			annotations = null;
		
		return annotations;
	}
	
	private static Document getNextCategoryAnnotation(BufferedReader reader) throws IOException {
		String s;
		reader.mark(1000);
		boolean running = true;
		while(running && (s = reader.readLine()) != null) {
			if(s.startsWith("	xlime:hasCategoryAnnotation")) {
				double confidence = getNextCategoryAnnotationConfidence(reader);
				String category = getNextCategoryAnnotationCategory(reader);
				Document d = new Document("category", category);
				d.append("confidence", confidence);
				// ]; - Leerzeile ueberspringen
				reader.readLine();
				return d;
			} else {
				// Reader zuruecksetzen, um keine Information zu verlieren
				reader.reset();
				running = false;
			}
		}
		return null;
		
	}
	
	private static double getNextCategoryAnnotationConfidence(BufferedReader reader) throws IOException {
		String s = reader.readLine();
		if(s.startsWith("		xlime:hasConfidence")) {
			return Double.parseDouble(s.substring(23, s.length()-14));
		} else
			return 0.0;
	}
	
	private static String getNextCategoryAnnotationCategory(BufferedReader reader) throws IOException {
		String s = reader.readLine();
		if(s.startsWith("		xlime:hasCategory")) {
			return s.substring(21, s.length()-2);
		} else
			return null;
	}
	
	
	public static ArrayList<Document> getNextEntityAnnotations(BufferedReader reader) throws IOException {
		ArrayList<Document> annotations = new ArrayList<>();
		Document d;
		while((d = getNextEntityAnnotation(reader)) != null) {
			annotations.add(d);
		}
		if(annotations.isEmpty())
			annotations = null;
		
		return annotations;
	}
	
	private static Document getNextEntityAnnotation(BufferedReader reader) throws IOException {
		String s;
		reader.mark(1000);
		boolean running = true;
		while(running && (s = reader.readLine()) != null) {
			if(s.startsWith("	xlime:hasEntityAnnotation")) {
				double confidence = getNextEntityAnnotationConfidence(reader);
				String entity = getNextEntityAnnotationEntity(reader);
				Document d = new Document("entity", entity);
				d.append("confidence", confidence);
				// ]; - Leerzeile ueberspringen
				reader.readLine();
				reader.readLine();
				reader.readLine();
				reader.readLine();
				reader.readLine();
				return d;
			} else {
				// Reader zuruecksetzen, um keine Information zu verlieren
				reader.reset();
				running = false;
			}
		}
		return null;
		
	}
	
	private static double getNextEntityAnnotationConfidence(BufferedReader reader) throws IOException {
		String s = reader.readLine();
		if(s.startsWith("		xlime:hasConfidence")) {
			return Double.parseDouble(s.substring(23, s.length()-14));
		} else
			return 0.0;
	}
	
	private static String getNextEntityAnnotationEntity(BufferedReader reader) throws IOException {
		String s = reader.readLine();
		if(s.startsWith("		xlime:hasEntity")) {
			return s.substring(19, s.length()-2);
		} else
			return null;
	}
	
	
	
	
	
	
	
	public static String getFileName(int[] activeFileNo) {
		return "xlime_newsfeed_en_" + activeFileNo[0] + "-" + activeFileNo[1] + ".ttl";
		//xlime_social_en_hashtags_
	}
	
	public static int[] getNextFileNo(int[] activeFileNo) {
		activeFileNo[0] = activeFileNo[1] + 1;
		activeFileNo[1] = activeFileNo [1] + 2000;
		return activeFileNo; 
	}
	
	
	public static MongoDatabase retrieveDatabase(String database) {
		@SuppressWarnings("resource")
		MongoDatabase db = new MongoClient().getDatabase(database);
		return db;
	}
	
	public static MongoCollection<Document> retrieveCollection(MongoDatabase db, String collection) {
		return db.getCollection(collection);
	}
}


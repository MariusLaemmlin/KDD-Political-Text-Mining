package mongoDB;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.bson.*;
import org.bson.conversions.Bson;

import com.mongodb.*;
import com.mongodb.client.*;

import frequencies.*;

/**
 * Repraesentiert eine Suche in einer MongoDB. Durch das Interface Runnable parallelisierbar.
 * 
 * @author Marius Laemmlin
 * @version 2017-06-11
 * 
 */
public class Search implements Runnable {

	private MongoClient mC;
	private MongoDatabase db;
	private ArrayList<String> sCol;
	private Bson filter;
	private String threadName;
	
	public Search(ArrayList<String> collections, Bson filter) {
		 mC = new MongoClient();
		 db = mC.getDatabase("social");
		 this.sCol = collections;
		 this.filter = filter;
	}
	
	public Search() {
		
	}

	/**
	 * Durchsucht eine Datenbank nach allen Ergebnissen einer Spalte, zaehlt mehrfach vorkommende Eintraege und speichert diese in einem Dokument.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		long start = System.nanoTime();
		threadName = Thread.currentThread().getName();
		ArrayList<Element> elementList = new ArrayList<>();
		ArrayList<String> stringList;
		
		MongoCollection<Document> col;
		for(String s : sCol) {
			col = db.getCollection(s);
			FindIterable<Document> cursor = col.find(filter);
			
			System.out.println(threadName + ": StringList...(" + ((System.nanoTime() - start) / 1000000000.0) + "s)");
			stringList = documentsToArray(cursor, "hashtags");
			System.out.println(threadName + ": ElementList...(" + ((System.nanoTime() - start) / 1000000000.0) + "s)");
			countDuplicates(stringList, elementList);
			System.out.println(threadName + ": Save...(" + ((System.nanoTime() - start) / 1000000000.0) + "s)");
		}
		elementsSaveToFile("C:/Users/Local/Documents/Universität/KDD Seminar/hashtagList"+threadName+".txt", elementList);
		System.out.println(threadName + ": Saved!");
		System.out.println("\nTime for search: " + ((System.nanoTime() - start) / 1000000000.0) + "s");
	}

	/**
	 * Entfernt mehrfach vorkommende Werte in einer ArrayList und addiert diese in einem einzigen Element
	 * 
	 * @param col
	 * @return
	 */
	public ArrayList<Element> removeDuplicates(ArrayList<Element> col) {
		Element e;
		Element f;

		ArrayList<Element> remove = new ArrayList<>();

		for (Iterator<Element> i1 = col.iterator(); i1.hasNext();) {
			e = i1.next();
			for (Iterator<Element> i2 = col.iterator(); i2.hasNext();) {
				f = i2.next();
				if (!e.equals(f) && e.getName().equalsIgnoreCase(f.getName())) {
					if (e.getCount() > f.getCount()) {
						e.setCount(e.getCount() + f.getCount());
						f.setCount(0);
						f.setName("*" + f.getName() + "*");
						remove.add(f);
					} else {
						f.setCount(e.getCount() + f.getCount());
						e.setCount(0);
						e.setName("*" + e.getName() + "*");
						remove.add(e);
					}
					i2 = col.iterator();
					break;
				}
			}
		}

		col.removeAll(remove);

		return col;
	}
	
	/**
	 * Zaehlt mehrfach vorkommende Werte in einer ArrayList aus Strings
	 * 
	 * @param ListCol
	 * @param countCol
	 * @return
	 */
	public ArrayList<Element> countDuplicates(ArrayList<String> ListCol, ArrayList<Element> countCol) {
		boolean match;

		for(String listElement : ListCol) {
			match = false;
			for(Element countElement : countCol) {
				if(countElement.getName().equals(listElement.toLowerCase())) {
					countElement.increaseCount();
					match = true;
				} 
			}
			if(!match) {
				countCol.add(new Element(listElement.toLowerCase()));
			}
		}
		
		return countCol;
	}

	
	/**
	 * Extrahiert eine String-Liste aus einer Datenbank
	 * 
	 * @param cursor
	 * @param dbField
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> documentsToArray(FindIterable<Document> cursor, String dbField) {
		ArrayList<String> result = new ArrayList<>();
		for(Document x: cursor) {
			for(String hashtag : (ArrayList<String>) (x.get(dbField))) {
				result.add(hashtag);
			}
		}
		return result;
	}
	
	/**
	 * Speichert eine ArrayList aus Elementen mit Haeufigkeiten in einer Datei
	 * 
	 * @param filePath
	 * @param elements
	 */
	public void elementsSaveToFile(String filePath, ArrayList<Element> elements) {
		try {
			PrintWriter print = new PrintWriter(new FileWriter(filePath));
			for (Element e : elements) {
				print.println(e.getName() + ", " + e.getCount());
			}
			print.flush();
			print.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Sortiert den Output einer in einer Datei gespeicherten Suche
	 * 
	 * @param path
	 */
	public void sortFile(String path) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			ArrayList<Element> list = new ArrayList<>();
			String s;
			String[] ret;
			int count;
			while((s = reader.readLine()) != null) {
				ret = s.split(", ");
				count = Integer.parseInt(ret[1]);
				list.add(new Element(ret[0], count));
			}
			
			list.sort(null);
			
			new Search().elementsSaveToFile("C:/Users/Local/Documents/Universität/KDD Seminar/hashtagListTotalSorted.txt", list);
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Liesst eine gespeicherte Suche ein und gibt sie als ArrayList zurueck
	 * 
	 * @param path
	 * @return
	 */
	public  ArrayList<Element> readArrayFromFile(String path) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			ArrayList<Element> list = new ArrayList<>();
			String s;
			String[] ret;
			int count;
			while((s = reader.readLine()) != null) {
				ret = s.split(", ");
				count = Integer.parseInt(ret[1]);
				list.add(new Element(ret[0], count));
			}
			return list;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public MongoClient getmC() {
		return mC;
	}

	public void setmC(MongoClient mC) {
		this.mC = mC;
	}

	public MongoDatabase getDb() {
		return db;
	}

	public void setDb(MongoDatabase db) {
		this.db = db;
	}

	public ArrayList<String> getsCol() {
		return sCol;
	}

	public void setsCol(ArrayList<String> sCol) {
		this.sCol = sCol;
	}

	public Bson getFilter() {
		return filter;
	}

	public void setFilter(Bson filter) {
		this.filter = filter;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

}

package frequencies;

import java.io.*;
import java.util.*;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import mongoDB.Search;

/**
 * Fuehrt eine Suche zur Bestimmung der Haeufigkeiten von Hashtags durch
 * 
 * @author Marius Laemmlin
 * @version 2017-06-11
 * 
 */
public class HaeufigkeitenAnalyse {

	private static final int NOOFTHREADS = 10;

	public static void main(String[] args) {
	}

	public static void mergeResults() {
		Search search = new Search();
		ArrayList<Element>[] col = new ArrayList[NOOFTHREADS];
		for(int i = 0; i <= 9; i++) {
			col[i] = search.readArrayFromFile("C:/Users/Local/Documents/Universität/KDD Seminar/hashtagListS"+(i+1)+".txt");
		}
		
		ArrayList<Element> comb = new ArrayList<>();
		for(ArrayList<Element> x : col) {
			comb.addAll(x);
		}
		
		Search s = new Search();
		
		s.removeDuplicates(comb);
		s.elementsSaveToFile("C:/Users/Local/Documents/Universität/KDD Seminar/hashtagListTotal.txt", comb);
		
	}
	
	public static void hashtagSearch() {
		ArrayList<String>[] col = new ArrayList[NOOFTHREADS];

		int no = 1;
		int j;
		
		for (int i = 0; i < col.length; i++) {
			col[i] = new ArrayList<>();
			for(j = no; j <= 50 && j < (no+(50/NOOFTHREADS)); j++) {
				if (j < 10) {
					col[i].add("social_0" + j);
				} else {
					col[i].add("social_" + j);
				}
			}
			no = j;
		}
		
		for(ArrayList<String> x : col) {
			System.out.println(x);
		}

		Search[] search = new Search[NOOFTHREADS];
		for(int i = 0; i < search.length; i++) {
			search[i] = new Search(col[i], Filters.ne("hashtags", null));
		}
		
		Thread[] t = new Thread[NOOFTHREADS];
		for(int i = 0; i < t.length; i++) {
			t[i] = new Thread(search[i], "S" + (1+i));
			t[i].start();
		}
	}
	
	
}

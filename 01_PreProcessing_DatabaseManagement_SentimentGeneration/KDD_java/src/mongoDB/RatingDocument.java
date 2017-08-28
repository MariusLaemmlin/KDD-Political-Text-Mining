package mongoDB;

import java.util.Map;

import org.bson.Document;

/**
 * Implementiert zusaetzlich das Comparable Interface, um Ratings zu vergleichen.
 * 
 * @author Marius Laemmlin
 * @version 2017-06-30
 * 
 */
public class RatingDocument extends Document implements Comparable<RatingDocument> {

	public RatingDocument() {
	}

	public RatingDocument(Map<String, Object> map) {
		super(map);
	}

	public RatingDocument(String key, Object value) {
		super(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
	}

	/**
	 * Achtung: Vergleich ist invers, damit automatisch absteigend sortiert wird!
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RatingDocument o) {
		int result = -99;
		if(this.getDouble("rating") != null && o.getDouble("rating") != null) {
			double rating1 = this.getDouble("rating");
			double rating2 = o.getDouble("rating");
			if(rating1 < rating2) {
				result = 1;
			} else if(rating2 < rating1) {
				result = -1;
			} else if(rating1 == rating2) {
				result = 0;
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		return this.compareTo((RatingDocument) o) == 0;
	}
	
	

}

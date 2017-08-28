package sentiments;

public class Entity {
	private String type;
	private String text;
	private double sentiment_score;
	private double relevance;
	private String name = null;
	private String dbPedia = null;
	private int count;
	
	
	/**
	 * Konstruktor fuer News-Daten (enthaelt zusaetzlich "name" und "dbPedia")
	 * 
	 * @param type
	 * @param text
	 * @param sentiment_score
	 * @param relevance
	 * @param name
	 * @param dbPedia
	 * @param count
	 */
	public Entity(String type, String text, double sentiment_score, double relevance, String name, String dbPedia,
			int count) {
		super();
		this.type = type;
		this.text = text;
		this.sentiment_score = sentiment_score;
		this.relevance = relevance;
		this.name = name;
		this.dbPedia = dbPedia;
		this.count = count;
	}
	
	
	/**
	 * Konstruktor fuer Social-Daten
	 * 
	 * @param type
	 * @param text
	 * @param sentiment_score
	 * @param relevance
	 * @param count
	 */
	public Entity(String type, String text, double sentiment_score, double relevance, int count) {
		super();
		this.type = type;
		this.text = text;
		this.sentiment_score = sentiment_score;
		this.relevance = relevance;
		this.count = count;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getDbPedia() {
		return dbPedia;
	}

	public void setDbPedia(String dbPedia) {
		this.dbPedia = dbPedia;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public double getSentiment_score() {
		return sentiment_score;
	}
	public void setSentiment_score(double sentiment_score) {
		this.sentiment_score = sentiment_score;
	}
	public double getRelevance() {
		return relevance;
	}
	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}

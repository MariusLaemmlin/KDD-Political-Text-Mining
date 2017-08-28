package sentiments;

import java.util.ArrayList;

public class Sentiment {
	private String language;
	private ArrayList<Entity> entities;
	private String id;
	
	public Sentiment(String language, ArrayList<Entity> entities, String id) {
		super();
		this.language = language;
		this.entities = entities;
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public void setEntities(ArrayList<Entity> entities) {
		this.entities = entities;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	
	
}

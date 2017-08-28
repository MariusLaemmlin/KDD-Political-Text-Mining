package mongoDB;

import java.util.Date;

public class ScrapperObject {

	private String id;
	private Date date;
	private String publisher;
	private String author;
	private String text;
	
	
	/**
	 * Spezifisch für die Journalist.txt erstellt
	 * 
	 * @param id
	 * @param publisher
	 * @param author
	 * @param text
	 */
	public ScrapperObject(String id, String publisher, String author, String text) {
		super();
		this.id = id;
		this.publisher = publisher;
		this.author = author;
		this.text = text;
	}
	
	
	
	
	/**
	 * Spezifisch für News Erweiterung
	 * 
	 * @param id
	 * @param date
	 * @param publisher
	 * @param text
	 */
	public ScrapperObject(String id, Date date, String publisher, String text) {
		super();
		this.id = id;
		this.date = date;
		this.publisher = publisher;
		this.text = text;
	}




	public String getId() {
		return id;
	}
	public Date getDate() {
		return date;
	}
	public String getPublisher() {
		return publisher;
	}
	public String getAuthor() {
		return author;
	}
	public String getText() {
		return text;
	}
	
	

}

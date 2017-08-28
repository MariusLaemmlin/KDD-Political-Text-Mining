package frequencies;

import java.util.Date;

/**
 * Hilfsklasse, um ein Objekt mit Date und Integer Variablen bereitzustellen
 * 
 * @author Marius Laemmlin
 * 
 */
public class DateCount {
	public Date date;
	public int count = 1;
	
	public DateCount(Date date) {
		this.date = date;
	}
}


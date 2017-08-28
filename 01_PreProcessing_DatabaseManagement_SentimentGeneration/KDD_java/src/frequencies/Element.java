package frequencies;


/**
 * Repraesentiert ein Element mit dessen Name und Haeufigkeit
 * 
 * @author Marius Laemmlin
 * @version 2017-06-11
 * 
 */
public class Element implements Comparable<Element> {
	private String name;
	private long count;
	
	public Element(String name) {
		this.name = name;
		this.count = 1;
	}
	
	public Element(String name, int count) {
		this.name = name;
		this.count = count;
	}
	
	@Override
	public int compareTo(Element o) {
		int result = 0;
		if(this.count > o.count) result = 1;
		else if(this.count < o.count) result = -1;
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}	
	
	public void increaseCount() {
		count++;
	}
}

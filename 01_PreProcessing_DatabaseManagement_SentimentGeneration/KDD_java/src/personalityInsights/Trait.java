package personalityInsights;

import java.util.ArrayList;

public class Trait {

	private String trait_id;
	private String name;
	private String category;
	private double percentile;
	private ArrayList<Trait> children = null;
	
	public Trait(String trait_id, String name, String category, double percentile, ArrayList<Trait> children) {
		super();
		this.trait_id = trait_id;
		this.name = name;
		this.category = category;
		this.percentile = percentile;
		this.children = children;
	}
	
	public Trait(String trait_id, String name, String category, double percentile) {
		super();
		this.trait_id = trait_id;
		this.name = name;
		this.category = category;
		this.percentile = percentile;
	}

	public String getTrait_id() {
		return trait_id;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public double getPercentile() {
		return percentile;
	}

	public ArrayList<Trait> getChildren() {
		return children;
	}
	
	public boolean hasChildren() {
		return children != null;
	}
	
	@Override
	public String toString() {
		String result = "";
		result += "Trait_ID: " + trait_id 
				+ " Name: " + name
				+ " Category: " + category
				+ " Percentile: " + percentile;
		if(this.hasChildren()) {
			result += "\n";
			for(Trait child : children) {
				result += "\t" + child.toString() + "\n";
			}
		}
		return result;
	}
	
	public String toShortString() {
		String result = "";
		result += "Name: " + name
				+ " Percentile: " + percentile;
		if(this.hasChildren()) {
			result += "\n";
			for(Trait child : children) {
				result += "\t" + child.toShortString() + "\n";
			}
		}
		return result;
	}
	
	public String toFileString() {
		String result = "";
		result += percentile;
		if(this.hasChildren()) {
			for(Trait child : children) {
				result += ";" + child.toFileString();
			}
		}
		return result;
	}
	
	
}

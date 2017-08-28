package personalityInsights;

import java.io.*;
import java.util.*;

import javax.json.*;

public class PersonalityObject {

	private int word_count;
	private String processed_language;
	private ArrayList<Trait> personality;
	private ArrayList<Trait> needs;
	private ArrayList<Trait> values;
	private ArrayList<Trait> consumption_preferences;
	
	public PersonalityObject(int word_count, String processed_language, ArrayList<Trait> personality,
			ArrayList<Trait> needs, ArrayList<Trait> values, ArrayList<Trait> consumption_preferences) {
		super();
		this.word_count = word_count;
		this.processed_language = processed_language;
		this.personality = personality;
		this.needs = needs;
		this.values = values;
		this.consumption_preferences = consumption_preferences;
	}

	public int getWord_count() {
		return word_count;
	}

	public String getProcessed_language() {
		return processed_language;
	}

	public ArrayList<Trait> getPersonality() {
		return personality;
	}

	public ArrayList<Trait> getNeeds() {
		return needs;
	}

	public ArrayList<Trait> getValues() {
		return values;
	}

	public ArrayList<Trait> getConsumption_preferences() {
		return consumption_preferences;
	}
	
	
	public static PersonalityObject buildFromString() {
		int word_count;
		String processed_language;
		ArrayList<Trait> personality = new ArrayList<>();
		ArrayList<Trait> needs = new ArrayList<>();
		ArrayList<Trait> values = new ArrayList<>();
		ArrayList<Trait> consumption_preferences = new ArrayList<>();
		JsonReader reader = Json.createReader(System.in);
		JsonObject obj = reader.readObject();
		word_count = obj.getInt("word_count");
		processed_language = obj.getString("processed_language");
		System.out.println("PersonalityObject> Words: " + word_count);
		
		// Personality
		for(JsonValue val : obj.getJsonArray("personality")) {
			JsonObject big5 = (JsonObject) val;
			ArrayList<Trait> children = new ArrayList<>();
			for(JsonValue val2 : big5.getJsonArray("children")) {
				JsonObject childrenObj = (JsonObject) val2;
				Trait childrenTrait = new Trait(childrenObj.getString("trait_id"), childrenObj.getString("name"), childrenObj.getString("category"), childrenObj.getJsonNumber("percentile").doubleValue());
				children.add(childrenTrait);
			}
			Trait big5Trait = new Trait(big5.getString("trait_id"), big5.getString("name"), big5.getString("category"), big5.getJsonNumber("percentile").doubleValue(), children);
			personality.add(big5Trait);
		}
		System.out.println("PersonalityObject> Personality.");
		
		
		// Needs
		for(JsonValue val : obj.getJsonArray("needs")) {
			JsonObject needsObj = (JsonObject) val;
			Trait needsTrait = new Trait(needsObj.getString("trait_id"), needsObj.getString("name"), needsObj.getString("category"), needsObj.getJsonNumber("percentile").doubleValue());
			needs.add(needsTrait);
		}
		
		System.out.println("PersonalityObject> Needs.");
		// Values
		for(JsonValue val : obj.getJsonArray("values")) {
			JsonObject valuesObj = (JsonObject) val;
			Trait valuesTrait = new Trait(valuesObj.getString("trait_id"), valuesObj.getString("name"), valuesObj.getString("category"), valuesObj.getJsonNumber("percentile").doubleValue());
			values.add(valuesTrait);
		}
		
		System.out.println("PersonalityObject> Consumption.");
		// Consumption
		for(JsonValue val : obj.getJsonArray("consumption_preferences")) {
			JsonObject consumptionObj = (JsonObject) val;
			ArrayList<Trait> children = new ArrayList<>();
			for(JsonValue val2 : consumptionObj.getJsonArray("consumption_preferences")) {
				JsonObject childrenObj = (JsonObject) val2;
				Trait childrenTrait = new Trait(childrenObj.getString("consumption_preference_id"), childrenObj.getString("name"), "consumption_sub", childrenObj.getJsonNumber("score").doubleValue());
				children.add(childrenTrait);
			}
			Trait big5Trait = new Trait(consumptionObj.getString("consumption_preference_category_id"), consumptionObj.getString("name"), "consumption_main", -1.0, children);
			consumption_preferences.add(big5Trait);
		}		
		
		System.out.println("PersonalityObject> Return Object.");
		return new PersonalityObject(word_count, processed_language, personality, needs, values, consumption_preferences);
	}
	
	@Override
	public String toString() {
		String result = "";
		result += "Words: " + word_count + "\n"
				+ "Personality:\n";
		for(Trait trait : personality) {
			result += trait.toString() + "\n";
		}
		result += "\nNeeds:\n";
		for(Trait trait : needs) {
			result += trait.toString() + "\n";
		}
		result += "\nValues:\n";
		for(Trait trait : values) {
			result += trait.toString() + "\n";
		}
		result += "\nConsumption Preferences:\n";
		for(Trait trait : consumption_preferences) {
			result += trait.toString() + "\n";
		}
		return result;
	}
	
	public String toShortString() {
		String result = "";
		result += "Words: " + word_count + "\n"
				+ "Personality:\n";
		for(Trait trait : personality) {
			result += trait.toShortString() + "\n";
		}
		result += "\nNeeds:\n";
		for(Trait trait : needs) {
			result += trait.toShortString() + "\n";
		}
		result += "\nValues:\n";
		for(Trait trait : values) {
			result += trait.toShortString() + "\n";
		}
		result += "\nConsumption Preferences:\n";
		for(Trait trait : consumption_preferences) {
			result += trait.toShortString() + "\n";
		}
		return result;
	}
	
	public String toFileString() {
		String result = ";";
		result += word_count;
		for(Trait trait : personality) {
			result += ";" + trait.toFileString();
		}
		for(Trait trait : needs) {
			result += ";" + trait.toFileString();
		}
		for(Trait trait : values) {
			result += ";" + trait.toFileString();
		}
		return result;
	}
	
	public void saveToFile(File file, boolean append) {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file, append));
			if(!append || new FileReader(file).read() == -1) {
				pw.println("Person;"
						+ "Words;"
						+ "Personality_Openness;"
						+ "Personality_Openness_Adventurousness;"
						+ "Personality_Openness_Aritstic interests;"
						+ "Personality_Openness_Emotionality;"
						+ "Personality_Openness_Imagination;"
						+ "Personality_Openness_Intellect;"
						+ "Personality_Openness_Authority-challenging;"
						+ "Personality_Conscientousness;"
						+ "Personality_Conscientousness_Achievement;"
						+ "Personality_Conscientousness_Cautiousness;"
						+ "Personality_Conscientousness_Dutifulness;"
						+ "Personality_Conscientousness_Orderliness;"
						+ "Personality_Conscientousness_Self-discipline;"
						+ "Personality_Conscientousness_Self-efficacy;"
						+ "Personality_Extraversion;"
						+ "Personality_Extraversion_Activity-level;"
						+ "Personality_Extraversion_Assertiveness;"
						+ "Personality_Extraversion_Cheerfulness;"
						+ "Personality_Extraversion_Excitement-seeking;"
						+ "Personality_Extraversion_Outgoing;"
						+ "Personality_Extraversion_Gregariousness;"
						+ "Personality_Agreeableness;"
						+ "Personality_Agreeableness_Altruism;"
						+ "Personality_Agreeableness_Cooperation;"
						+ "Personality_Agreeableness_Modesty;"
						+ "Personality_Agreeableness_Uncompromising;"
						+ "Personality_Agreeableness_Sympathy;"
						+ "Personality_Agreeableness_Trust;"
						+ "Personality_Emotional-Range;"
						+ "Personality_Emotional-Range_Fiery;"
						+ "Personality_Emotional-Range_Prone-to-worry;"
						+ "Personality_Emotional-Range_Melancholy;"
						+ "Personality_Emotional-Range_Immoderation;"
						+ "Personality_Emotional-Range_Self-consciousness;"
						+ "Personality_Emotional-Range_Susceptible-to-stress;"
						+ "Needs_Challenge;"
						+ "Needs_Closeness;"
						+ "Needs_Curiosity;"
						+ "Needs_Excitement;"
						+ "Needs_Needs_Harmony;"
						+ "Needs_Ideal;"
						+ "Needs_Liberty;"
						+ "Needs_Love;"
						+ "Needs_Practicality;"
						+ "Needs_Self-expression;"
						+ "Needs_Stability;"
						+ "Needs_Structure;"
						+ "Values_Conservation;"
						+ "Values_Openness-to-change;"
						+ "Values_Hedonism;"
						+ "Values_Self-enhancement;"
						+ "Values_Self-transcendence");
			}
			pw.println(this.toFileString());
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
}

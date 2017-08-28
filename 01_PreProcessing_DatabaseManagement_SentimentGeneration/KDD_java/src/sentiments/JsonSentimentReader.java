package sentiments;

import java.io.*;
import java.util.ArrayList;

public class JsonSentimentReader {

	public static ArrayList<Sentiment> readSentiments(File file, boolean isNewsFile) {
		ArrayList<Sentiment> sentiments = new ArrayList<>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			boolean running = true;
			String s;
			while (running) {
				reader.readLine();
				String language = readLanguage(reader);
				if (language == null) {
					running = false;
					break;
				}
				ArrayList<Entity> entities = null;
				if ((s = reader.readLine()).equals("  \"entities\": [") && !s.endsWith("],")) {
					entities = readEntities(reader, isNewsFile);
					reader.readLine();
				}
				String id = readID(reader);
				sentiments.add(new Sentiment(language, entities, id));

			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sentiments;
	}

	/**
	 * Beginnt mit { nach Entities. Endet in der Zeile mit } nach der letzten
	 * Entity
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<Entity> readEntities(BufferedReader reader, boolean isNewsFile) throws IOException {
		ArrayList<Entity> list = new ArrayList<>();

		boolean running = true;

		while (running) {
			reader.readLine();
			if (isNewsFile) {
				list.add(new Entity(readEntityType(reader), readEntityText(reader), readEntitySentimentScore(reader),
						readEntityRelevance(reader), readEntityName(reader), readEntityDBPedia(reader),
						readEntityCount(reader)));
			} else {
				list.add(new Entity(readEntityType(reader), readEntityText(reader), readEntitySentimentScore(reader),
						readEntityRelevance(reader), readEntityCount(reader)));
			}
			String s;
			if ((s = reader.readLine()).equals("    }"))
				running = false;
		}
		return list;

	}

	private static String readLanguage(BufferedReader reader) throws IOException {
		String s;
		String result = null;
		if ((s = reader.readLine()) != null && s.startsWith("  \"language")) {
			result = s.substring(15, s.length() - 2);
		}
		return result;
	}

	private static String readEntityType(BufferedReader reader) throws IOException {
		String s;
		String result = null;
		if ((s = reader.readLine()) != null && s.startsWith("      \"type\":")) {
			result = s.substring(15, s.length() - 2);
		}
		return result;
	}

	private static String readEntityText(BufferedReader reader) throws IOException {
		String s;
		String result = null;
		if ((s = reader.readLine()) != null && s.startsWith("      \"text\":")) {
			result = s.substring(15, s.length() - 2);
		}
		return result;
	}

	private static double readEntitySentimentScore(BufferedReader reader) throws IOException {
		String s;
		double result = 0.0;
		reader.mark(500);
		if ((s = reader.readLine()) != null && s.startsWith("      \"sentiment\":")) {
			if ((s = reader.readLine()) != null && s.startsWith("        \"score\":")) {
				result = Double.parseDouble(s.substring(17, s.length()));
				reader.readLine();
			} else {
				System.out.println("Warning: SentimentScore not readable!");
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.exit(1);
			}
		} else {
			reader.reset();
		}
		return result;
	}

	private static double readEntityRelevance(BufferedReader reader) throws IOException {
		String s;
		double result = 0.0;
		if ((s = reader.readLine()) != null && s.startsWith("      \"relevance\":")) {
			result = Double.parseDouble(s.substring(19, s.length() - 1));
		} else {
			System.out.println(s);
			System.out.println(reader.readLine());
			System.out.println(reader.readLine());
			System.out.println(reader.readLine());
			System.out.println(reader.readLine());
			System.out.println(reader.readLine());
			System.out.println(reader.readLine());
			System.out.println("Warning: Relevance not readable!");
			System.exit(1);
		}

		return result;
	}

	private static String readEntityName(BufferedReader reader) throws IOException {
		String s;
		String result = null;
		reader.mark(500);
		if ((s = reader.readLine()) != null && s.startsWith("      \"disambiguation\"")) {
			boolean searching = true;
			while (searching) {
				reader.mark(500);
				if ((s = reader.readLine()) != null && s.startsWith("        \"name\":")) {
					result = s.substring(17, s.length() - 2);
					searching = false;
				} else {
					if (s.equals("      },")) {
						searching = false;
						reader.reset();
					}
				}
			}
		} else {
			reader.reset();
		}
		return result;
	}

	private static String readEntityDBPedia(BufferedReader reader) throws IOException {
		String s;
		String result = null;
		reader.mark(500);
		if ((s = reader.readLine()) != null && s.startsWith("        \"dbpedia_resource")) {
			result = s.substring(29, s.length() - 1);
		} else {
			reader.reset();
		}
		return result;
	}

	private static int readEntityCount(BufferedReader reader) throws IOException {
		String s;
		int result = 0;
		// Suche hier notwendig, da disambiguation dazwischen liegen koennte
		boolean searching = true;
		while (searching) {
			if ((s = reader.readLine()) != null && s.startsWith("      \"count\":")) {
				result = Integer.parseInt(s.substring(15, s.length()));
				searching = false;
			}
		}

		return result;
	}

	private static String readID(BufferedReader reader) throws IOException {
		String s;
		String result = null;
		boolean searching = true;
		while (searching) {
			if ((s = reader.readLine()) != null && s.startsWith("  \"id\":")) {
				result = s.substring(9, s.length() - 1);
				searching = false;
			}
		}
		return result;
	}
}

package charHandling;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Klasse, um Zeichen zu entfernen, die mit Apache Jena Fuseki inkompatibel sind
 * 
 * @author Marius Laemmlin
 * 
 */
public class CharKiller {
 
    public static void illegalCharKiller(String[] args) {
 
        try {
			File f1 = new File(args[1]);
			File f2 = new File(args[2]);
			f1.createNewFile();
			f2.createNewFile();
        	
            FileReader fr = new FileReader(f1);
            BufferedReader br = new BufferedReader(fr);            
            FileWriter ausgabestrom = new FileWriter(f2);
            PrintWriter ausgabe = new PrintWriter(ausgabestrom);
            String line;
            Boolean berabeitungszeile = false;
 
            
            while ((line = br.readLine()) != null) {
            	
            	if(line.contains("sioc:content")){   // herausfinden ob content beginnt
            		berabeitungszeile = true;
            	}
            	
            	while(berabeitungszeile){
            		
            		line = line.replace("\""," "); //entferne alle Anf�hrungszeichen
            		
            		line = line.replace("sioc:content	", "sioc:content	\"\"\"");  // drei anf�hrungszeichen am anfang von content 
            		line = line.replace("^^xsd:string;", "\"\"\"^^xsd:string;");     // drei anf�hrungszeichen am ende von content !!!ACUHTUNG problem falls teil von "^^xsd:string; in n�chste zeile verschoben ist wird nichts ge�ndert
            		
            		line = line.replace("\\"," ");
           		
           		    line = line.replace("_", " "); //links werden mit gefilteret 
            		line = line.replace("/", " ");
                 	line = line.replace(")", " ");
          
            		
    
            		
//            		System.out.println(line.toString());
            		ausgabe.println(line.toString());   // neue datei schreiben
            		
            		line = br.readLine();
            		
                	if(line.contains("has_creator") || line.contains("has_subject")){
                		berabeitungszeile = false;
                	}  
                	
            	}	
                	
            	
            	if(line.contains("xlime:hasEntity")){
                		line = line.replace("\"",""); //entferne alle Anf�hrungszeichen
                	}
            	
//                System.out.println(line.toString());
                ausgabe.println(line.toString()); 
            }
 
            ausgabe.close();
            br.close();
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
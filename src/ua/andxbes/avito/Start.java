package ua.andxbes.avito;

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Start {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	
	boolean atTheBeginning = interview();
	
        Avito a = new Avito();

	if(atTheBeginning) parse(a);
	
	
	try {
	    a.getEmail();
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
	}

    }

    private static boolean interview() {
	
	boolean atTheBeginning = true;
	System.out.println("Start new Scan? y/n");
	String answer = new Scanner(System.in).nextLine();
	if (answer.contains("n")) {
	    atTheBeginning = false;
	}
	System.out.println("Do you set proxy? y/n");
	String answer2 = new Scanner(System.in).nextLine();
	if (answer2.contains("y")) {
	    System.out.println("Insert like : 192.168.1.1:8080");
	    String proxy = new Scanner(System.in).nextLine().trim();
	    setProxy(proxy);
	}
	return atTheBeginning;
    }

    private static void parse(Avito a) {
	try {

	    Avito.loadFieldFromFile();
	    Logger.getLogger("Start").info("------------------------ Loaded -------------------------");
	    a.start();
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(Start.class.getName()).log(Level.INFO, "Not file \n{0}", ex);
	    a.start();

	}
	System.out.println("конец");
	Avito.save();
    }

    private static void setProxy(String arg) {
	String proxy[] = arg.split(":");
	System.setProperty("http.proxyHost", proxy[0]);
	System.setProperty("http.proxyPort", proxy[1]);
    }
}

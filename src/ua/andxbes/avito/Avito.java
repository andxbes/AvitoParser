/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.andxbes.avito;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Andr
 */
public class Avito {

    public final static Logger log = Logger.getLogger("Avito");
    public static final String BASE_URL = "https://www.avito.ru/shops/rossiya",
	    NEXT_BASE_URL = "https://www.avito.ru/shops/rossiya?p=",
	    SITE_URL = "https://www.avito.ru",
	    NAME_FILE_GSONE = "./saveGsone.txt";

    private ExecutorService ex = Executors.newFixedThreadPool(15);
    private static Map<String, Contact> contactMap = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, List<Contact>> contactMapfromSity = Collections.synchronizedMap(new HashMap<>());
    private static int page = 1;
    private static boolean b = true;
    private long pause = 500;

    public Map<String, Contact> getContactMap() {
	return contactMap;
    }
//step one 

    public String getMapCompany(String url) throws IOException {

	URL u = null;

	Connection connect = Jsoup.connect(url);
	//Document doc = connect.userAgent(USER_AGENT).referrer(REFERER).timeout(TIME_OUT).get();
	Document doc = connect.get();
	u = connect.execute().url();

	Elements els = doc.select(".t_s_i .t_s_title .t_s_h3 a[href]");
	for (Element el : els) {
	    log.log(Level.INFO, "\nhtml = {0}" + "\n" + "href = {1}\n", new Object[]{el.html(), el.attr("href")});
	    log.log(Level.INFO, "-----------------  Contact # {0} -------------------", contactMap.size());
	    Contact contact = new Contact();
	    contact.setUrlOnAvito(SITE_URL + el.attr("href") + "/contact");
	    contact.setName(el.html());
	    getContactMap().put(el.attr("href"), contact);

	    getContactsField(contact);
	    waitLoadPages();

	}

	log.log(Level.INFO, "_____________page = {0}_____________", u.toString());
	return u.toString();
    }

    public void getContactsField(Contact contact) throws IOException {

	Connection connect = Jsoup.connect(contact.getUrlOnAvito());
	//Document doc = connect.userAgent(USER_AGENT).referrer(REFERER).timeout(TIME_OUT).get();
	Document doc = connect.get();
	//log.log(Level.INFO, "url = {0}", connect.execute().url());

	Elements addresses = doc.select("  .address .info");
	for (Element addresse : addresses) {
	    // log.log(Level.INFO, "ADDRESS  = {0}", addresse.text());
	    contact.setAddress(addresse.text());
	}

	addresses = doc.select(" .column-l .contact-item .info");
	for (Element addresse : addresses) {
	    //log.log(Level.INFO, "phone  = {0}", addresse.text());
	    contact.setPhone(addresse.text());
	}

	addresses = doc.select(" .column-r .contact-item .info");
	for (Element addresse : addresses) {
	    //log.log(Level.INFO, "site  = {0}", addresse.text());
	    contact.setSite(addresse.text());
	}
	log.info(contact.toString());

    }

    public void start() {

	log.info("$$$$$$$$$$$$$$$$ STEP ONE (get base for parsing) $$$$$$$$$$$$$$$$$");
	while (b) {

	    try {
		String u = getMapCompany(NEXT_BASE_URL + page);
		if (u.equals(BASE_URL)) {
		    b = false;
		}

	    } catch (IOException ex) {
		Logger.getLogger(Avito.class.getName()).log(Level.SEVERE, null, ex);
		try {
		    save();
		    Thread.sleep(30 * 60 * 1000);
		    
		    pause += 1000;
		    log.log(Level.INFO, "::::::::::::::::::::: pause = {0} :::::::::::::::::::::::", pause);
		    start();//возобновляем работу если в бане , бан длится 30 минут 
		} catch (InterruptedException ex1) {
		    Logger.getLogger(Avito.class.getName()).log(Level.SEVERE, null, ex1);
		}

	    }
	    page++;

	}

    }

    private void waitLoadPages() {

	try {
	    Thread.sleep(pause);

	} catch (InterruptedException ex) {
	    Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
	}
	log.log(Level.INFO, "\nPAGE = {0}\n", page);
    }

    ///================================== stap two =============================================
    public void getEmail() throws FileNotFoundException {
	log.info("$$$$$$$$$$$$$$$$ STEP TWO (getting email address) $$$$$$$$$$$$$$$$$");
	if (contactMap.isEmpty()) {
	    loadFieldFromFile();
	}

	contactMap.entrySet().stream().map((entrySet) -> entrySet.getValue()).forEach((contact) -> {

	    ex.submit(() -> sortToMapFromTheSity(contact)); 
	    ex.submit(new GetEmaiFromSite(contact, ex));

	});

	ex.shutdown();

	while (!ex.isTerminated()) {
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException ex) {
		Logger.getLogger(Avito.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	saveSort();
	save();

    }

    //===================================== sort ====================================
    private void sortToMapFromTheSity(Contact contact) {

	String sity = getSity(contact);

	List<Contact> contactList = contactMapfromSity.get(sity);
	if (contactList == null) {
	    log.info("\n\nnew List\n");
	    contactList = new ArrayList<>();
	}
	contactList.add(contact);
	contactMapfromSity.put(sity, contactList);
    }

    private String getSity(Contact contact) {
	String sity = null;
	String[] temp = contact.getAddress().split(" ");
	for (int i = 0; i < temp.length; i++) {
	    if (temp[i].equals("г.") && i < temp.length - 1) {
		sity = temp[++i];
	    }
	}
	sity = sity == null ? "uknown" : sity;
	sity = sity.replace(',', ' ').trim();
	return sity;
    }

    //=================================== Save or load contact map ================================================
    public synchronized static void save() {
	File f = new File(NAME_FILE_GSONE);

	Gson gson = new Gson();
	String json = gson.toJson(contactMap);

	try (FileWriter fw = new FileWriter(f);) {

	    fw.write(json);

	} catch (IOException ex) {
	    Logger.getLogger("Avito").log(Level.SEVERE, null, ex);
	}

	log.info("==========================  Save  =========================");
    }

    public static void loadFieldFromFile() throws FileNotFoundException {
	FileReader fr = null;
	try {
	    StringBuilder json = new StringBuilder();
	    fr = new FileReader(new File(NAME_FILE_GSONE));
	    BufferedReader br = new BufferedReader(fr);
	    try {
		while (br.ready()) {
		    json.append(br.readLine());
		}
	    } catch (IOException ex) {
		Logger.getLogger("11").log(Level.SEVERE, null, ex);
	    } finally {

		try {
		    br.close();
		} catch (IOException ex) {
		    Logger.getLogger("11").log(Level.SEVERE, null, ex);
		}

	    }

	    contactMap = new Gson().fromJson(json.toString(), new TypeToken<Map<String, Contact>>() {
	    }.getType());
	    int current = (int) contactMap.size() / 50;
	    page = current == 0 ? 1 : current;

	} catch (FileNotFoundException ex) {
	    throw new FileNotFoundException(ex.getMessage());
	} finally {

	    try {
		if (fr != null) {
		    fr.close();
		}
	    } catch (IOException ex) {
		Logger.getLogger(Avito.class.getName()).log(Level.SEVERE, null, ex);
	    }

	}

    }

    public void saveSort() {
	int all = 0;
	log.info("==========================  Save Sort =========================");
	String SITY = "./sitywithEmail";
	File dir = new File(SITY);
	dir.mkdirs();

	for (Map.Entry<String, List<Contact>> entrySet : contactMapfromSity.entrySet()) {
	    int numEmail = 0;
	    StringBuilder sb = new StringBuilder();
	    String sity = entrySet.getKey();
	    List<Contact> contactList = entrySet.getValue();

	    for (Contact contactList1 : contactList) {
		numEmail += contactList1.getNumEmail();
		String emails = contactList1.getEmail();
		all += numEmail;
		if (emails.isEmpty()) {
		    continue;
		}
		sb.append(emails).append("\n");

	    }

	    String path = SITY + "/" + sity + "_" + numEmail + ".txt";
	    File f = new File(path);
	    try (FileWriter fw = new FileWriter(f);) {

		fw.write(sb.toString());

		log.log(Level.INFO, "=================  {0} Save =======================", path);
	    } catch (IOException ex) {
		Logger.getLogger("Avito").log(Level.SEVERE, null, ex);
	    }
	}
	log.log(Level.INFO, "====================  all = {0} =====================", all);

    }

    /**
     * @return the page
     */
    public int getPage() {
	return page;
    }

}

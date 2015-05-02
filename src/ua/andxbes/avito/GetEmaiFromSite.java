/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.andxbes.avito;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Andr
 */
public class GetEmaiFromSite implements Runnable {
    private static int numOfObgect = 0;
    private int thisObgect;
    Logger log = Logger.getLogger(this.getClass().getSimpleName()+" - " + thisObgect);
    
    private ExecutorService exec ;
    private Contact contact;
    private static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0",
	    REFERER = "http://www.google.com";
    public static final int TIME_OUT = (1000 * 10);

    private volatile int numOfTries;
    private static final int LIMIT = 5;
    
    public GetEmaiFromSite(Contact contact,ExecutorService exec ) {
	thisObgect = numOfObgect++;
	this.contact = contact;
	this.exec = exec ;
	numOfTries = 0 ;
    }

    @Override
    public void run() {
	    numOfTries++;
	    log.log(Level.INFO, "%%%%%%%%%%%%% {0} continue {1} %%%%%%%%%%%%%%", 
		    new Object[]{contact.getName(), numOfTries});
            processFinding(contact);
	   
	    
    }

    private void processFinding(Contact contact) {
	//get url
	String url1 = contact.getSite().replaceAll("Написать письмо", "");
	String urlArr[] = url1.split(" ");

	for (String url : urlArr) {

	    url = url.trim().toLowerCase();

	    Pattern p = Pattern.compile(".+\\.(info|club|ag|me|de|moscow|house|in|pro|com|eu|ee|net|ua|рф|ru|cc|su|org|biz|xn--p1ai)+/?+(.)*");
	    Matcher m = p.matcher(url);

	    if (m.matches() && !url.contains("vk.com") && !url.contains("avito.ru")) {

		contact.setUrl(url);

		//TODO готовы ссылки  , продолжаем
		parseExternalSite(contact);
		// log.log(Level.INFO, "url - {0} , email - {1}", new Object[]{contact.getUrl(), contact.getEmail()});

	    } else {
		//log.log(Level.INFO, "url - {0}", url);

	    }
	}
    }

    private void parseExternalSite(Contact contact) {

	log.log(Level.INFO, "url ======={0}", contact.getUrl());
	Connection connect = Jsoup.connect(contact.getUrl());
	try {
	    // Document doc = connect.userAgent(USER_AGENT).referrer(REFERER).timeout(TIME_OUT).get();
	    Document doc = connect.get();
	    searchEmailforAll(contact, doc);

	    Elements elements = doc.select("a[href]");

	    for (Element element : elements) {

		if (searchText(element, "Контакты")) {
		    //если ссылка и есть контакт заносим в список и пропускаем 
		    if (contact.setLinktoContacts(element.attr("href"))) {
			//ex.submit(() -> searchEmail(contact));

			searchEmail(contact);
			//log.log(Level.INFO, "text = {0}, href {1}", new Object[]{element.text(), contact.getLinkFtoContacts()});
		    }

		}
	    }

	} catch (IOException ex) {
	    Logger.getLogger(Avito.class.getName()).log(Level.SEVERE, null, ex);
	    tryAgain();
	}

    }

    private void tryAgain() {
	if(numOfTries < LIMIT){ 
	   run();
	}else{
	    log.info("==============================END==================================");
	}
	
    }

    private boolean searchText(Element element, String text) {
	//если в блоке или его детях содержистя текст 
	if (element.text().contains(text)) {
	    return true;
	} else if (element.html().contains(text)) {

	    return true;
	}

	return false;
    }

    public void searchEmail(Contact contact) {

	try {
	    Connection connect = Jsoup.connect(contact.getLinkFtoContacts());
	    // Document doc = connect.userAgent(USER_AGENT).referrer(REFERER).timeout(TIME_OUT).get();
	    Document doc = connect.get();

	    searchEmailforAll(contact, doc);
	} catch (IOException ex) {
	    Logger.getLogger(Avito.class.getName()).log(Level.SEVERE, null, ex);
	    tryAgain();
	}
    }

    public void searchEmailforAll(Contact contact, Document doc) {
	Pattern pat = Pattern.compile(".+\\@.+\\.?[a-z]*");
	Elements els = doc.select(":containsOwn(@)");
	for (Element el : els) {
	    Matcher m = pat.matcher(el.text());
	    while (m.find()) {

		//log.log(Level.INFO, "Matcher - {0}", m.group());
		String group = m.group();
		group = group.replace("(", "").replace(")", "");
		String temp[] = group.split(" |'|\"|:|=");
		for (String temp1 : temp) {

		    if (pat.matcher(temp1).matches() & temp1.length() > 5) {
			log.log(Level.INFO, "\n\n +++++++++++++++ Found  = {0} +++++++++++++++", temp1);
			contact.setEmail(temp1);
		    } else {
			log.log(Level.INFO, "\n\n ERRROR text  = {0}    on - {1}", new Object[]{temp1, contact.getUrl()});
		    }
		}
	    }

	}

    }

}

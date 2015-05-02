/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.andxbes.avito;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andr
 */
public class Contact {

    private String name = "";
    private String phone = "";
    private String site = "";
    private String address = "";
    private String urlOnAvito = "";
    private String email = "";
    private String url = "";
    private String linktoContacts = "";
    private boolean ready = false;
    private int numEmail = 0;

    @Override
    public String toString() {
	return "Contact{" + "name=" + name + ", phone=" + phone + ", site=" + site + ", address=" + address + ", urlOnAvito=" + urlOnAvito + ", email=" + email + ", url=" + url + ", linktoContacts=" + linktoContacts + ", ready=" + ready + ", numEmail=" + numEmail + '}';
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name the name to set
     */
    public synchronized void setName(String name) {
	this.name = this.name + " " + name;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
	return phone;
    }

    /**
     * @param phone the phone to set
     */
    public synchronized void setPhone(String phone) {
	this.phone = this.phone + " " + phone;
    }

    /**
     * @return the site
     */
    public String getSite() {
	return site;
    }

    /**
     * @param site the site to set
     */
    public synchronized void setSite(String site) {
	this.site = this.site + " " + site;
    }

    /**
     * @return the address
     */
    public String getAddress() {
	return address;
    }

    /**
     * @param address the address to set
     */
    public synchronized void setAddress(String address) {
	this.address = this.address + " " + address;
    }

    /**
     * @return the urlOnAvito
     */
    public String getUrlOnAvito() {
	return urlOnAvito;
    }

    /**
     * @param urlOnAvito the urlOnAvito to set
     */
    public synchronized void setUrlOnAvito(String urlOnAvito) {
	this.urlOnAvito += urlOnAvito;
    }

    /**
     * @return the ready
     */
    public boolean isReady() {
	return ready;
    }

    /**
     * @param ready the ready to set
     */
    public synchronized void setReady(boolean ready) {
	this.ready = ready;
    }

    /**
     * @return the email
     */
    public String getEmail() {
	return email;
    }

    /**
     * @param email the email to set
     */
    public synchronized void setEmail(String email) {
	if (email.startsWith("mailto:")) {
	    email = email.replace("mailto:", "");
	}
	if (!this.email.contains(email)) {
	    this.email = this.email.isEmpty() ? email : this.email + "\n" + email ;
	    Logger.getLogger("Contact").log(Level.INFO, "\n\n+++++++++++++++++ Email added {0}+++++++++++++++++++++\n\n", email);
	    numEmail++;
	}
    }

    /**
     * @return the url
     */
    public String getUrl() {
	return url;
    }

    /**
     * @param url the url to set
     */
    public synchronized void setUrl(String url) {
	if (!url.contains("http")) {
	    this.url = "http://" + url;
	} else {
	    this.url = url;
	}
    }

    /**
     * @return the linktoContacts
     */
    public String getLinkFtoContacts() {
	return linktoContacts;
    }

    /**
     * @param linktoContacts the linktoContacts to set
     * @return
     */
    public synchronized boolean setLinktoContacts(String linktoContacts) {

	if (linktoContacts.startsWith("mailto:")||linktoContacts.contains("@")) {
	    setEmail(linktoContacts);
	    return false;
	}

	if (!linktoContacts.contains("http")) {
	    if (linktoContacts.startsWith("/") & getUrl().endsWith("/")) {
		linktoContacts = linktoContacts.substring(1);
	    } else if (!linktoContacts.startsWith("/") & !getUrl().endsWith("/")) {
		linktoContacts = "/" + linktoContacts;
	    }
	    Logger.getLogger("Contact ").log(Level.INFO, "url  = {0} \u043a\u043e\u043d =  {1}", new Object[]{getUrl(), linktoContacts});
	    this.linktoContacts = getUrl() + linktoContacts;
	} else {
	    this.linktoContacts = linktoContacts;
	}
	return true;
    }

    /**
     * @return the numEmail
     */
    public int getNumEmail() {
	return numEmail;
    }

}

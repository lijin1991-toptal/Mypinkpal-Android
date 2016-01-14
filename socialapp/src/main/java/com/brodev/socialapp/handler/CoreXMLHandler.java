package com.brodev.socialapp.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CoreXMLHandler extends DefaultHandler {
	
	private String url;
	private String gcmKey;
	boolean currentElement = false;
	private String currentValue = "";

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getGcmKey() {
		return gcmKey;
	}

	public void setGcmKey(String gcmKey) {
		this.gcmKey = gcmKey;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		currentElement = false;

		//set core url
		if (qName.equalsIgnoreCase("url"))
			url = currentValue.trim();
		
		//set gcm key
		if (qName.equalsIgnoreCase("senderid"))
			gcmKey = currentValue.trim();

		currentValue = "";
		super.endElement(uri, localName, qName);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		currentElement = true;

		super.startElement(uri, localName, qName, attributes);
	}
	
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (currentElement) {
			currentValue = currentValue + new String(ch, start, length);
		}

	}
}

package org.kaaproject.kaa.server.appenders.pahomqtt.appender;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 
 * @author Raghunathan R, www.ethicstech.in
 * @version 1.0.1
 * @since  2017-02-01 
 *
 */
public final class LogEvent implements Serializable {

    private static final long serialVersionUID = -1L;
    private String header = "";
    private String event = "";
    private String channel = "";
    private HashMap<String, String> jsonMessageMap = null;
    

    public LogEvent() {
    	
    }

    public String getHeader() {
		return header;
	}


	public void setHeader(String header) {
		this.header = header;
	}


	public String getEvent() {
		return event;
	}


	public void setEvent(String event) {
		this.event = event;
	}


	public String getChannel() {
		return channel;
	}


	public void setChannel(String channel) {
		this.channel = channel;
	}


	public HashMap<String, String> getJsonMessageMap() {
		return jsonMessageMap;
	}


	public void setJsonMessageMap(HashMap<String, String> jsonMessageMap) {
		this.jsonMessageMap = jsonMessageMap;
	}


	@Override
	public String toString() {
		return "LogEvent [header=" + header + ", event=" + event + ", channel=" + channel + ", jsonMessageMap="
				+ jsonMessageMap + "]";
	}



}
package org.kaaproject.kaa.server.appenders.pahomqtt.appender;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.pahomqtt.config.gen.PahoMqttConfig;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This class used to process the mqtt connection and message publish.
 * 
 *

	# Plain MQTT protocol
			listener 1883
	
	# End of plain MQTT configuration
	
	# MQTT over TLS/SSL
			listener 8883
			cafile /etc/mosquitto/certs/ca.crt
			certfile /etc/mosquitto/certs/hostname.crt
			keyfile /etc/mosquitto/certs/hostname.key
	
	# End of MQTT over TLS/SLL configuration
	
	# Plain WebSockets configuration
			listener 9001
			protocol websockets
	
	# End of plain Websockets configuration
	
	# WebSockets over TLS/SSL
			listener 9883
			protocol websockets
			cafile /etc/mosquitto/certs/ca.crt
			certfile /etc/mosquitto/certs/hostname.crt
			keyfile /etc/mosquitto/certs/hostname.key

 * 
 * @author Raghunathan R, www.ethicstech.in
 * @version 1.0.1
 * @since  2017-02-01
 */
public class LogEventPahoMQTTDao implements LogEventDao {

    private static final Logger LOG = LoggerFactory.getLogger(LogEventPahoMQTTDao.class);

	private String kaaEventMessage = "dump message from appender.";
	private int qos = 2;
	private String broker = "tcp://localhost:1883";
	private String clientId = "sandbox100";
	private MqttClient mqttClient = null;
	private java.lang.String host = "localhost";
	private int port = 1883;
	private boolean ssl = false;
	private boolean verifySslCert = false;
	private java.lang.String username = null;
	private java.lang.String password = null;
	private java.lang.String mqtttopic = "home/kaa/topic";
	private MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

	/**
	 * 
	 * @param configuration
	 * @throws Exception
	 */
    public LogEventPahoMQTTDao(PahoMqttConfig configuration) throws Exception {
		initKaaConfig(configuration);
		mqttConnectionConfiguration();
    }

	/**
	 * This method used to initializations.
	 * @param configuration
	 */
	private void initKaaConfig(PahoMqttConfig configuration){
		StringBuffer sBuffer = new StringBuffer(400);
		try {
			if (configuration!=null){
				this.host=configuration.getHost();
				this.port=configuration.getPort();
				this.ssl = configuration.getSsl();
				this.verifySslCert=configuration.getVerifySslCert();
				this.username=configuration.getUsername();
				this.password=configuration.getPassword();
				this.clientId=configuration.getClientId();
				this.mqtttopic=configuration.getMqtttopic();
				this.qos=configuration.getQos();
				sBuffer.append("tcp://");
				sBuffer.append(this.host);
				sBuffer.append(":");
				sBuffer.append(this.port);
				this.broker = sBuffer.toString();
			}else {
				LOG.info("Check your configuration");
			}
		} catch (Exception e) {
			LOG.error("initKaaConfig::" + e);
			sBuffer=null;
		}
		sBuffer=null;
 		LOG.info("Mqtt broker: " + this.broker);
 	}
	/**
	 * This method is used to configure the MQTT server and clients.
	 */
	private void mqttConnectionConfiguration(){
		MemoryPersistence persistence = null;
		try {
			persistence = new MemoryPersistence();
			this.mqttClient = new MqttClient(this.broker, this.clientId, persistence);
			this.mqttConnectOptions.setCleanSession(true);
			this.mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

			if (this.username!=null && !this.username.isEmpty()){
				this.mqttConnectOptions.setUserName(this.username);
				if (this.password!=null)
					this.mqttConnectOptions.setPassword(this.password.toCharArray());
			}
			
			if (this.ssl){
				//TODO
				/*Properties sslProps = new Properties();
	            addSystemProperty("com.ethicstech.ssl..protocol", sslProps);
	            addSystemProperty("com.ethicstech.ssl..contextProvider", sslProps);
	            addSystemProperty("com.ethicstech.ssl..keyStore", sslProps);
	            addSystemProperty("com.ethicstech.ssl..keyStorePassword", sslProps);
	            addSystemProperty("com.ethicstech.ssl..keyStoreType", sslProps);
	            addSystemProperty("com.ethicstech.ssl..keyStoreProvider", sslProps);
	            addSystemProperty("com.ethicstech.ssl..trustStore", sslProps);
	            addSystemProperty("com.ethicstech.ssl..trustStorePassword", sslProps);
	            addSystemProperty("com.ethicstech.ssl..trustStoreType", sslProps);
	            addSystemProperty("com.ethicstech.ssl..trustStoreProvider", sslProps);
	            addSystemProperty("com.ethicstech.ssl..enabledCipherSuites", sslProps);
	            addSystemProperty("com.ethicstech.ssl..keyManager", sslProps);
	            addSystemProperty("com.ethicstech.ssl..trustManager", sslProps);
	            this.mqttConnectOptions.setSSLProperties(sslProps);*/
			}
			if (verifySslCert){
				//TODO
			}
		} catch (MqttException mnex) {
			LOG.debug("mqttConnectionConfiguration... {} exception", mnex);
		}
	}
	
		
    @Override
    public List<LogEvent> publish(RecordHeader recordHeader, List<LogEventDto> logEventDtos) throws Exception{
      	MqttMessage mqttMessage =null;
    	LogEvent logEvent = null;
    	String kaaEventMessage=null;
       	LOG.info("RecordHeader {} , LogEventDto {} ", recordHeader,logEventDtos);
       	
        List<org.kaaproject.kaa.server.appenders.pahomqtt.appender.LogEvent> logEvents = new ArrayList<>(logEventDtos.size());
        
        for (LogEventDto logEventDto : logEventDtos) {
            try {
            	logEvent = doProcessEvent( logEventDto);
            	if (logEvent!=null){
            		logEvents.add(logEvent);
            		kaaEventMessage = logEvent.getEvent();
            	}	
            	if (kaaEventMessage!=null && kaaEventMessage.length()>0){
         			mqttMessage = new MqttMessage(kaaEventMessage.toString().getBytes( "UTF-8"));
         			if (this.qos <=2){
         				mqttMessage.setQos(this.qos);
         			}else{
         				mqttMessage.setQos(2);
         			}
           		}
            	LOG.debug("MQTT Publishing {} log event with configuration {}", logEvent,this.mqttClient);
            	if (this.mqttConnectOptions==null || this.mqttClient==null){
              		mqttConnectionConfiguration();
             	}
            	if (this.mqttConnectOptions!=null && this.mqttClient!=null){
            		if (!this.mqttClient.isConnected()){
            			this.mqttClient.connect(this.mqttConnectOptions);
            		}
            	}
          		LOG.info("MQTT {} is creating new configurations with client {} " , this.mqttConnectOptions,this.mqttClient);
            	if (this.mqttClient.isConnected() && mqttMessage!=null){
              		this.mqttClient.publish(this.mqtttopic, mqttMessage);
             	}
            } catch (IllegalArgumentException exmsg) {
				LOG.info("Qos : Check the qos values.." + exmsg.getLocalizedMessage());			
            } catch (MqttException mnex) {
            	LOG.debug("Publish : MqttException... {} exception", mnex);
				throw mnex;
            } catch (Exception ex) {	
            	LOG.error("Publish : " + ex);
            }
            LOG.debug("MQTT Log {} published to channel.", this.kaaEventMessage);
        }
        return logEvents;
    }
    
	/**
	 * This method used to get the event messages.
	 * @param logEventDto
	 * @return
	 */
	private org.kaaproject.kaa.server.appenders.pahomqtt.appender.LogEvent doProcessEvent(LogEventDto logEventDto) {
		String header = "";
		String event = "";
		LogEvent logEvent = new org.kaaproject.kaa.server.appenders.pahomqtt.appender.LogEvent();
		try {
			header = logEventDto.getHeader();
			event = logEventDto.getEvent();
			LOG.info("RECORDS Header :: " + header);
			LOG.info("RECORDS Event ::" + event);
			logEvent.setEvent(event);
			logEvent.setHeader(header);
		} catch (Exception e) {
			LOG.error("LogEvent: ", e);
		}
		return logEvent;
	}

    @Override
    public void close() {
    	/*LOG.info("MQTT Client close start.");
		try {
			if (this.mqttClient!=null){
				this.mqttClient.disconnect();
				LOG.info("Disconnected");
			}
		} catch (MqttException ex) {
			LOG.error(ex.getMessage());
			//this.mqttClient= null;
		}
		LOG.info("MQTT Client close end.");*/
    }
}

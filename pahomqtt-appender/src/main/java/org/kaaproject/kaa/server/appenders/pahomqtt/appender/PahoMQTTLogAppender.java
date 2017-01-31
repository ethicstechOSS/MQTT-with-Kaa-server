package org.kaaproject.kaa.server.appenders.pahomqtt.appender;

import java.text.MessageFormat;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.pahomqtt.config.gen.PahoMqttConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Raghunathan R
 * @version 1.0.1
 *
 */

public class PahoMQTTLogAppender extends AbstractLogAppender<PahoMqttConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(PahoMQTTLogAppender.class);
    
    private LogEventDao logEventDao=null;
    private boolean closed = false;

    public PahoMQTTLogAppender() {
    	super(PahoMqttConfig.class);
    }
    
    /**
     * Inits the appender from configuration.
     *
     * @param appender the metadata object that contains useful info like application token, tenant id, etc.
     * @param configuration the configuration object that you have specified during appender provisioning.
     */
    @Override
    protected void initFromConfiguration(LogAppenderDto appender, PahoMqttConfig configuration) {
   	try {
     		LOG.info("***** Initializing Paho MQTT Appender Configuration......=>" + configuration);
			this.logEventDao = new LogEventPahoMQTTDao(configuration);
        } catch (Exception e) {
            LOG.error("Failed to init Paho MQTT log appender: ", e);
        }
    }
 
    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener) {
    	RecordHeader localheader= header;
       	try {
			if (!closed) {
				try {
					LOG.debug("[{}] appending {} logs to Paho MQTT channel", getApplicationToken(),
							logEventPack.getEvents().size());
					
					List<LogEventDto> listdtos = generateLogEvent(logEventPack, header);
					LOG.debug("[{}] saving {} objects", getApplicationToken(), listdtos.size());
					if (listdtos.size()>0) {
						this.logEventDao.publish(localheader, listdtos);
						LOG.debug("[{}] published {} logs to Paho MQTT channel", getApplicationToken(),
								logEventPack.getEvents().size());
					}
					listener.onSuccess();
				} catch (Exception e) {
					LOG.error(MessageFormat.format("[{0}] Attempted to publish logs failed due to internal error",
							getName()), e);
					listener.onInternalError();
				}
			} else {
				LOG.info("Attempted to publish to closed appender named [{}].", getName());
				listener.onInternalError();
			} 
		} catch (Exception e) {
			LOG.error(MessageFormat.format("[{0}] Attempted to publish logs failed due to internal error",
					getName()), e);
		}
    }
    
    /**
     * Closes this appender and releases any resources associated with it.
     *
     */
    @Override
    public void close() {
    	if (!closed) {
            closed = true;
            if (logEventDao != null) {
                logEventDao.close();
                logEventDao = null;
            }
        }
    }
}

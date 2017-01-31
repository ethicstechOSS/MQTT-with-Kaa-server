package org.kaaproject.kaa.server.appenders.pahomqtt.appender;

import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;

/**
 * 
 * @author Raghunathan R
 * @version 1.0.1
 *
 */
public interface LogEventDao {
	
    List<LogEvent> publish(RecordHeader header, List<LogEventDto> logEventDtos) throws Exception;
    
    void close();
    
}
